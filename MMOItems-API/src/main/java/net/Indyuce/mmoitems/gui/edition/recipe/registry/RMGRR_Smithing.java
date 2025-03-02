package net.Indyuce.mmoitems.gui.edition.recipe.registry;

import io.lumine.mythic.lib.api.crafting.ingredients.MythicRecipeIngredient;
import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeBlueprint;
import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeStation;
import io.lumine.mythic.lib.api.crafting.recipes.ShapedRecipe;
import io.lumine.mythic.lib.api.crafting.recipes.ShapelessRecipe;
import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.util.Ref;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.api.crafting.recipe.CustomSmithingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.SmithingCombinationType;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMGRI_Smithing;
import net.Indyuce.mmoitems.gui.edition.recipe.button.*;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RMG_Smithing;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeEditorGUI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RMGRR_Smithing implements RecipeRegistry {

    @NotNull @Override public String getRecipeTypeName() { return "Smithing"; }
    @NotNull @Override public String getRecipeConfigPath() { return "smithing"; }

    @NotNull final ItemStack displayListItem = RecipeEditorGUI.rename(new ItemStack(Material.SMITHING_TABLE), FFPMMOItems.get().getExampleFormat() + "锻造合成配方");
    @NotNull @Override public ItemStack getDisplayListItem() { return displayListItem; }

    @Override public void openForPlayer(@NotNull EditionInventory inv, @NotNull String recipeName, Object... otherParams) {
        new RMG_Smithing(inv.getNavigator(), inv.getEdited(), recipeName, this).open(inv);
    }

    @NotNull
    @Override
    public MythicRecipeBlueprint sendToMythicLib(@NotNull MMOItemTemplate template, @NotNull ConfigurationSection recipeTypeSection, @NotNull String recipeName, @NotNull Ref<NamespacedKey> namespace, @NotNull FriendlyFeedbackProvider ffp) throws IllegalArgumentException {

        // Prior Preparations (update old formats)
        RecipeEditorGUI.moveInput(recipeTypeSection, recipeName);

        // Read some values
        ConfigurationSection recipeSection = RecipeEditorGUI.getSection(recipeTypeSection, recipeName);
        NamespacedKey nk = namespace.getValue();
        if (nk == null) { throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "非法 (空) 命名空间")); }

        //region Identify the input

        // Find value in files
        String input = RMGRI_Smithing.updateIngredients(recipeSection.getString(RecipeEditorGUI.INPUT_INGREDIENTS));
        String[] inputSplit = input.split("\\|");

        // All right lets read them
        ProvidedUIFilter itemPoof = RecipeEditorGUI.readIngredientFrom(inputSplit[0], ffp);
        ProvidedUIFilter ingotPoof = RecipeEditorGUI.readIngredientFrom(inputSplit[1], ffp);
        if (itemPoof.isAir() || ingotPoof.isAir()) { throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "含有空气的锻造配方, $f已忽略$b.")); }

        // Make ingredients
        MythicRecipeIngredient itemIngredient = new MythicRecipeIngredient(itemPoof);
        MythicRecipeIngredient ingotIngredient = new MythicRecipeIngredient(ingotPoof);

        // Make input recipes
        ShapelessRecipe inputItem = new ShapelessRecipe(nk.getKey(), itemIngredient);
        ShapelessRecipe inputIngot = new ShapelessRecipe(nk.getKey(), ingotIngredient);
        //endregion

        //region Identify the output of ingredients
        // Find value in files
        String output = RMGRI_Smithing.updateIngredients(recipeSection.getString(RecipeEditorGUI.OUTPUT_INGREDIENTS));
        String[] outputSplit = output.split("\\|");

        // All right lets read them
        ProvidedUIFilter itemOPoof = RecipeEditorGUI.readIngredientFrom(outputSplit[0], ffp);
        ProvidedUIFilter ingotOPoof = RecipeEditorGUI.readIngredientFrom(outputSplit[1], ffp);

        // Make output recipes
        ShapedRecipe outputItem = itemOPoof.isAir() ? null : ShapedRecipe.single(nk.getKey(), itemOPoof);
        ShapedRecipe outputIngot = ingotOPoof.isAir() ? null : ShapedRecipe.single(nk.getKey(), ingotOPoof);
        //endregion

        // Read the options and output
        int outputAmount = recipeSection.getInt(RBA_AmountOutput.AMOUNT_INGREDIENTS, 1);
        boolean dropGems = recipeSection.getBoolean(RBA_DropGems.SMITH_GEMS, false);
        SmithingCombinationType enchantEffect = readSCT(recipeSection.getString(RBA_SmithingEnchantments.SMITH_ENCHANTS));
        SmithingCombinationType upgradeEffect = readSCT(recipeSection.getString(RBA_SmithingUpgrades.SMITH_UPGRADES));

        // Build Output
        CustomSmithingRecipe outputRecipe = new CustomSmithingRecipe(template, dropGems, enchantEffect, upgradeEffect, outputAmount);
        outputRecipe.setMainInputConsumption(outputItem);
        outputRecipe.setIngotInputConsumption(outputIngot);

        // That's our blueprint :)
        MythicRecipeBlueprint ret = new MythicRecipeBlueprint(inputItem, outputRecipe, nk);
        ret.addSideCheck("ingot", inputIngot);

        // Enable it
        ret.deploy(MythicRecipeStation.SMITHING, namespace);

        // That's it
        return ret;
    }

    @NotNull SmithingCombinationType readSCT(@Nullable String str) {

        // Default value is max
        if (str == null) { return SmithingCombinationType.MAXIMUM; }

        // Correct syntax or default
        try { return SmithingCombinationType.valueOf(str); } catch (IllegalArgumentException ignored) { return SmithingCombinationType.MAXIMUM; }
    }
}
