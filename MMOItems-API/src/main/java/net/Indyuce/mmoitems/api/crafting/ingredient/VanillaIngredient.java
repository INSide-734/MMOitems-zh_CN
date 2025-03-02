package net.Indyuce.mmoitems.api.crafting.ingredient;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.crafting.uifilters.VanillaUIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.UIFilterManager;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackMessage;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.VanillaPlayerIngredient;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VanillaIngredient extends Ingredient<VanillaPlayerIngredient> {
	@NotNull public ProvidedUIFilter getFilter() { return filter; }
	@NotNull final ProvidedUIFilter filter;

	@NotNull public Material getMaterial() { return material; }
	@NotNull final Material material;

	/**
	 * displayName is the itemMeta display name; display corresponds to how the
	 * ingredient displays in the crafting recipe GUI item lore
	 */
	@NotNull final String display;
	@Nullable final String displayName;

	/**
	 * Use vanilla stuff?
	 */
	boolean vanillaBackward = true;

	public VanillaIngredient(MMOLineConfig config) {
		super("vanilla", config);

		FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());

		// Validate type I guess
		config.validate("type");
		String itemFilter = config.getString("type", "");

		//VING//MMOItems.log("\u00a78VING\u00a73 RD\u00a77 Reading\u00a7a " + itemFilter);

		// UIFilter or Vanilla Backwards Compatible?
		if (itemFilter.contains(" ")) {
			vanillaBackward = false;
			//VING//MMOItems.log("\u00a78VING\u00a73 RD\u00a77 As Item Filter (yes)");

			// Which item
			ProvidedUIFilter sweetFilter = UIFilterManager.getUIFilter(itemFilter, ffp);
			if (sweetFilter == null) {

				// Throw message
				throw new IllegalArgumentException(SilentNumbers.collapseList(SilentNumbers.transcribeList(ffp.getFeedbackOf(FriendlyFeedbackCategory.ERROR), message -> { if (message instanceof FriendlyFeedbackMessage) { return ((FriendlyFeedbackMessage) message).forConsole(FFPMMOItems.get()); } return ""; }), ""));
			}

			// Accepted as not-null
			filter = sweetFilter;

			// Valid UIFilter?
			if (!filter.isValid(ffp)) {

				// Throw message
				throw new IllegalArgumentException(SilentNumbers.collapseList(SilentNumbers.transcribeList(ffp.getFeedbackOf(FriendlyFeedbackCategory.ERROR), message -> { if (message instanceof FriendlyFeedbackMessage) { return ((FriendlyFeedbackMessage) message).forConsole(FFPMMOItems.get()); } return ""; }), ""));
			}

			// Consistent amounts
			setAmount(filter.getAmount(getAmount()));
			filter.setAmount(getAmount());

			// Find the display name of the item
			display = config.getString("display", findName());
			material = Material.STONE;
			//VING//MMOItems.log("\u00a78VING\u00a73 RD\u00a77 Determined\u00a7e " + filter);

		} else {
			//VING//MMOItems.log("\u00a78VING\u00a73 RD\u00a77 As Material (eh)");

			// Parse material
			material = Material.valueOf(itemFilter.toUpperCase().replace("-", "_").replace(" ", "_"));

			// Filter
			filter = new ProvidedUIFilter(VanillaUIFilter.get(), material.toString(), "0");
			filter.setAmount(getAmount());

			// Display is the name of the material, or whatever specified in the config.
			display = config.getString("display", UtilityMethods.caseOnWords(material.toString().toLowerCase().replace("_", " ")));
			//VING//MMOItems.log("\u00a78VING\u00a73 RD\u00a77 Determined\u00a73 " + material.toString());

		}

		// Valid UIFilter?
		if (filter.getItemStack(null) == null) {

			// Throw message
			throw new IllegalArgumentException(SilentNumbers.collapseList(SilentNumbers.transcribeList(ffp.getFeedbackOf(FriendlyFeedbackCategory.ERROR), message -> { if (message instanceof FriendlyFeedbackMessage) { return ((FriendlyFeedbackMessage) message).forConsole(FFPMMOItems.get()); } return ""; }), ""));
		}

		// Display-Name, apparently
		displayName = config.contains("name") ? config.getString("name") : null;

	}

	@Override
	public String formatDisplay(String s) {
		return s.replace("#item#", display).replace("#amount#", String.valueOf(getAmount()));
	}

	@Override
	public boolean matches(VanillaPlayerIngredient ing) {
		//VING//MMOItems.log("\u00a78VING\u00a79 MCH\u00a77 Comparing given \u00a73 " + SilentNumbers.getItemName(ing.getSourceItem()) + " to expected\u00a79 " + filter);

		if (vanillaBackward) {
			// Check for material
			if (ing.getType() != material) {
				//VING//MMOItems.log("\u00a78VING\u00a79 MCH\u00a7c Not right material \u00a78(expected \u00a76" + material.toString() + "\u00a78)");
				return false; }

			//MMOItems.log("\u00a78VING\u00a79 MCH\u00a77 Display Name Ingredient:\u00a7a " + ing.getDisplayName());
			//MMOItems.log("\u00a78VING\u00a79 MCH\u00a77 Display Name Requested:\u00a7a " + displayName);
			//MMOItems.log("\u00a78VING\u00a79 MCH\u00a77 Display Name Check:\u00a7a " + (ing.getDisplayName() != null ? ing.getDisplayName().equals(displayName) : displayName == null));

			// Check for display name
			return ing.getDisplayName() != null ? ing.getDisplayName().equals(displayName) : displayName == null;

		} else {
			//VING//MMOItems.log("\u00a78VING\u00a79 MCH\u00a77 Poof Check \u00a73 " + filter + "\u00a77: \u00a7a " + (filter.matches(ing.getSourceItem(), true, null)));

			// Sweet PooF matching
			return filter.matches(ing.getSourceItem(), true, null);
		}
	}

	@Override
	public String toString() {
		return "VanillaIngredient{" +
				"displayName='" + displayName + '\'' +
				", material=" + material +
				'}';
	}

	@NotNull
	@Override
	public ItemStack generateItemStack(@NotNull RPGPlayer player, boolean forDisplay) {

		// Stack
		ItemStack stack = filter.getItemStack(null);
		stack.setAmount(getAmount());

		// Then rename (okay)
		if (displayName != null) {
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName(MythicLib.plugin.parseColors(displayName));
			stack.setItemMeta(meta);
		}

		// Return
		return stack;
	}

	/**
	 * @return The name of the item the ProvidedUIFilter encodes for.
	 */
	@NotNull String findName() { return SilentNumbers.getItemName(filter.getParent().getDisplayStack(filter.getArgument(), filter.getData(), null), false); }
}
