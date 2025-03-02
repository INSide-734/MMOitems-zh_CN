package net.Indyuce.mmoitems.api.crafting;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.NextPageItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.PreviousPageItem;
import io.lumine.mythic.lib.util.AdventureUtils;
import io.lumine.mythic.lib.util.DelayFormat;
import io.lumine.mythic.lib.version.Sounds;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.CustomSound;
import net.Indyuce.mmoitems.api.crafting.condition.CheckedCondition;
import net.Indyuce.mmoitems.api.crafting.ingredient.CheckedIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.IngredientInventory;
import net.Indyuce.mmoitems.api.crafting.recipe.CheckedRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.Recipe;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradingRecipe;
import net.Indyuce.mmoitems.api.event.PlayerUseCraftingStationEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.comp.eco.MoneyCondition;
import net.Indyuce.mmoitems.listener.CustomSoundListener;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class EditableCraftingStationView extends EditableInventory {
    private final CraftingStation station;

    public Sound craftSound, upgradeSound, queueCancelSound, queueClaimSound, queueAddSound;

    private boolean rightClickPreview;

    public EditableCraftingStationView(CraftingStation station) {
        super("view-" + station.getId());

        this.station = station;
    }

    @NotNull
    public Generated generate(Player player) {
        return new Generated(new Navigator(player), PlayerData.get(player));
    }

    private final Map<String, Function<ConfigurationSection, InventoryItem<?>>> ITEM_REGISTRY = Map.of(
            "previous_page", PreviousPageItem::new,
            "next_page", NextPageItem::new,
            "previous_queue_item", PreviousInQueueItem::new,
            "next_queue_item", NextInQueueItem::new,
            "recipe", RecipeItem::new,
            "queued_item", CraftingQueueItem::new
    );

    @Override
    public void reload(@NotNull JavaPlugin plugin, @NotNull ConfigurationSection config) {
        super.reload(plugin, config);

        rightClickPreview = config.getBoolean("enable_right_click_preview");

        craftSound = findSound(config, "sound.craft");
        upgradeSound = findSound(config, "sound.upgrade");
        queueCancelSound = findSound(config, "sound.queue_cancel");
        queueClaimSound = findSound(config, "sound.queue_claim");
        queueAddSound = findSound(config, "sound.queue_add");
    }

    private static @Nullable Sound findSound(ConfigurationSection config, String key) {
        return config.contains(key) ? MMOUtils.friendlyValueOf(Sounds::fromName, config.getString(key), "No sound with name '%s'") : null;
    }

    @Nullable
    @Override
    public InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        Function<ConfigurationSection, InventoryItem<?>> found = ITEM_REGISTRY.get(function);
        return found == null ? null : found.apply(config);
    }

    public class NextInQueueItem extends SimpleItem<Generated> {
        public NextInQueueItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(@NotNull Generated inv) {
            CraftingStatus.CraftingQueue queue = inv.playerData.getCrafting().getQueue(station);
            return inv.queueOffset + inv.queueSlots < queue.getCrafts().size();
        }

        @Override
        public void onClick(@NotNull Generated generated, @NotNull InventoryClickEvent inventoryClickEvent) {
            generated.queueOffset++;
            generated.open();
        }
    }

    public class PreviousInQueueItem extends SimpleItem<Generated> {
        public PreviousInQueueItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(@NotNull Generated inv) {
            return inv.queueOffset > 0;
        }

        @Override
        public void onClick(@NotNull Generated generated, @NotNull InventoryClickEvent inventoryClickEvent) {
            generated.queueOffset--;
            generated.open();
        }
    }

    private static final NamespacedKey RECIPE_ID_KEY = new NamespacedKey(MMOItems.plugin, "recipe_id");

    public class UpgradingRecipeItem extends InventoryItem<Generated> {
        private final String name;
        private final List<String> _lore;
        private final DelayFormat delayFormat;

        public UpgradingRecipeItem(ConfigurationSection config, DelayFormat delayFormat) {
            super(config);

            this.delayFormat = delayFormat;
            name = config.getString("name");
            _lore = config.getStringList("lore");
        }

        @Override
        public ItemStack getDisplayedItem(@NotNull Generated inv, int i) {
            int recipeIndex = (inv.page - 1) * inv.recipeSlots + i;
            CheckedRecipe recipe = inv.recipes.get(recipeIndex);
            UpgradingRecipe upgradingRecipe = (UpgradingRecipe) recipe.getRecipe();

            List<String> newLore = preprocessAnyRecipeLore(recipe, upgradingRecipe, _lore, delayFormat);

            ItemStack item = upgradingRecipe.getItem().getPreview();
            ItemMeta meta = item.getItemMeta();
            AdventureUtils.setDisplayName(meta, this.name.replace("{name}", MMOUtils.getDisplayName(item)));
            AdventureUtils.setLore(meta, newLore);
            meta.addItemFlags(ItemFlag.values());
            meta.getPersistentDataContainer().set(RECIPE_ID_KEY, PersistentDataType.STRING, upgradingRecipe.getId());
            item.setItemMeta(meta);

            return item;
        }
    }

    private static List<String> preprocessAnyRecipeLore(CheckedRecipe recipe, Recipe registeredRecipe, List<String> _lore, DelayFormat delayFormat) {

        boolean ingredientsDisplayed = false,
                conditionsDisplayed = false,
                loreDisplayed = false;

        /*
         * Backwards pass is preferable for inserting lists while
         * preserving O(n) complexity
         */
        List<String> newLore = new ArrayList<>();
        for (int lineIndex = _lore.size() - 1; lineIndex >= 0; lineIndex--) {
            String currentLine = _lore.get(lineIndex);

            // Crafting time
            if (registeredRecipe instanceof CraftingRecipe && currentLine.startsWith("{crafting_time}")) {
                long craftingTime = ((CraftingRecipe) registeredRecipe).getCraftingTime();
                if (craftingTime <= 0) continue;
                currentLine = currentLine.substring(15).replace("{crafting_time}",
                        delayFormat.format(craftingTime));
            }

            // Conditions
            else if (currentLine.startsWith("{conditions}")) {
                if (recipe.getConditions().isEmpty()) continue;
                currentLine = currentLine.substring(12);

                if (!conditionsDisplayed) {
                    conditionsDisplayed = true;

                    for (ListIterator<CheckedCondition> it = MMOUtils.backwards(recipe.getConditions()); it.hasPrevious(); ) {
                        CheckedCondition condition = it.previous();
                        if (condition.getCondition().hiddenFromLore()) continue;
                        ConditionalDisplay display = condition.getCondition().getDisplay();
                        if (display != null) newLore.add(condition.format());
                    }
                }
            }

            // Recipe lore
            else if (currentLine.startsWith("{lore}")) {
                if (recipe.getRecipe().getLore().isEmpty()) continue;
                currentLine = currentLine.substring(6);

                if (!loreDisplayed) {
                    loreDisplayed = true;

                    MMOUtils.addAllBackwards(newLore, recipe.getRecipe().getLore());
                }
            }

            // Ingredients
            else if (currentLine.startsWith("{ingredients}")) {
                if (recipe.getIngredients().isEmpty()) continue;
                currentLine = currentLine.substring(13);

                if (!ingredientsDisplayed) {
                    ingredientsDisplayed = true;

                    for (ListIterator<CheckedIngredient> it = MMOUtils.backwards(recipe.getIngredients()); it.hasPrevious(); ) {
                        CheckedIngredient ingredient = it.previous();
                        newLore.add(ingredient.format());
                    }
                }
            }

            newLore.add(currentLine);
        }

        Collections.reverse(newLore);
        return newLore;
    }

    public class CraftingRecipeItem extends InventoryItem<Generated> {
        private final String name, nameMultiple;
        private final List<String> _lore;
        private final DelayFormat delayFormat;

        public CraftingRecipeItem(ConfigurationSection config, DelayFormat delayFormat) {
            super(config);

            this.delayFormat = delayFormat;
            name = config.getString("name");
            nameMultiple = config.getString("name_multiple");
            _lore = config.getStringList("lore");
        }

        @Override
        public ItemStack getDisplayedItem(@NotNull Generated inv, int i) {
            int recipeIndex = (inv.page - 1) * inv.recipeSlots + i;
            CheckedRecipe recipe = inv.recipes.get(recipeIndex);
            CraftingRecipe craftingRecipe = (CraftingRecipe) recipe.getRecipe();

            List<String> newLore = preprocessAnyRecipeLore(recipe, craftingRecipe, this._lore, delayFormat);

            ItemStack item = Objects.requireNonNull(craftingRecipe.getPreviewItemStack(), "Null recipe output");

            // Display amount
            int amount = craftingRecipe.getOutputAmount();
            item.setAmount(Math.min(64, amount));

            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.values());
            String rawNameFormat = amount > 1 ? nameMultiple.replace("{amount}", String.valueOf(amount)) : name; // Item name?
            AdventureUtils.setDisplayName(meta, rawNameFormat.replace("{name}", MMOUtils.getDisplayName(item)));
            AdventureUtils.setLore(meta, newLore);
            meta.getPersistentDataContainer().set(RECIPE_ID_KEY, PersistentDataType.STRING, craftingRecipe.getId());
            item.setItemMeta(meta);

            return item;
        }
    }

    public class RecipeItem extends InventoryItem<Generated> {
        private final SimpleItem<Generated> none;
        private final UpgradingRecipeItem upgradingRecipe;
        private final CraftingRecipeItem craftingRecipe;

        public RecipeItem(ConfigurationSection config) {
            super(config);

            DelayFormat delayFormat = new DelayFormat(Objects.requireNonNull(config.get("delay_format"), "Could not find delay format"));
            none = config.isConfigurationSection("none") ? new SimpleItem<>(config.getConfigurationSection("none")) : null;
            upgradingRecipe = new UpgradingRecipeItem(config.getConfigurationSection("upgrade"), delayFormat);
            craftingRecipe = new CraftingRecipeItem(config.getConfigurationSection("craft"), delayFormat);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(@NotNull Generated inv, int n) {
            int index = (inv.page - 1) * inv.recipeSlots + n;

            // No recipe at given index
            if (index >= inv.recipes.size()) {
                return none != null ? none.getDisplayedItem(inv, 0) : null;
            }

            CheckedRecipe recipe = inv.recipes.get(index);
            return (recipe.getRecipe() instanceof UpgradingRecipe ? upgradingRecipe : craftingRecipe).getDisplayedItem(inv, n);
        }

        @Override
        public void onClick(@NotNull Generated inv, @NotNull InventoryClickEvent event) {
            String tag = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(RECIPE_ID_KEY, PersistentDataType.STRING);
            if (tag == null || tag.isEmpty()) return;

            // First re-update the player's inventory
            // to avoid duplication glitches
            inv.updateData();

            final CheckedRecipe recipe = Objects.requireNonNull(inv.getRecipe(tag));
            if (rightClickPreview && event.isRightClick())
                station.getEditablePreview().generate(inv, recipe, event.getCurrentItem()).open();
            else {
                inv.processRecipe(recipe);
                inv.open();
            }
        }
    }

    private static final NamespacedKey QUEUE_ITEM_ID_KEY = new NamespacedKey(MMOItems.plugin, "queue_item");

    public class CraftingQueueItem extends InventoryItem<Generated> {
        private final SimpleItem<Generated> none;
        private final List<String> lore;
        private final String name;
        private final DelayFormat delayFormat;

        public CraftingQueueItem(ConfigurationSection config) {
            super(config);

            none = config.isConfigurationSection("none") ? new SimpleItem<>(config.getConfigurationSection("none")) : null;
            lore = config.getStringList("existing.lore");
            name = config.getString("existing.name");
            delayFormat = new DelayFormat(Objects.requireNonNull(config.getString("existing.delay_format"), "Could not find delay format"));
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack getDisplayedItem(@NotNull Generated inv, int i) {
            int index = inv.queueOffset + i;

            CraftingStatus.CraftingQueue queue = inv.playerData.getCrafting().getQueue(station);

            // No item at this index in queue
            if (index >= queue.getCrafts().size()) {
                return none != null ? none.getDisplayedItem(inv, 0) : null;
            }

            CraftingStatus.CraftingQueue.QueueItem queueItem = queue.getCrafts().get(index);

            List<String> newLore = new ArrayList<>();
            for (String currentLine : this.lore) {

                // Is in the queue?
                if (currentLine.startsWith("{queue}")) {
                    if (queueItem.isReady()) continue;
                    else currentLine = currentLine.substring(7);
                }

                // Is ready?
                else if (currentLine.startsWith("{ready}")) {
                    if (!queueItem.isReady()) continue;
                    else currentLine = currentLine.substring(7);
                }

                // Lore placeholders
                newLore.add(currentLine.replace("{time_left}", delayFormat.format(queueItem.getLeft())));
            }

            ItemStack item = queueItem.getRecipe().getPreviewItemStack();
            item.setAmount(index + 1);
            ItemMeta meta = item.getItemMeta();
            AdventureUtils.setDisplayName(meta, this.name.replace("{name}", meta.getDisplayName()));
            AdventureUtils.setLore(meta, newLore);
            meta.addItemFlags(ItemFlag.values());
            meta.getPersistentDataContainer().set(QUEUE_ITEM_ID_KEY, PersistentDataType.STRING, queueItem.getUniqueId().toString());
            item.setItemMeta(meta);

            return item;
        }

        @Override
        public void onClick(@NotNull Generated inv, @NotNull InventoryClickEvent event) {
            String tag = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(QUEUE_ITEM_ID_KEY, PersistentDataType.STRING);
            if (tag == null || tag.isEmpty()) return;

            CraftingStatus.CraftingQueue.QueueItem recipeInfo = inv.playerData.getCrafting().getQueue(station).getCraft(UUID.fromString(tag));
            CraftingRecipe recipe = recipeInfo.getRecipe();

            /*
             * If the crafting recipe is ready, give the player the output item
             * to the player and remove the recipe from the queue
             */
            if (recipeInfo.isReady()) {
                ItemStack result = recipe.hasOption(Recipe.RecipeOption.OUTPUT_ITEM) ? recipe.getOutputItemStack(inv.playerData.getRPG()) : null;

                PlayerUseCraftingStationEvent called = new PlayerUseCraftingStationEvent(inv.playerData, station, recipe, result);
                Bukkit.getPluginManager().callEvent(called);
                if (called.isCancelled())
                    return;

                // Remove from crafting queue
                inv.playerData.getCrafting().getQueue(station).remove(recipeInfo);
                recipe.whenClaimed().forEach(trigger -> trigger.whenCrafting(inv.playerData));

                // Play sounds
                CustomSoundListener.playSound(result, CustomSound.ON_CRAFT, inv.getPlayer());
                if (queueClaimSound != null && !recipe.hasOption(Recipe.RecipeOption.SILENT_CRAFT)) {
                    inv.getPlayer().playSound(inv.getPlayer().getLocation(), queueClaimSound, 1, 1);
                }

                if (result != null) new SmartGive(inv.getPlayer()).give(result);
            }

            /*
             * If the recipe is not ready, cancel the recipe and give the
             * ingredients back to the player
             */
            else {
                PlayerUseCraftingStationEvent called = new PlayerUseCraftingStationEvent(inv.playerData, station, recipe);
                Bukkit.getPluginManager().callEvent(called);
                if (called.isCancelled()) return;

                // Remove from crafting queue
                inv.playerData.getCrafting().getQueue(station).remove(recipeInfo);
                recipe.whenCanceled().forEach(trigger -> trigger.whenCrafting(inv.playerData));

                // Play sound
                if (queueCancelSound != null && !recipe.hasOption(Recipe.RecipeOption.SILENT_CRAFT)) {
                    inv.getPlayer().playSound(inv.getPlayer().getLocation(), queueCancelSound, 1, 1);
                }

                // Give ingredients back
                for (Ingredient ingredient : recipeInfo.getRecipe().getIngredients())
                    new SmartGive(inv.getPlayer()).give(ingredient.generateItemStack(inv.playerData.getRPG(), false));

                // Give money back
                // TODO shit code. improve modularity
                recipe.getConditions()
                        .stream()
                        .filter(condition -> condition instanceof MoneyCondition)
                        .map(condition -> (MoneyCondition) condition)
                        .forEach(condition -> MMOItems.plugin.getVault().getEconomy().depositPlayer(inv.getPlayer(), condition.getAmount()));
            }

            inv.updateData();
            inv.open();
        }
    }

    public class Generated extends GeneratedInventory {
        private final PlayerData playerData;
        private final int queueSlots, recipeSlots;

        private List<CheckedRecipe> recipes;
        private IngredientInventory ingredients;
        private int maxPage;

        private int queueOffset;

        public Generated(Navigator navigator, PlayerData playerData) {
            super(navigator, EditableCraftingStationView.this);

            this.playerData = playerData;
            this.queueSlots = Objects.requireNonNull(EditableCraftingStationView.this.getByFunction("queued_item"), "Missing item with function 'queued_item'").getSlots().size();
            this.recipeSlots = Objects.requireNonNull(EditableCraftingStationView.this.getByFunction("recipe"), "Missing item with function 'recipe'").getSlots().size();

            updateData();

            // Refresh crafting queue
            registerRepeatingTask(open -> {
                InventoryItem found = getByFunction("queued_item");
                if (found != null) displayItem(open, found);
            }, 20);
        }

        @Override
        public int getMaxPage() {
            return maxPage;
        }

        @NotNull
        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{page}", String.valueOf(page)).replace("{max_page}", String.valueOf(maxPage));
        }

        public PlayerData getPlayerData() {
            return playerData;
        }

        void updateData() {
            ingredients = new IngredientInventory(player);
            recipes = station.getAvailableRecipes(playerData, ingredients);
            maxPage = UtilityMethods.getPageNumber(recipes.size(), recipeSlots);
        }

        public void processRecipe(CheckedRecipe recipe) {
            if (!recipe.areConditionsMet()) {
                Message.CONDITIONS_NOT_MET.format(ChatColor.RED).send(player);
                player.playSound(player.getLocation(), Sounds.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }

            if (!recipe.allIngredientsHad()) {
                Message.NOT_ENOUGH_MATERIALS.format(ChatColor.RED).send(player);
                player.playSound(player.getLocation(), Sounds.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }

            if (!recipe.getRecipe().canUse(playerData, ingredients, recipe, station)) {
                updateData();
                return;
            }

            if (recipe.getRecipe().whenUsed(playerData, ingredients, recipe, station)) {
                recipe.getIngredients().forEach(ingredient -> ingredient.takeAway());
                recipe.getConditions().forEach(condition -> condition.getCondition().whenCrafting(playerData));
                recipe.getRecipe().whenUsed().forEach(trigger -> trigger.whenCrafting(playerData));

                updateData();
            }
        }

        @Nullable
        public CheckedRecipe getRecipe(@NotNull String id) {
            for (CheckedRecipe info : recipes)
                if (info.getRecipe().getId().equals(id))
                    return info;
            return null;
        }
    }
}



