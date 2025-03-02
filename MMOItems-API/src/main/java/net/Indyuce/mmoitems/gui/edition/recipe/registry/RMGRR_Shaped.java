package net.Indyuce.mmoitems.gui.edition.recipe.registry;

import io.lumine.mythic.lib.api.crafting.ingredients.ShapedIngredient;
import io.lumine.mythic.lib.api.crafting.outputs.MRORecipe;
import io.lumine.mythic.lib.api.crafting.outputs.MythicRecipeOutput;
import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeBlueprint;
import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeStation;
import io.lumine.mythic.lib.api.crafting.recipes.ShapedRecipe;
import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.util.Ref;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.crafting.MMOItemUIFilter;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMGRI_Shaped;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_AmountOutput;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_HideFromBook;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RMG_Shaped;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeEditorGUI;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class RMGRR_Shaped implements RecipeRegistry {

    @NotNull @Override public String getRecipeConfigPath() { return "shaped"; }
    @NotNull @Override public String getRecipeTypeName() { return "Shaped"; }

    @NotNull final ItemStack displayListItem = RecipeEditorGUI.rename(new ItemStack(Material.CRAFTING_TABLE), FFPMMOItems.get().getExampleFormat() + "有序合成配方");
    @NotNull @Override public ItemStack getDisplayListItem() { return displayListItem; }

    @Override public void openForPlayer(@NotNull EditionInventory inv, @NotNull String recipeName, Object... otherParams) {
        new RMG_Shaped(inv.getNavigator(), inv.getEdited(), recipeName, this).open(inv);
    }

    @NotNull
    @Override
    public MythicRecipeBlueprint sendToMythicLib(@NotNull MMOItemTemplate template, @NotNull ConfigurationSection recipeTypeSection, @NotNull String recipeName, @NotNull Ref<NamespacedKey> namespace, @NotNull FriendlyFeedbackProvider ffp) throws IllegalArgumentException {

        // Read some values
        ConfigurationSection recipeSection = RecipeEditorGUI.moveInput(recipeTypeSection, recipeName);

        NamespacedKey nk = namespace.getValue();
        if (nk == null) { throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "非法 (空) 命名空间")); }

        // Identify the input
        ShapedRecipe input = shapedRecipeFromList(nk.getKey(), new ArrayList<>(recipeSection.getStringList(RecipeEditorGUI.INPUT_INGREDIENTS)), ffp);
        if (input == null) { throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "仅包含空气的合成配方, $f已忽略$b.")); }

        // Read the options and output
        ShapedRecipe output = shapedRecipeFromList(nk.getKey(), new ArrayList<>(recipeSection.getStringList(RecipeEditorGUI.OUTPUT_INGREDIENTS)), ffp);
        int outputAmount = recipeSection.getInt(RBA_AmountOutput.AMOUNT_INGREDIENTS, 1);
        boolean hideBook = recipeSection.getBoolean(RBA_HideFromBook.BOOK_HIDDEN, false);

        // Build Output
        ShapedRecipe outputItem = ShapedRecipe.single(nk.getKey(),  new ProvidedUIFilter(MMOItemUIFilter.get(), template.getType().getId(), template.getId(), Math.max(outputAmount, 1)));
        MythicRecipeOutput outputRecipe = new MRORecipe(outputItem, output);

        // That's our blueprint :)
        MythicRecipeBlueprint ret = new MythicRecipeBlueprint(input, outputRecipe, nk);

        // Required permission?
        RandomStatData perm = template.getBaseItemData().get(ItemStats.CRAFT_PERMISSION);
        if (perm instanceof StringData) {

            // Ah yes
            String permission = ((StringData) perm).getString();

            // Finally
            if (permission != null) { ret.addRequiredPermission(permission); } }

        // Enable it
        ret.deploy(MythicRecipeStation.WORKBENCH, namespace);

        // Hide book if specified
        if (hideBook) { namespace.setValue(null); }

        // That's it
        return ret;
    }

    /**
     * Shorthand for reading list of strings that are intended to be shaped recipes: <br><br>
     * <code>
     *     m MATERIAL AMETHYST|m MATERIAL AMETHYST|m MATERIAL AMETHYST      <br>
     *     v AIR 0|v STICK 0|v AIR 0                                        <br>
     *     v AIR 0|v STICK 0|v AIR 0
     * </code>
     *
     * @param namespace Some name to give to this thing, it can be anything really.
     *
     * @param recipe The list of strings, probably directly from your YML Config
     *
     * @param ffp Provider of failure text
     *
     * @return The most optimized version of this recipe, ready to be put into a Blueprint.
     *         <br> <br>
     *         Will be <code>null</code> if it would have been only AIR.
     *
     * @throws IllegalArgumentException If any ingredient is illegal (wrong syntax or something).
     */
    @Nullable public static ShapedRecipe shapedRecipeFromList(@NotNull String namespace, @NotNull ArrayList<String> recipe, @NotNull FriendlyFeedbackProvider ffp) throws IllegalArgumentException {

        // All right lets read them
        ArrayList<ShapedIngredient> poofs = new ArrayList<>();
        boolean nonAirFound = false;
        int rowNumber = 0;
        //UPT//MMOItems.log("\u00a7e" + namespace + "\u00a77 loading:");

        // Read through the recipe
        for (String row : recipe) {
            //UPT//MMOItems.log("\u00a7e-\u00a77 " + row);

            // Update
            String updatedRow = RMGRI_Shaped.updateRow(row);
            //UPT//MMOItems.log("\u00a7eU-\u00a77 " + updatedRow);

            /*
             * This row could be in either legacy or new format, and we will assume no combination of them.
             *
             * Either:
             *  ANYTHING ANY.THING ANYTHING
             *
             * or
             *  A NYT THIN G|A NYT THING|A NYT THIN G
             */

            // What are the three ingredients encoded in this row?
            String[] positions;

            if (updatedRow.contains("|")) {

                // Split by |s
                positions = updatedRow.split("\\|");

                // Is legacy
            } else {

                // Split by spaces
                positions = updatedRow.split(" ");
            }

            // Size not 3? BRUH
            if (positions.length != 3) { throw new IllegalArgumentException("无效的制作表 $u" + updatedRow + "$b ($f不完全是 3 个格子的长宽度$b)."); }

            // Identify
            ProvidedUIFilter left = RecipeEditorGUI.readIngredientFrom(positions[0], ffp);
            ProvidedUIFilter center = RecipeEditorGUI.readIngredientFrom(positions[1], ffp);
            ProvidedUIFilter right = RecipeEditorGUI.readIngredientFrom(positions[2], ffp);
            if (!left.isAir()) { nonAirFound = true; }
            if (!center.isAir()) { nonAirFound = true; }
            if (!right.isAir()) { nonAirFound = true; }

            /*
             * To detect if a recipe can be crafted in the survival inventory (and remove extra AIR),
             * we must see that a whole row AND a whole column be air. Not any column or row though,
             * but any of those that do not cross the center.
             *
             * If a single left item is not air, LEFT is no longer an unsharped column.
             * If a single right item is not air, RIGHT is no longer an unsharped column.
             *
             * All items must be air in TOP or BOTTOM for they to be unsharped.
             */

            // Bake
            ShapedIngredient leftIngredient = new ShapedIngredient(left, 0, -rowNumber);
            ShapedIngredient centerIngredient = new ShapedIngredient(center, 1, -rowNumber);
            ShapedIngredient rightIngredient = new ShapedIngredient(right, 2, -rowNumber);

            // Parse and add
            poofs.add(leftIngredient);
            poofs.add(centerIngredient);
            poofs.add(rightIngredient);

            // Prepare for next row
            rowNumber++;
        }
        if (!nonAirFound) { return null; }

        // Make ingredients
        return ShapedRecipe.unsharpen((new ShapedRecipe(namespace, poofs)));
    }
}
