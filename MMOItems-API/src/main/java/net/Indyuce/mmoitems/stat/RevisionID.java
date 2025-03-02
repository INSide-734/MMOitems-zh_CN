package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.RevisionInventory;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Regarding the auto updating of items
 * @see RevisionInventory
 * @see MMOItemReforger
 */
public class RevisionID extends ItemStat<NumericStatFormula, DoubleData> implements GemStoneStat {
	public RevisionID() {
		super("REVISION_ID", Material.ITEM_FRAME, "修改 ID", new String[] { "修改版 ID 用于确定项目是否是旧版", "每当对物品进行更改时", "都应该增加此值！", "(同装备高ID会自动更新低ID的属性效果等等)!", "", "§6更新程序很智能", "§6会对物品的基本属性进行更改,", "§6(例如)保持宝石完好无损 ."},
		        new String[0]);
	}

	@Override
	public NumericStatFormula whenInitialized(Object object) {
		if (object instanceof Integer)
			return new NumericStatFormula((Integer) object, 0, 0, 0);

		throw new IllegalArgumentException("必须指定一个整数");
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {
		item.addItemTag(getAppliedNBT(data));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull DoubleData data) {
		ArrayList<ItemTag> ret = new ArrayList<>();
		ret.add(new ItemTag(getNBTPath(), (int) data.getValue()));
		return ret;
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		new RevisionInventory(inv.getNavigator(), inv.getEdited()).open(inv);
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) { }

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
		ArrayList<ItemTag> tags = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			tags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.INTEGER));
		DoubleData data = getLoadedNBT(tags);
		if (data != null) { mmoitem.setData(this, data); }
	}

	@Nullable
	@Override
	public DoubleData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
		ItemTag tag = ItemTag.getTagAtPath(getNBTPath(), storedTags);
		if (tag != null) { return new DoubleData((int) tag.getValue()); }
		return null;
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<NumericStatFormula> statData) {
		if (statData.isPresent()) {
			NumericStatFormula data = (NumericStatFormula) statData.get();
			lore.add(ChatColor.GRAY + "当前修改 ID: " + ChatColor.GREEN + ((int) data.getBase()));
		} else
			lore.add(ChatColor.GRAY + "当前修改 ID: " + ChatColor.GREEN + "1");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击可增加该值");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击可减小该值");
	}

	@NotNull
	@Override
	public DoubleData getClearStatData() {
		return new DoubleData(0D);
	}
}
