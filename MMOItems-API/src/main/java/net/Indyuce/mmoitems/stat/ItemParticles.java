package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.gson.JsonParser;
import io.lumine.mythic.lib.gson.JsonSyntaxException;
import io.lumine.mythic.lib.version.VParticle;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.ParticlesEdition;
import net.Indyuce.mmoitems.particle.api.ParticleType;
import net.Indyuce.mmoitems.stat.data.ParticleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemParticles extends ItemStat<ParticleData, ParticleData> {
	public ItemParticles() {
		super("ITEM_PARTICLES", Material.PINK_STAINED_GLASS, "物品粒子", new String[] { "持有/佩戴物品时显示的粒子", "", ChatColor.BLUE + "wiki 上提供了教程" }, new String[] { "all", "!block" });
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			inv.getEditedSection().set(getPath(), null);
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "已成功删除 " + getName() + ChatColor.GRAY + ".");
			return;
		}

		new ParticlesEdition(inv.getNavigator(), inv.getEdited()).open(inv);
	}

	@Override
	public ParticleData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "必须指定配置部分");
		return new ParticleData((ConfigurationSection) object);
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull ParticleData data) {
		item.addItemTag(getAppliedNBT(data));
	}

	@NotNull
	@Override
	public ArrayList<ItemTag> getAppliedNBT(@NotNull ParticleData data) {

		// Ret
		ArrayList<ItemTag> ret = new ArrayList<>();

		// Yes
		ret.add(new ItemTag(getNBTPath(), ((ParticleData) data).toJson().toString()));

		return ret;
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<ParticleData> statData) {
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击以设置项目粒子");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击清除");
	}

	@NotNull
	@Override
	public ParticleData getClearStatData() { return new ParticleData(ParticleType.AURA, VParticle.LARGE_EXPLOSION.get()); }

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String edited = (String) info[0];

		String format = message.toUpperCase().replace("-", "_").replace(" ", "_");
		if (edited.equals("particle-type")) {
			ParticleType particleType = ParticleType.valueOf(format);

			inv.getEditedSection().set("item-particles.type", particleType.name());
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "粒子类型成功设置为 " + ChatColor.GOLD
					+ particleType.getDefaultName() + ChatColor.GRAY + ".");
			return;
		}

		if (edited.equals("particle-color")) {
			String[] split = message.split(" ");
			int red = Integer.parseInt(split[0]), green = Integer.parseInt(split[1]), blue = Integer.parseInt(split[2]);

			inv.getEditedSection().set("item-particles.color.red", red);
			inv.getEditedSection().set("item-particles.color.green", green);
			inv.getEditedSection().set("item-particles.color.blue", blue);
			inv.registerTemplateEdition();
			inv.getPlayer()
					.sendMessage(MMOItems.plugin.getPrefix() + "粒子颜色成功设置为 " + ChatColor.RED + ChatColor.BOLD + red
							+ ChatColor.GRAY + " - " + ChatColor.GREEN + ChatColor.BOLD + green + ChatColor.GRAY + " - " + ChatColor.BLUE
							+ ChatColor.BOLD + blue + ChatColor.GRAY + ".");
			return;
		}

		if (edited.equals("particle")) {
			Particle particle = Particle.valueOf(format);

			inv.getEditedSection().set("item-particles.particle", particle.name());
			inv.registerTemplateEdition();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "粒子成功设置为 " + ChatColor.GOLD
					+ UtilityMethods.caseOnWords(particle.name().toLowerCase().replace("_", " ")) + ChatColor.GRAY + ".");
			return;
		}

		double value = MMOUtils.parseDouble(message);

		inv.getEditedSection().set("item-particles." + edited, value);
		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + UtilityMethods.caseOnWords(edited.replace("-", " ")) + ChatColor.GRAY
				+ " 设置为 " + ChatColor.GOLD + value + ChatColor.GRAY + ".");
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Fetch the tags
		ArrayList<ItemTag> tags = new ArrayList<>();
		if (mmoitem.getNBT().hasTag(getNBTPath()))
			tags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

		// Generate
		StatData data = getLoadedNBT(tags);

		// Valid?
		if (data != null) { mmoitem.setData(this, data); }
	}

	@Nullable
	@Override
	public ParticleData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

		ItemTag tagg = ItemTag.getTagAtPath(getNBTPath(), storedTags);
		if (tagg != null) {
			try {
				return new ParticleData(new JsonParser().parse((String) tagg.getValue()).getAsJsonObject());

			} catch (JsonSyntaxException|IllegalStateException exception) {

				/*
				 * OLD ITEM WHICH MUST BE UPDATED.
				 */
			}
		}

		return null;
	}
}
