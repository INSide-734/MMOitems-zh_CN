package net.Indyuce.mmoitems.api.edition;

import io.lumine.mythic.lib.gui.PluginInventory;

public interface Edition {

	/**
	 * Processes the player input.
	 * 
	 * @param  input Current player input
	 * @return       False if it should continue listening to player input
	 */
	boolean processInput(String input);

	/**
	 * @return The inventory used to edit some data, which also contains info
	 *         about the player currently editing
	 */
	PluginInventory getInventory();

	/**
	 * Called when edition is opened.
	 * 
	 * @param message Message which should be sent to the player
	 */
	void enable(String... message);

	/**
	 * @return If the previously opened GUI should be opened right after edition
	 *         ends or if it should be ignored
	 */
	boolean shouldGoBack();
}
