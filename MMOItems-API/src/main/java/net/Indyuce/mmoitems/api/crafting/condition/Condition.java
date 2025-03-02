package net.Indyuce.mmoitems.api.crafting.condition;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.player.PlayerData;

public abstract class Condition {
	private final String id;

	/**
	 * Instanciated for every condition in a crafting recipe
	 * when loading a crafting station from the config file.
	 * 
	 * @param id The condition id
	 */
	public Condition(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public boolean hiddenFromLore() {
		return false;
	}

	public ConditionalDisplay getDisplay() {
		return MMOItems.plugin.getCrafting().getConditionInfo(id).getDisplay();
	}

	/**
	 * @param  data The player opening the crafting station
	 * @return      If the condition is met by the player
	 */
	public abstract boolean isMet(PlayerData data);

	/**
	 * Apply specific placeholders to display the condition in the item lore.
	 * 
	 * @param  string String with unparsed placeholders
	 * @return        String with parsed placeholders
	 */
	public abstract String formatDisplay(String string);

	/**
	 * Conditions like mana or stamina costs may behave like triggers ie they
	 * can perform an action if the recipe is used by the player. This method is
	 * called when the player crafts the item (not when he opens the station
	 * inventory)
	 * 
	 * @param data The player crafting the item
	 */
	public abstract void whenCrafting(PlayerData data);

	public CheckedCondition evaluateCondition(PlayerData data) {
		return new CheckedCondition(this, isMet(data));
	}
}
