package net.Indyuce.mmoitems.gui.edition.recipe.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.crafting.uifilters.VanillaUIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.UIFilterManager;
import io.lumine.mythic.lib.api.util.ItemFactory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.QuickNumberRange;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.util.AdventureUtils;
import io.lumine.mythic.lib.version.VersionUtils;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_AmountOutput;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RecipeButtonAction;
import net.Indyuce.mmoitems.gui.edition.recipe.interpreter.RMG_RecipeInterpreter;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.RecipeRegistry;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Inventory displayed when the user edits one of the many
 * recipes associated to an item. It has many functions:
 * <br><br>
 * * Choose amount of output <br>
 * * Choose input (of course) <br>
 * * Specify output in the input slots <br>
 * * Preview the recipe <br>
 * * Reload the recipe <br>
 * * Make it auto-unlock in crafting book <br>
 *
 * @author Gunging
 */
@SuppressWarnings("unused")
public abstract class RecipeEditorGUI extends EditionInventory {

    /**
     * An editor for a recipe of this crafting system.
     * <br> <br>
     * Mind the difference between Recipe and Recipe Type: <br> <code>
     * <p>
     * > Recipe: This specific recipe being edited. An item may have multiple recipes of the same type. <br><br>
     * > Recipe Type: How to use the recipe, is it Shaped, Shapeless, Smithing, Smelting...?
     * </code>
     *
     * @param navigator      Current UI navigator
     * @param template       MMOItem Template being edited
     * @param recipeName     Name of this particular Recipe
     * @param recipeRegistry Load/Save Information of this Recipe Type
     */
    public RecipeEditorGUI(@NotNull Navigator navigator, @NotNull MMOItemTemplate template, @NotNull String recipeName, @NotNull RecipeRegistry recipeRegistry) {
        super(navigator, template);

        // Store name
        this.recipeName = recipeName;
        this.recipeRegistry = recipeRegistry;

        // Update old formats
        moveInput();

        // Identify sections
        craftingSection = getSection(getEditedSection(), "crafting");
        typeSection = getSection(craftingSection, getRecipeRegistry().getRecipeConfigPath());
        nameSection = getSection(typeSection, recipeName);

        // In general, they all have the amount output button
        //noinspection NestedAssignment
        addButton(amountButton = new RBA_AmountOutput(this, getCachedItem().clone()));
    }

    @Override
    public String getName() {
        return "编辑 " + getRecipeRegistry().getRecipeTypeName() + " 配方";
    }

    // Button Bar Buttons
    @NotNull
    final ItemStack nextButtonPage = ItemFactory.of(Material.SPECTRAL_ARROW).name("\u00a7e更多设置 \u00a7c»").build();
    @NotNull
    final ItemStack prevButtonPage = ItemFactory.of(Material.SPECTRAL_ARROW).name("\u00a7c« \u00a7e更多设置").build();
    @NotNull
    public final ItemStack noButton = ItemFactory.of(Material.IRON_BARS).name("\u00a78---").build();

    // Ingredient-Related Buttons
    @NotNull
    public final ItemStack emptySlot = ItemFactory.of(Material.BARRIER).name("\u00a77无物品").build();
    @NotNull
    public final ItemStack airSlot = ItemFactory.of(Material.STRUCTURE_VOID).name("\u00a77无物品").build();

    /**
     * [ID].base.crafting
     */
    @NotNull
    final ConfigurationSection craftingSection;

    /**
     * @return [ID].base.crafting
     */
    @NotNull
    public ConfigurationSection getCraftingSection() {
        return craftingSection;
    }

    /**
     * [ID].base.crafting.[TYPE]
     */
    @NotNull
    final ConfigurationSection typeSection;

    /**
     * @return [ID].base.crafting.[TYPE]
     */
    @NotNull
    public ConfigurationSection getTypeSection() {
        return typeSection;
    }

    /**
     * [ID].base.crafting.[TYPE].[NAME]
     */
    @NotNull
    final ConfigurationSection nameSection;

    /**
     * @return [ID].base.crafting.[TYPE].[NAME]
     */
    @NotNull
    public ConfigurationSection getNameSection() {
        return nameSection;
    }

    @NotNull
    final RecipeRegistry recipeRegistry;

    /**
     * @return The information to save and load this recipe.
     */
    @NotNull
    public RecipeRegistry getRecipeRegistry() {
        return recipeRegistry;
    }

    /**
     * The reference to the Amount Button, for ease of access
     * of the ItemStack displayed for the output of this recipe.
     */
    @NotNull
    final RBA_AmountOutput amountButton;

    /**
     * @return The reference to the Amount Button, for ease of access
     *         of the ItemStack displayed for the output of this recipe.
     */
    @NotNull
    public RBA_AmountOutput getAmountButton() {
        return amountButton;
    }

    @NotNull
    final String recipeName;

    /**
     * @return An item may have multiple recipes, this is the name
     *         of the one being edited. So far, historically, they
     *         have just been a number.
     *         <br>
     *         <br>
     *         In YML, <code>[ID].crafting.[recipe].[name]</code> this
     *         string is the value of [name]
     *         <br>
     *         Ex. <code>STEEL_SWORD.crafting.shaped.1</code>
     */
    @NotNull
    public String getRecipeName() {
        return recipeName;
    }

    int buttonsPage;
    /**
     * Map containing the absolute inventory slot links to the buttons placed there.
     */
    @NotNull
    final HashMap<Integer, RecipeButtonAction> buttonsMap = new HashMap<>();

    /**
     * Puts the general buttons, used for any Recipe Maker variant.
     * <br><br>
     * The general template, where K is the edge, = is the equals edge,
     * and r is the result item, looks like this:
     * <br>
     * <code>K K K K K K K K K </code><br>
     * <code>K K K = K K K K K </code><br>
     * <code>K K K = K K K K r </code><br>
     * <code>K K K = K K K K K </code>
     * <br><br>
     * This is further edited, then, in {@link #putRecipe()}, where
     * for example, the crafting recipe, will show the items and empty slots
     * in the correct places:
     * <br>
     * <code>K K K K K K K K K </code><br>
     * <code>g g g = - - - K K </code><br>
     * <code>- s - = - - - K r </code><br>
     * <code>- s - = - - - K K </code>
     */
    public void putButtons() {

        // Ignore negative rows
        if (getButtonsRow() < 0) {
            return;
        }

        // Clear
        buttonsMap.clear();

        // Include page buttons
        if (buttonsPage > 0) {
            inventory.setItem((getButtonsRow() * 9) + 8, prevButtonPage);
        }
        if (buttonsMap.size() >= ((buttonsPage + 1) * 7)) {
            inventory.setItem((getButtonsRow() * 9), nextButtonPage);
        }

        // Fill the space I guess
        for (int p = 7 * buttonsPage; p < 7 * (buttonsPage + 1); p++) {

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
            int absolute = buttonRowPageClamp(p);

            /*
             * Going through the whole page, first thing
             * to check is that there is a recipe here.
             *
             * Note that clicking the very next glass pane
             * creates a new recipe.
             */
            if (p >= buttons.size()) {

                // Just snooze
                inventory.setItem(absolute, noButton);

                // There exists a recipe for this slot
            } else {

                // Get button
                RecipeButtonAction rmg = buttons.get(p);

                // Display
                inventory.setItem(absolute, rmg.getButton());

                // Store
                buttonsMap.put(absolute, rmg);
            }
        }
    }

    /**
     * Restrains a number between 1 and 7, which is the allowed
     * absolute slot values to put buttons onto.
     * <br> <br>
     * Basically, button #4 will be assigned to the fifth slot (#4) of
     * the {@link #getButtonsRow()}th Row, just like button #11
     *
     * @param p Button number, Ex. 8
     * @return Slot of the inventory it will be placed, Ex. 10
     */
    public int buttonRowPageClamp(int p) {

        /*
         * A page is the seven center slots of the #getButtonsRow()
         *
         * #1 Obtain the relative column, and relative row
         *
         * #2 Convert to absolute inventory positions
         */
        int red = SilentNumbers.floor(p / 7.00D);
        p -= red * 7;

        /*
         * A page is the seven center slots of the #getButtonsRow()
         *
         * #1 Obtain the relative column
         *
         * #2 Convert to absolute inventory positions
         */
        int rowAdditive = (getButtonsRow() * 9);
        int columnAdditive = p + 1;

        // Sum to obtain final
        return rowAdditive + columnAdditive;
    }

    /**
     * Should probably avoid this being row #0, since that will occlude
     * the back button and those other edition inventory buttons.
     *
     * @return The inventory row at which the buttons will display.
     */
    public abstract int getButtonsRow();

    /**
     * @param absolute Absolute slot clicked by the player, for example,
     *                 0 is the top left corner of the edition inventory.
     * @return <code>-1</code> If the slot is not one of the <b>input ingredient</b>
     *         slots, or a number greater or equal to zero depending on which input
     *         ingredient it is.
     */
    abstract int getInputSlot(int absolute);

    @Override
    public void arrangeInventory() {
        putButtons();
        putRecipe();
    }

    /**
     * Puts the buttons specific for this kind of recipe, display
     * in the correct places the input and output items.
     *
     * @see #putButtons() for a better, more lengthy description.
     */
    public abstract void putRecipe();

    /**
     * Get the item stack associated with this slot, depending
     * on Input or Output being edited. If it is air, it will
     * return the chad {@link #emptySlot} ItemStack.
     *
     * @param input Should fetch from the INPUT section of the YML Config?
     * @param slot  Which slot of the crafting table?
     * @return The correct stack to display.
     */
    @NotNull
    public ItemStack getDisplay(boolean input, int slot) {

        // Find poof
        ProvidedUIFilter poof = input ? getInterpreter().getInput(slot) : getInterpreter().getOutput(slot);

        // Null equals fail
        if (poof == null || poof.isAir()) {
            return isShowingInput() ? emptySlot : airSlot;
        }

        // Generate display
        return poof.getDisplayStack(null);
    }

    /**
     * All the buttons added by this recipe.
     */
    @NotNull
    final ArrayList<RecipeButtonAction> buttons = new ArrayList<>();

    /**
     * Registers a button to check when clicking the Edition Inventory for this recipe.
     *
     * @param rba Method to run and evaluate the button click.
     */
    public void addButton(@NotNull RecipeButtonAction rba) {
        buttons.add(rba);
    }

    @NotNull
    public final String[] recipeLog = {
            FriendlyFeedbackProvider.quickForPlayer(FFPMMOItems.get(), "在聊天中输入您想要的物品, 遵循任何格式:"),
            FriendlyFeedbackProvider.quickForPlayer(FFPMMOItems.get(), "原版: $e[MATERIAL] [AMOUNT] $bex $eDIAMOND 2.."),
            FriendlyFeedbackProvider.quickForPlayer(FFPMMOItems.get(), "MMOItem: $e[TYPE].[ID] [AMOUNT] $bex $eSWORD.CUTLASS 1.."),
            FriendlyFeedbackProvider.quickForPlayer(FFPMMOItems.get(), "其他: $e[KEY] [ARG] [DAT] [AMOUNT]$b (查看wiki)"),
            FriendlyFeedbackProvider.quickForPlayer(FFPMMOItems.get(), "\u00a78金额采用范围格式, $e[min]..[max]\u00a78, 如果未指定将为 $r1..\u00a78")};

    /**
     * @return The protocols to edit the ConfigurationSection based on the user input.
     */
    @NotNull
    public abstract RMG_RecipeInterpreter getInterpreter();

    @Override
    public void whenClicked(InventoryClickEvent event) {

        // Clicked inventory was not the observed inventory? Not our business
        if (VersionUtils.getView(event).getTopInventory() != event.getClickedInventory()) {
            return;
        }

        // Disallow any clicking.
        event.setCancelled(true);

        // Was it an ingredient slot?
        int ingredient = getInputSlot(event.getRawSlot());

        // Setting an ingredient?
        if (event.getAction() == InventoryAction.PICKUP_ALL) {

            // Is it an input slot?
            if (ingredient >= 0) {

                // Input or output?
                if (isShowingInput()) {

                    // Query user for input
                    new StatEdition(this, ItemStats.CRAFTING, INPUT, getInterpreter(), ingredient).enable(recipeLog);

                } else {

                    // Query user for output
                    new StatEdition(this, ItemStats.CRAFTING, OUTPUT, getInterpreter(), ingredient).enable(recipeLog);
                }

                // Maybe its a button
            } else {

                // Find button
                RecipeButtonAction rmg = buttonsMap.get(event.getRawSlot());

                // Found?
                if (rmg != null) {
                    rmg.runPrimary();
                }
            }

            // Removing an ingredient?
        } else if (event.getAction() == InventoryAction.PICKUP_HALF) {


            // Is it an input slot?
            if (ingredient >= 0) {

                // Input or output?
                if (isShowingInput()) {

                    // Delete Input
                    getInterpreter().deleteInput(ingredient);

                } else {

                    // Delete Output
                    getInterpreter().deleteOutput(getInputSlot(event.getRawSlot()));
                }

                // Register
                registerTemplateEdition();

                // Refresh yes
                refreshInventory();

                // Maybe its a button
            } else {

                // Find button
                RecipeButtonAction rmg = buttonsMap.get(event.getRawSlot());

                // Found?
                if (rmg != null) {
                    rmg.runSecondary();
                }
            }
        }
    }

    //region ############------- Input/Output Switch -------############
    /**
     * There are several reasons why this was the best way, however
     * unique looking. The best reason is that, for an user editing
     * many recipes at once, its quicker to just toggle it globally.
     * <p>
     * From a programmatic point of view, this is also easier to implement.
     */
    @NotNull
    static final HashMap<UUID, Boolean> showingInput = new HashMap<>();

    /**
     * Change between showing input and output for some user.
     *
     * @param whom Player the inventory is built for
     */
    public static void switchInputFor(@NotNull UUID whom) {

        // Flip
        showingInput.put(whom, !isShowingInputFor(whom));
    }

    /**
     * @param whom Player the inventory is built for
     * @return If the player wants to see the input recipes.
     */
    public static boolean isShowingInputFor(@NotNull UUID whom) {

        // The value, true by default
        return showingInput.getOrDefault(whom, true);
    }

    /**
     * @return If the player opening this inventory is looking at INPUT (as opposed to OUTPUT)
     */
    public boolean isShowingInput() {
        return isShowingInputFor(getPlayer().getUniqueId());
    }

    /**
     * Switch between input and output for this player.
     */
    public void switchInput() {
        switchInputFor(getPlayer().getUniqueId());
    }
    //endregion

    //region ############------- Constants -------############
    /*
     * Sure this could be in an enum but it annoys me to
     * make a new file just such few constants.
     */
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int PRIMARY = 2;
    public static final int SECONDARY = 3;

    public static final String INPUT_INGREDIENTS = "input";
    public static final String OUTPUT_INGREDIENTS = "output";

    public static final ProvidedUIFilter AIR = new ProvidedUIFilter(new VanillaUIFilter(), "AIR", "0");

    /**
     * Why is it so cumbersome to have a one line renaming method?
     *
     * @param itm  ItemStack.
     * @param name Name to give to your item.
     * @return The item, renamed.
     */
    @NotNull
    public static ItemStack rename(@NotNull ItemStack itm, @NotNull String name) {

        // Bruh
        ItemMeta iMeta = itm.getItemMeta();
        //noinspection ConstantConditions
        iMeta.setDisplayName(MythicLib.plugin.parseColors(name));
        itm.setItemMeta(iMeta);
        return itm;
    }

    /**
     * Why is it so cumbersome to have some lore lines method?
     *
     * @param itm   ItemStack.
     * @param lines Lore to add to the item
     * @return The item, lored.
     */
    @NotNull
    public static ItemStack addLore(@NotNull ItemStack itm, @NotNull ArrayList<String> lines) {

        // Not falling for that
        if (!itm.hasItemMeta()) {
            return itm;
        }

        // Bruh
        ItemMeta iMeta = itm.getItemMeta();
        if (iMeta == null) {
            return itm;
        }

        List<String> currentLore = iMeta.getLore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }

        // Add lore
        currentLore.addAll(lines);
        AdventureUtils.setLore(iMeta, currentLore);

        // That's it
        itm.setItemMeta(iMeta);
        return itm;
    }

    /**
     * Because obviously getConfigurationSection had to be annotated with {@link Nullable} even
     * through it does basically this same thing.
     *
     * @param root Root section
     * @param path Path to get/create
     * @return The subsection of this config.
     */
    @NotNull
    public static ConfigurationSection getSection(@NotNull ConfigurationSection root, @NotNull String path) {
        ConfigurationSection section = root.getConfigurationSection(path);
        if (section == null) {
            section = root.createSection(path);
        }
        return section;
    }
    //endregion

    //region ############------- Updating Legacy Formats -------############

    /**
     * In the past, crafting recipes only supported (for example) a 3x3 grid of input.
     * This was stored under [ID].base.crafting.shaped.recipe
     * <br> <br>
     * This method moves that into [ID].crafting.shaped.recipe.input
     */
    public void moveInput() {

        ConfigurationSection crafting = getSection(getEditedSection(), "crafting");
        ConfigurationSection shaped = getSection(crafting, getRecipeRegistry().getRecipeConfigPath());

        // Move to generalized method
        moveInput(shaped, recipeName);
    }

    /**
     * Absolute brute force method that will alter this configuration section,
     * note that code wont run the same after it has been called, as any variable
     * that is not a ConfigurationSection within 3 levels deep of the passed
     * section will be cleared.
     * <p>
     * For advanced debug purposes only.
     *
     * @param section Section to mess up
     */
    public static void tripleDebug(@NotNull ConfigurationSection section) {

        MMOItems.print(null, "\u00a7d-\u00a77 部分 \u00a75" + section.getCurrentPath(), null);
        for (String key : section.getKeys(false)) {
            MMOItems.print(null, "\u00a7d +\u00a77 " + key, null);

            MMOItems.print(null, "\u00a7d-\u00a7e-\u00a77 作为列表 \u00a75" + section.getCurrentPath() + "." + key + "\u00a77 {\u00a7d" + section.getStringList(key).size() + "\u00a77}", null);
            for (String listKey : section.getStringList(key)) {
                MMOItems.print(null, "\u00a7d +\u00a7e-\u00a77" + listKey, null);
            }

            ConfigurationSection asSection = getSection(section, key);
            MMOItems.print(null, "\u00a78--\u00a7d-\u00a77 部分 \u00a75" + asSection.getCurrentPath(), null);
            for (String asKey : asSection.getKeys(false)) {
                MMOItems.print(null, "\u00a78--\u00a7d +\u00a77 " + asKey, null);

                MMOItems.print(null, "\u00a78--\u00a7d-\u00a7e-\u00a77 作为列表 \u00a75" + asSection.getCurrentPath() + "." + asKey + "\u00a77 {\u00a7d" + asSection.getStringList(asKey).size() + "\u00a77}", null);
                for (String listKey : asSection.getStringList(asKey)) {
                    MMOItems.print(null, "\u00a78--\u00a7d +\u00a7e-\u00a77" + listKey, null);
                }

                ConfigurationSection asESection = getSection(asSection, asKey);
                MMOItems.print(null, "\u00a70--\u00a78--\u00a7d-\u00a77 部分 \u00a75" + asESection.getCurrentPath(), null);
                for (String asEKey : asESection.getKeys(false)) {
                    MMOItems.print(null, "\u00a70--\u00a78--\u00a7d +\u00a77 " + asEKey, null);

                    MMOItems.print(null, "\u00a70--\u00a78--\u00a7d-\u00a7e-\u00a77 作为列表 \u00a75" + asESection.getCurrentPath() + "." + asEKey + "\u00a77 {\u00a7d" + asESection.getStringList(asEKey).size() + "\u00a77}", null);
                    for (String listKey : asESection.getStringList(asEKey)) {
                        MMOItems.print(null, "\u00a70--\u00a78--\u00a7d +\u00a7e-\u00a77" + listKey, null);
                    }
                }
            }
        }
    }

    /**
     * In the past, crafting recipes only supported (for example) a 3x3 grid of input.
     * This was stored under [ID].base.crafting.shaped.recipe
     * <br> <br>
     * This method moves that into [ID].crafting.shaped.recipe.input
     *
     * @return The recipe name section result from this operation. <br>
     *         [ID].base.crafting.[TYPE].[NAME]
     */
    public static ConfigurationSection moveInput(@NotNull ConfigurationSection recipeSection, @NotNull String nameOfRecipe) {
        ConfigurationSection name;

        /*
         * This converts a recipe from the old format into the new.
         *
         * This is detected by the recipe name containing list information
         */

        if (recipeSection.isConfigurationSection(nameOfRecipe)) {
            //UPT//MMOItems.log("\u00a7a*\u00a77 Was config section");

            // Get as config section
            name = getSection(recipeSection, nameOfRecipe);

            // Both must exist for smithing conversion
            String item_yml = name.getString("input1");
            String ingot_yml = name.getString("input2");
            //UPT//MMOItems.log("\u00a7a*\u00a77 I1:\u00a76 " + item_yml + "\u00a77, I2:\u00a73 " + ingot_yml);

            // Is it smithing?
            if (item_yml != null && ingot_yml != null) {
                //UPT//MMOItems.log("\u00a7a*\u00a77 Identified as \u00a7aSmithing");

                // Build correctly
                name.set("input1", null);
                name.set("input2", null);
                name.set(INPUT_INGREDIENTS, poofFromLegacy(item_yml) + "|" + poofFromLegacy(ingot_yml));
                name.set(OUTPUT_INGREDIENTS, "v AIR 0|v AIR 0");
            }

        } else {
            //UPT//MMOItems.log("\u00a7a*\u00a77 No config section");

            // Get as String List
            List<String> sc = recipeSection.getStringList(nameOfRecipe);

            //UPT//MMOItems.log("\u00a78--\u00a7e-\u00a7d+\u00a77 Ingredients: \u00a75" + nameOfRecipe);
            //UPT//for (String key : sc) { MMOItems.log("\u00a78--\u00a7e-\u00a7d +\u00a77" + key); }

            // Clear
            recipeSection.set(nameOfRecipe, null);

            // Edit
            name = getSection(recipeSection, nameOfRecipe);
            name.set(INPUT_INGREDIENTS, sc);
        }

        // That's it
        return name;
    }

    /**
     * Converts legacy formats into ProvidedUIFilters
     *
     * @param legacy Legacy string
     * @return Converted string as best as possible
     */
    @NotNull
    public static String poofFromLegacy(@Nullable String legacy) {
        if (legacy == null || "[]".equals(legacy)) {
            //UPT//MMOItems.log("\u00a7b+\u00a77 Null, \u00a7b" + "v AIR - 1..");
            return "v AIR - 1..";
        }

        // Spaces are assumed to be updated
        if (legacy.contains(" ")) {
            //UPT//MMOItems.log("\u00a7b+\u00a77 Mirror, \u00a7b" + legacy);
            return legacy;
        }

        // Split by amount
        int aLoc = legacy.indexOf(':');
        QuickNumberRange amount = new QuickNumberRange(1.0, null);
        if (aLoc > 0) {

            String am = legacy.substring(aLoc + 1);
            legacy = legacy.substring(0, aLoc);
            Integer du = SilentNumbers.IntegerParse(am);
            if (du == null) {
                du = 1;
            }
            amount = new QuickNumberRange((double) du, null);
        }

        if (legacy.contains(".")) {

            // Must be MMOItem
            String[] mmo = legacy.split("\\.");
            //UPT//MMOItems.log("\u00a7b+\u00a77 MMOItem, \u00a7bm " + mmo[0] + " " + mmo[1] + " " + amount);

            // Build
            return "m " + mmo[0] + " " + mmo[1] + " " + amount;

        } else {
            //UPT//MMOItems.log("\u00a7b+\u00a77 Vanilla, \u00a7bv " + legacy + " - " + amount);

            // That's it
            return "v " + legacy + " - " + amount;
        }
    }

    /**
     * To support legacy formats, at least for now, we use this method
     * to read individual ingredients.
     * <p></p>
     * It supports the formats:
     * <p><code>MATERIAL</code> (legacy vanilla material)
     * </p><code>TYPE.ID</code> (legacy MMOItem)
     * <p><code>KEY ARGUMENT DATA AMOUNT</code> (current)
     * </p>
     *
     * @param str String that's should be in one of the formats above.
     * @param ffp To tell what happened
     * @return An ingredient read from this string.
     * @throws IllegalArgumentException If not in the correct format.
     */
    @NotNull
    public static ProvidedUIFilter readIngredientFrom(@NotNull String str, @NotNull FriendlyFeedbackProvider ffp) throws IllegalArgumentException {

        /*
         * This entry, is it a vanilla material?
         *
         * Then build it as material.
         */
        Material asMaterial = null;
        try {
            asMaterial = Material.valueOf(str.toUpperCase().replace(" ", "_").replace("-", "_"));
        } catch (IllegalArgumentException ignored) {
        }
        if (asMaterial != null) {

            // Is it AIR?
            if (asMaterial.isAir()) {

                ProvidedUIFilter result = new ProvidedUIFilter(VanillaUIFilter.get(), "AIR", "0");
                result.setAmountRange(new QuickNumberRange(null, null));
                return result;
            }

            // We snooze if its AIR or such
            if (!asMaterial.isItem()) {
                throw new IllegalArgumentException("无效材料 $u" + str + "$b ($f不是一个物品$b).");
            }

            // All right create filter and go
            ProvidedUIFilter poof = UIFilterManager.getUIFilter("v", asMaterial.toString(), "", "1..", ffp);

            // Valid?
            if (poof != null) {

                // Add
                return poof;

            } else {

                // Send all I guess
                ffp.sendTo(FriendlyFeedbackCategory.ERROR, MMOItems.getConsole());
                ffp.sendTo(FriendlyFeedbackCategory.FAILURE, MMOItems.getConsole());

                // Ew
                throw new IllegalArgumentException("无效材料 $u" + str);
            }
        }

        /*
         * Not a vanilla material, lets try to read it as a Legacy MMOItem thing.
         *
         * It must have a dot, and no spaces.
         */
        if (str.contains(".") && !str.contains(" ")) {

            // Split by dot
            String[] split = str.split("\\.");

            // Exactly two?
            if (split.length == 2) {

                // Well
                String iType = split[0];
                String iID = split[1];

                // All right create filter and go
                ProvidedUIFilter poof = UIFilterManager.getUIFilter("m", iType, iID, "1..", ffp);

                // Valid?
                if (poof != null) {

                    // Add
                    return poof;

                } else {

                    // Send all I guess
                    ffp.sendAllTo(MMOItems.getConsole());

                    // Ew
                    throw new IllegalArgumentException("无效材料 $u" + str);
                }
            }
        }

        /*
         * Not a vanilla Material, but what about a UIFilter itself?
         */
        ProvidedUIFilter poof = UIFilterManager.getUIFilter(str, ffp);

        // Valid?
        if (poof != null) {

            // Add
            return poof;

        } else {

            // Send all I guess
            ffp.sendAllTo(MMOItems.getConsole());

            // Ew
            throw new IllegalArgumentException("无效材料 $u" + str);
        }
    }
    //endregion
}
