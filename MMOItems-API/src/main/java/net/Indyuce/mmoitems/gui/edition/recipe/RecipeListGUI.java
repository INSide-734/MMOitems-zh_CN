package net.Indyuce.mmoitems.gui.edition.recipe;

import io.lumine.mythic.lib.api.util.ItemFactory;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.version.VersionUtils;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeEditorGUI;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.RecipeRegistry;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * The Recipe List GUI is in charge of showing all the recipes
 * of a specific kind, and allowing the user to choose which
 * to edit, delete, or create.
 * <br> <br>
 * Because theoretically the recipes could overflow, this does
 * support paging which is UGH who is ever going to make over 20
 * recipes for a single item but whatever.
 *
 * @author Gunging
 */
public class RecipeListGUI extends EditionInventory {

    @NotNull final ItemStack nextPage = ItemFactory.of(Material.ARROW).name("\u00a77下一页").build();
    @NotNull final ItemStack prevPage = ItemFactory.of(Material.ARROW).name("\u00a77上一页").build();
    @NotNull final ItemStack noRecipe = ItemFactory.of(Material.BLACK_STAINED_GLASS_PANE).name("\u00a77没有合成配方").build();

    @NotNull final RecipeRegistry recipeType;
    @NotNull public RecipeRegistry getRecipeRegistry() { return recipeType; }

    @NotNull final ItemStack listedItem;
    @NotNull public ItemStack getListedItem() { return listedItem; }

    @NotNull final ArrayList<String> recipeNames = new ArrayList<>();
    @NotNull public ArrayList<String> getRecipeNames() { return recipeNames; }

    boolean invalidRecipe;

    public RecipeListGUI(@NotNull Navigator navigator, @NotNull MMOItemTemplate template, @NotNull RecipeRegistry kind) {
        super(navigator, template);
        recipeType = kind;

        // Which item to fill the area with?
        listedItem = getRecipeRegistry().getDisplayListItem();

        // Obtain the crafting section
        ConfigurationSection section = RecipeEditorGUI.getSection(getEditedSection(), "crafting");
        ConfigurationSection type = RecipeEditorGUI.getSection(section, kind.getRecipeConfigPath());

        // What is all the recipes within this kind?
        recipeNames.addAll(type.getKeys(false));

        page = 0;
    }

    int createSlot = -1;
    @NotNull final HashMap<Integer, String> recipeMap = new HashMap<>();

    @Override
    public String getName() {
        return "选择 " + getRecipeRegistry().getRecipeTypeName() + " 配方";
    }

    /**
     * Updates the inventory, refreshes the page number whatever.
     */
    @Override
    public void arrangeInventory() {

        // Start fresh
        recipeMap.clear();
        createSlot = -1;

        // Include page buttons
        if (page > 0) { inventory.setItem(27, prevPage); }
        if (recipeNames.size() >= ((page + 1) * 21)) { inventory.setItem(36, nextPage); }

        // Fill the space I guess
        for (int p = 21 * page; p < 21 * (page + 1); p++) {

            /*
             * The job of this is to identify which slots of this
             * inventory will trigger which action.
             *
             * If the slot has a recipe to edit, a connection will
             * be made between clicking this and which recipe to
             * edit via the HashMap 'recipeMap'
             *
             * But for that we must calculate which absolute slot
             * of this inventory are we talking about...
             */
            int absolute = page(p);

            /*
             * Going through the whole page, first thing
             * to check is that there is a recipe here.
             *
             * Note that clicking the very next glass pane
             * creates a new recipe.
             */
            if (p == recipeNames.size()) {

                // Rename list item...
                inventory.setItem(absolute, RecipeEditorGUI.rename(new ItemStack(Material.NETHER_STAR),   FFPMMOItems.get().getBodyFormat() + "新建配方 " + SilentNumbers.getItemName(getListedItem(), false)));

                // If this slot is clicked, a new recipe will be created.
                createSlot = absolute;

            // The current item is greater, fill with empty glass panes
            } else if (p > recipeNames.size()) {

                // Just snooze
                inventory.setItem(absolute, noRecipe);

            // There exists a recipe for this slot
            } else {

                // Display name
                inventory.setItem(absolute, RecipeEditorGUI.rename(getListedItem().clone(),  FFPMMOItems.get().getBodyFormat() + "编辑 " + FFPMMOItems.get().getInputFormat() + recipeNames.get(p)));

                // Store
                recipeMap.put(absolute, recipeNames.get(p));
            }
        }
    }

    public static int page(int p) {

        // Remove multiples of 21
        int red = SilentNumbers.floor(p / 21.00D);
        p -= red * 21;

        /*
         * A page is the third, fourth, and fifth rows, excluding the first and last column.
         *
         * #1 Obtain the relative column, and relative row
         *
         * #2 Convert to absolute inventory positions
         */
        int relRow = SilentNumbers.floor(p / 7.00D);
        int relCol = p - (7 * relRow);

        // Starting at the third row, each row adds 9 slots.
        int rowAdditive = 18 + (relRow * 9);
        int columnAdditive = relCol + 1;

        // Sum to obtain final
        return rowAdditive + columnAdditive;
    }


    @Override public void whenClicked(InventoryClickEvent event) {

        // Clicked inventory was not the observed inventory? Not our business
        if ((VersionUtils.getView(event).getTopInventory() != event.getClickedInventory())) { return; }

        // Disallow any clicking.
        event.setCancelled(true);

        if (invalidRecipe) { return; }

        // Selecting a recipe to edit (or creating?)
        if (event.getAction() == InventoryAction.PICKUP_ALL) {

            // Previous page
            if (event.getSlot() == 27) {

                // Retreat page
                page--;
                refreshInventory();

            // Next Page
            } else if (event.getSlot() == 36) {

                // Advance page
                page++;
                refreshInventory();

            // Create a new recipe
            } else if (event.getSlot() == createSlot) {

                // Well make sure tha name is not taken
                String chadName = String.valueOf(recipeMap.size() + 1);
                if (recipeMap.containsValue(chadName)) { chadName = chadName + "_" + UUID.randomUUID(); }

                // Create a new one with that chad name
                getRecipeRegistry().openForPlayer(this, chadName);

            // Might be clicking a recipe to edit then
            } else if (event.getSlot() > 18) {

                // A recipe exists of this name?
                String recipeName = recipeMap.get(event.getSlot());

                // Well, found anything?
                if (recipeName != null) {

                    // Open that menu for the player
                    getRecipeRegistry().openForPlayer(this, recipeName);
                }
            }

        // Deleting a recipe
        } else if (event.getAction() == InventoryAction.PICKUP_HALF) {

            // A recipe exists of this name?
            String recipeName = recipeMap.get(event.getSlot());

            // Seems there was
            if (recipeName != null) {

                // Delete that
                ConfigurationSection section = RecipeEditorGUI.getSection(getEditedSection(), "crafting");
                ConfigurationSection type = RecipeEditorGUI.getSection(section, getRecipeRegistry().getRecipeConfigPath());
                recipeNames.remove(recipeName);
                type.set(recipeName, null);

                // Register edition
                registerTemplateEdition();
            }
        }
    }
}
