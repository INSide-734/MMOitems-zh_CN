package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.lumine.mythic.lib.api.item.ItemTag;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ColorData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DyeColor extends ItemStat<ColorData, ColorData> {
	public DyeColor() {
		super("DYE_COLOR", VersionMaterial.RED_DYE.toMaterial(), "染料颜色",
				new String[] { "您物品的颜色 (对于可染色物品 ) ,RGB 模式" }, new String[0], Material.LEATHER_HELMET,
				Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, VersionMaterial.LEATHER_HORSE_ARMOR.toMaterial());
	}

	@Override
	public ColorData whenInitialized(Object object) {
		Validate.isTrue(object instanceof String, "必须指定一个字符串");
		return new ColorData((String) object);
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStats.DYE_COLOR).enable("在聊天中输入您想要的 RGB 颜色",
					ChatColor.AQUA + "格式: {Red} {Green} {Blue}");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			inv.getEditedSection().set("dye-color", null);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "成功删除染料颜色");
		}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String[] split = message.split(" ");
		Validate.isTrue(split.length == 3, "格式: {Red} {Green} {Blue}.");
		for (String str : split) {
			int k = Integer.parseInt(str);
			Validate.isTrue(k >= 0 && k < 256, "颜色必须介于 0 到 255 之间");
		}

		inv.getEditedSection().set("dye-color", message);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "染料颜色成功更改为 " + message);
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<ColorData> statData) {
		lore.add(ChatColor.GRAY + "当前值: " + (statData.isPresent() ? ChatColor.GREEN + statData.get().toString() : ChatColor.RED + "None"));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击进行选择");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击以删除染料颜色");
	}

	@NotNull
	@Override
	public ColorData getClearStatData() {
		return new ColorData(0, 0, 0);
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Actually ignore the NBT item altogether, we're looking at its colour this time
		ItemMeta iMeta = mmoitem.getNBT().getItem().getItemMeta();

		if (iMeta instanceof LeatherArmorMeta) {

			// Make a tag with thay colour property
			ArrayList<ItemTag> relevantTags = new ArrayList<>();

			// Kreate and Add
			relevantTags.add(new ItemTag(getNBTPath(), ((LeatherArmorMeta) iMeta).getColor()));

			// Cook
			StatData data = getLoadedNBT(relevantTags);

			// Put if nonull
			if (data != null) { mmoitem.setData(this, data); }
		}
	}

	/**
	 * For this specific stat, the NBT is not saved as a custom item, but as the color itself of the dyed item.
	 * <p></p>
	 * Just pass into here a unique ItemTag of path {@link #getNBTPath()} and of value {@link org.bukkit.Color},
	 * which is unique because
	 */
	@Nullable
	@Override
	public ColorData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		// Find tag
		ItemTag dyedColour = ItemTag.getTagAtPath(getNBTPath(), storedTags);

		// Found?
		if (dyedColour != null) {

			// Get that colour
			Color c = (Color) dyedColour.getValue();

			// Make and return thay colour data
			return new ColorData(c);
		}

		return null;
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull ColorData data) {

		// Only does anything if it is a colourable meta
		if (item.getMeta() instanceof LeatherArmorMeta) {

			// Just set the colour
			((LeatherArmorMeta) item.getMeta()).setColor(((ColorData) data).getColor());
		}
	}

	/**
	 * For this specific stat, the StatData is not saved as a custom NBT tag, but as the color itself of the dyed item.
	 * <p></p>
	 * Alas, this array <u>will</u> be empty. Check the colour of the item as a {@link LeatherArmorMeta} to read the value.
	 */
	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull ColorData data) {

		// No tags are added
		return new ArrayList<>();
	}
}
