package net.Indyuce.mmoitems.api.crafting.ingredient;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.util.ui.QuickNumberRange;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.ConfigMMOItem;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.MMOItemPlayerIngredient;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.DisplayName;
import net.Indyuce.mmoitems.stat.data.MaterialData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class MMOItemIngredient extends Ingredient<MMOItemPlayerIngredient> {
	private final MMOItemTemplate template;

	@NotNull private final QuickNumberRange level;
	private final String display;

	public MMOItemIngredient(MMOLineConfig config) {
		super("mmoitem", config);

		// Which MMOItem?
		config.validate("type", "id");
		Type type = MMOItems.plugin.getTypes().getOrThrow(config.getString("type").toUpperCase().replace("-", "_").replace(" ", "_"));
		template = MMOItems.plugin.getTemplates().getTemplateOrThrow(type, config.getString("id"));

		// Read level, the default is that any level will work
		QuickNumberRange qnr = QuickNumberRange.getFromString(config.getString("level", ".."));
		if (qnr != null) { level = qnr; } else { level = new QuickNumberRange(null, null); }

		// Find the display name of the item
		display = config.contains("display") ? config.getString("display") : findName();
	}

	public MMOItemIngredient(ConfigMMOItem mmoitem) {
		super("mmoitem", mmoitem.getAmount());

		// Just the defaults
		template = mmoitem.getTemplate();
		level = new QuickNumberRange(null, null);
		display = findName();
	}

	public MMOItemTemplate getTemplate() {
		return template;
	}

	@Override
	public String formatDisplay(String s) {
		return s.replace("#item#", display).replace("#level#", (level.hasMax() || level.hasMax()) ? "lvl." + level.toString() + " " : "").replace("#amount#", String.valueOf(getAmount()));
	}

	@Override
	public boolean matches(MMOItemPlayerIngredient playerIngredient) {

		// Check for item type
		if (!playerIngredient.getType().equals(template.getType().getId()))
			return false;

		// Check for item id
		if (!playerIngredient.getId().equals(template.getId()))
			return false;

		// Checks for level range
		if (SilentNumbers.floor(level.getAsDouble(0)) != 0 && !level.inRange(playerIngredient.getUpgradeLevel()))
			return false;

		// Yuss
		return true;
	}

	@NotNull
	@Override
	public ItemStack generateItemStack(@NotNull RPGPlayer player, boolean forDisplay) {

		// Generate fresh from the template
		MMOItem mmo = template.newBuilder(player, forDisplay).build();

		// Build it for display, obviously
		ItemStack item = mmo.newBuilder().build(forDisplay);

		// Set level lma0
		int lvl = SilentNumbers.floor(level.getAsDouble(0));
		if (lvl != 0 && item.getItemMeta() != null) {

			ItemMeta iMeta = item.getItemMeta();
			iMeta.setDisplayName(MythicLib.plugin.parseColors(findName()));
			item.setItemMeta(iMeta); }

		// Set the amount
		item.setAmount(getAmount());

		// Lets go
		return item;
	}

    @Override
    public String toString() {
        return "MMOItemIngredient{" +
                "type=" + template.getType().getName() +
                ", id=" + template.getId()+
                ", level=" + level +
                '}';
    }

    private String findName() {
		String name;

		// By default, take item display name
		if (template.getBaseItemData().containsKey(ItemStats.NAME))
			name = template.getBaseItemData().get(ItemStats.NAME).toString().replace("<tier-color>", "").replace("<tier-name>", "").replace("<tier-color-cleaned>", "");

			// Try and take the material name
		else if (template.getBaseItemData().containsKey(ItemStats.MATERIAL))
			name = UtilityMethods.caseOnWords(((MaterialData) template.getBaseItemData().get(ItemStats.MATERIAL)).getMaterial().name().toLowerCase().replace("_", " "));

			// Ultra rare case to avoid a NPE
		else name = "Unrecognized Item";

		// Append upgrade level
		if (SilentNumbers.floor(level.getAsDouble(0)) != 0)
			return DisplayName.appendUpgradeLevel(name, SilentNumbers.floor(level.getAsDouble(0)));
		return name;
	}
}
