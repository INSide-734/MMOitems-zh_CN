package net.Indyuce.mmoitems.api.crafting;

import io.lumine.mythic.lib.gui.editable.EditableInventory;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.item.InventoryItem;
import io.lumine.mythic.lib.gui.editable.item.SimpleItem;
import io.lumine.mythic.lib.gui.editable.item.builtin.GoBackItem;
import net.Indyuce.mmoitems.api.crafting.ingredient.CheckedIngredient;
import net.Indyuce.mmoitems.api.crafting.recipe.CheckedRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.crafting.recipe.UpgradingRecipe;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class EditableCraftingStationPreview extends EditableInventory {
    public EditableCraftingStationPreview(CraftingStation station) {
        super("preview-crafting-station-" + station.getId());
    }

    @NotNull
    public Generated generate(EditableCraftingStationView.Generated previousInventory, CheckedRecipe recipe, ItemStack recipePreviewItem) {
        return new Generated(previousInventory, recipe, recipePreviewItem);
    }

    private final Map<String, Function<ConfigurationSection, InventoryItem<?>>> ITEM_REGISTRY = Map.of(
            "back", GoBackItem::new,
            "confirm", ConfirmItem::new,
            "ingredient", IngredientItem::new,
            "preview_output", PreviewOutputItem::new,
            "recipe", RecipeItem::new
    );

    @Nullable
    @Override
    public InventoryItem<?> resolveItem(@NotNull String function, @NotNull ConfigurationSection config) {
        Function<ConfigurationSection, InventoryItem<?>> found = ITEM_REGISTRY.get(function);
        return found == null ? null : found.apply(config);
    }

    public class RecipeItem extends InventoryItem<Generated> {
        private final Material material;
        private final int stripLoreLines;

        public RecipeItem(ConfigurationSection config) {
            super(config);

            material = config.contains("item") ? MMOUtils.friendlyValueOf(Material::valueOf, config.getString("item"), "Could not find material with ID '%s'") : null;
            stripLoreLines = config.getInt("remove_lore_lines");
        }

        @Override
        public ItemStack getDisplayedItem(@NotNull Generated inv, int n) {
            ItemStack bookStack = inv.recipePreviewItem.clone();

            // Replace material
            if (material != null) bookStack.setType(material);
            bookStack.setAmount(1);

            ItemMeta meta = bookStack.getItemMeta();
            for (Enchantment ench : meta.getEnchants().keySet())
                meta.removeEnchant(ench);

            // Remove lore lines
            if (stripLoreLines > 0)
                meta.setLore(meta.getLore().subList(0, Math.max(meta.getLore().size() - stripLoreLines, 0)));
            bookStack.setItemMeta(meta);

            return bookStack;
        }
    }

    public class PreviewOutputItem extends InventoryItem<Generated> {
        public PreviewOutputItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public ItemStack getDisplayedItem(@NotNull Generated inv, int n) {

            if (inv.recipe.getRecipe() instanceof CraftingRecipe) {
                ItemStack item = ((CraftingRecipe) inv.recipe.getRecipe()).getPreviewItemStack();
                item.setAmount(((CraftingRecipe) inv.recipe.getRecipe()).getOutputAmount());
                return item;
            }

            if (inv.recipe.getRecipe() instanceof UpgradingRecipe) {
                final ItemStack item = ((UpgradingRecipe) inv.recipe.getRecipe()).getItem().getPreview();
                final ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(item.getItemMeta().getDisplayName() + ChatColor.GREEN + "+1!");
                item.setItemMeta(itemMeta);
                return item;
            }

            throw new RuntimeException("Unknown recipe type");
        }
    }

    public class ConfirmItem extends SimpleItem<Generated> {
        public ConfirmItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public void onClick(@NotNull Generated inv, @NotNull InventoryClickEvent inventoryClickEvent) {

            // First re-update the player's inventory
            // to avoid duplication glitches
            inv.updateData();

            inv.previousInventory.processRecipe(inv.recipe); // Confirm recipe processing
            inv.getNavigator().popOpen(); // Go back to previous inventory
        }
    }

    public class IngredientItem extends SimpleItem<Generated> {
        // TODO add "no ingredient" item
        public IngredientItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Nullable
        @Override
        public ItemStack getDisplayedItem(Generated inv, int n) {
            return n < inv.ingredients.size() ? inv.ingredients.get(n) : null;
        }
    }

    public class Generated extends GeneratedInventory {
        private final EditableCraftingStationView.Generated previousInventory;
        private final PlayerData playerData;
        private final ItemStack recipePreviewItem;

        private final List<ItemStack> ingredients = new ArrayList<>();

        private CheckedRecipe recipe;

        public Generated(EditableCraftingStationView.Generated previousInventory, CheckedRecipe recipe, ItemStack recipePreviewItem) {
            super(previousInventory.getNavigator(), EditableCraftingStationPreview.this);

            this.recipePreviewItem = recipePreviewItem;
            this.playerData = previousInventory.getPlayerData();
            this.previousInventory = previousInventory;
            this.recipe = recipe;

            updateIngredients();
        }

        public void updateData() {
            previousInventory.updateData();

            recipe = Objects.requireNonNull(previousInventory.getRecipe(recipe.getRecipe().getId()), "Could not match old recipe");
        }

        public void updateIngredients() {

            // Include each ingredient
            for (CheckedIngredient ing : recipe.getIngredients()) {

                // Generate new item for display
                ItemStack sample = ing.getIngredient().generateItemStack(playerData.getRPG(), true);
                sample.setAmount(64);

                // Time to calculate the stacks and put through the crafting station space.
                int amount = ing.getIngredient().getAmount();

                // Add what must be added
                while (amount > 0) {
                    if (amount > 64) {
                        ingredients.add(sample.clone()); // Put whole stack
                        amount -= 64; // Subtract
                    } else {
                        sample.setAmount(amount); // Add remaining
                        ingredients.add(sample.clone());
                        amount -= amount;
                    }
                }
            }
        }
    }
}
