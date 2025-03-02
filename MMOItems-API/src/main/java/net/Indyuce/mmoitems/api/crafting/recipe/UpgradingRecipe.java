package net.Indyuce.mmoitems.api.crafting.recipe;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.Sounds;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import net.Indyuce.mmoitems.api.crafting.ingredient.CheckedIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.MMOItemIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.IngredientInventory;
import net.Indyuce.mmoitems.api.event.PlayerUseCraftingStationEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class UpgradingRecipe extends Recipe {
	private final ConfigMMOItem item;
	private final Ingredient ingredient;

	private static final Random random = new Random();

	public UpgradingRecipe(ConfigurationSection config) {
		super(config);

		// load item being upgraded.
		item = new ConfigMMOItem(config.getConfigurationSection("item"));
		ingredient = new MMOItemIngredient(item);
	}

	public ConfigMMOItem getItem() {
		return item;
	}

	@Override
	public boolean whenUsed(PlayerData data, IngredientInventory inv, CheckedRecipe castRecipe, CraftingStation station) {
		if (!data.isOnline())
			return false;

		CheckedUpgradingRecipe recipe = (CheckedUpgradingRecipe) castRecipe;
		PlayerUseCraftingStationEvent event = new PlayerUseCraftingStationEvent(data, station, recipe);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return false;

		// Update item
		recipe.getUpgradeData().upgrade(recipe.getMMOItem());
		recipe.getUpgraded().setItemMeta(recipe.getMMOItem().newBuilder().build().getItemMeta());

		Message.UPGRADE_SUCCESS.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(recipe.getUpgraded())).send(data.getPlayer());

		// Play sound
		if (station.getEditableView().upgradeSound != null && !hasOption(RecipeOption.SILENT_CRAFT)) {
			data.getPlayer().playSound(data.getPlayer(), station.getEditableView().upgradeSound, 1,1);
		}

		// Recipe used successfully
		return true;
	}

	@Override
	public boolean canUse(PlayerData data, IngredientInventory inv, CheckedRecipe uncastRecipe, CraftingStation station) {

		// Find the item which should be upgraded
		CheckedIngredient upgraded = inv.findMatching(ingredient);
		if (!upgraded.isHad()) {
			if (!data.isOnline())
				return false;

			Message.NOT_HAVE_ITEM_UPGRADE.format(ChatColor.RED).send(data.getPlayer());
			data.getPlayer().playSound(data.getPlayer().getLocation(), Sounds.ENTITY_VILLAGER_NO, 1, 2);
			return false;
		}

		// Finds the item that will be upgraded
		NBTItem firstItem = NBTItem.get(upgraded.getFound().stream().findFirst().get().getItem());

		// Checks if upgraded item DOES has an upgrade template
		CheckedUpgradingRecipe recipe = (CheckedUpgradingRecipe) uncastRecipe;
		if (!(recipe.mmoitem = new LiveMMOItem(firstItem)).hasData(ItemStats.UPGRADE))
			return false;

		// Checks for max upgrade level
		if (!(recipe.upgradeData = (UpgradeData) recipe.getMMOItem().getData(ItemStats.UPGRADE)).canLevelUp()) {
			if (!data.isOnline())
				return false;

			Message.MAX_UPGRADES_HIT.format(ChatColor.RED).send(data.getPlayer());
			data.getPlayer().playSound(data.getPlayer().getLocation(), Sounds.ENTITY_VILLAGER_NO, 1, 2);
			return false;
		}

		// Checks for failure
		if (random.nextDouble() > recipe.getUpgradeData().getSuccess()) {

			// Should the item be destroyed when failing to upgrade
			if (recipe.getUpgradeData().destroysOnFail())
				recipe.getUpgraded().setAmount(recipe.getUpgraded().getAmount() - 1);

			// Take away ingredients
			recipe.getIngredients().forEach(ingredient -> ingredient.takeAway());

			if (!data.isOnline())
				return false;

			Message.UPGRADE_FAIL_STATION.format(ChatColor.RED).send(data.getPlayer());
			data.getPlayer().playSound(data.getPlayer().getLocation(), Sounds.ENTITY_ITEM_BREAK, 1, 2);
			return false;
		}

		return true;
	}

	@Override
	public CheckedRecipe evaluateRecipe(PlayerData data, IngredientInventory inv) {
		return new CheckedUpgradingRecipe(this, data, inv);
	}

	/**
	 * Used to cache the LiveMMOItem instance and UpgradeData
	 * which take a little performance to calculate.
	 */
	public class CheckedUpgradingRecipe extends CheckedRecipe {
		private LiveMMOItem mmoitem;
		private UpgradeData upgradeData;

        public CheckedUpgradingRecipe(Recipe recipe, PlayerData data, IngredientInventory inv) {
            super(recipe, data, inv);

            // Have the upgraded item count as an ingredient
            if (ingredientsHad && !inv.findMatching(ingredient).isHad()) ingredientsHad = false;
        }

		public UpgradeData getUpgradeData() {
			return upgradeData;
		}

		public LiveMMOItem getMMOItem() {
			return mmoitem;
		}

		public ItemStack getUpgraded() {
			return mmoitem.getNBT().getItem();
		}
	}
}
