package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ArrowParticlesEdition extends EditionInventory {
	public ArrowParticlesEdition(Navigator navigator, MMOItemTemplate template) {
		super(navigator, template);
	}

	@Override
	public String getName() {
		return "箭头粒子: " + template.getId();
	}

	@Override
	public void arrangeInventory() {
		Particle particle = null;
		try {
			particle = Particle.valueOf(getEditedSection().getString("arrow-particles.particle"));
		} catch (Exception ignored) {}

		ItemStack particleItem = new ItemStack(Material.BLAZE_POWDER);
		ItemMeta particleItemMeta = particleItem.getItemMeta();
		particleItemMeta.setDisplayName(ChatColor.GREEN + "粒子");
		List<String> particleItemLore = new ArrayList<>();
		particleItemLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "显示在箭头周围的粒子");
		particleItemLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "当箭落地时消失");
		particleItemLore.add("");
		particleItemLore.add(ChatColor.GRAY + "当前值: " + (particle == null ? ChatColor.RED + "未选择任何粒子"
				: ChatColor.GOLD + UtilityMethods.caseOnWords(particle.name().toLowerCase().replace("_", " "))));
		particleItemLore.add("");
		particleItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击选择");
		particleItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击重置");
		particleItemMeta.setLore(particleItemLore);
		particleItem.setItemMeta(particleItemMeta);

		ItemStack amount = new ItemStack(Material.GRAY_DYE);
		ItemMeta amountMeta = amount.getItemMeta();
		amountMeta.setDisplayName(ChatColor.GREEN + "粒子数量");
		List<String> amountLore = new ArrayList<>();
		amountLore.add("");
		amountLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GOLD + getEditedSection().getInt("arrow-particles.amount"));
		amountLore.add("");
		amountLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击选择");
		amountLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击重置");
		amountMeta.setLore(amountLore);
		amount.setItemMeta(amountMeta);

		ItemStack offset = new ItemStack(Material.GRAY_DYE);
		ItemMeta offsetMeta = offset.getItemMeta();
		offsetMeta.setDisplayName(ChatColor.GREEN + "粒子偏移");
		List<String> offsetLore = new ArrayList<>();
		offsetLore.add("");
		offsetLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GOLD + getEditedSection().getDouble("arrow-particles.offset"));
		offsetLore.add("");
		offsetLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击选择");
		offsetLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击重置");
		offsetMeta.setLore(offsetLore);
		offset.setItemMeta(offsetMeta);

		if (particle != null) {
			ConfigurationSection section = getEditedSection().getConfigurationSection("arrow-particles");
			if (MMOUtils.isColorable(particle)) {
				int red = section.getInt("color.red");
				int green = section.getInt("color.green");
				int blue = section.getInt("color.blue");

				ItemStack speed = new ItemStack(Material.GRAY_DYE);
				ItemMeta speedMeta = speed.getItemMeta();
				speedMeta.setDisplayName(ChatColor.GREEN + "粒子颜色");
				List<String> speedLore = new ArrayList<>();
				speedLore.add("");
				speedLore.add(ChatColor.GRAY + "当前值 (R-G-B ) :");
				speedLore.add("" + ChatColor.RED + ChatColor.BOLD + red + ChatColor.GRAY + " - " + ChatColor.GREEN + ChatColor.BOLD + green
						+ ChatColor.GRAY + " - " + ChatColor.BLUE + ChatColor.BOLD + blue);
				speedLore.add("");
				speedLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击选择");
				speedLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击重置");
				speedMeta.setLore(speedLore);
				speed.setItemMeta(speedMeta);

				inventory.setItem(41, speed);
			} else {
				ItemStack colorItem = new ItemStack(Material.GRAY_DYE);
				ItemMeta colorItemMeta = colorItem.getItemMeta();
				colorItemMeta.setDisplayName(ChatColor.GREEN + "粒子速度");
				List<String> colorItemLore = new ArrayList<>();
				colorItemLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "粒子向随机方向");
				colorItemLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "飞行的速度.");
				colorItemLore.add("");
				colorItemLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GOLD + section.getDouble("speed"));
				colorItemLore.add("");
				colorItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击选择");
				colorItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击重置");
				colorItemMeta.setLore(colorItemLore);
				colorItem.setItemMeta(colorItemMeta);

				inventory.setItem(41, colorItem);
			}
		}

		inventory.setItem(30, particleItem);
		inventory.setItem(23, amount);
		inventory.setItem(32, offset);
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
			return;

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "粒子")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.ARROW_PARTICLES, "particle").enable("在聊天栏中输入您想要的粒子");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				if (getEditedSection().contains("arrow-particles.particle")) {
					getEditedSection().set("arrow-particles", null);
					registerTemplateEdition();
					player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置粒子");
				}
			}
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "粒子颜色")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.ARROW_PARTICLES, "color").enable("在聊天栏中输入您想要的 RGB 颜色",
						ChatColor.AQUA + "格式: [RED] [GREEN] [BLUE]");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				if (getEditedSection().contains("arrow-particles.color")) {
					getEditedSection().set("arrow-particles.color", null);
					registerTemplateEdition();
					player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置粒子颜色.");
				}
			}
		}

		for (String string : new String[] { "amount", "offset", "speed" })
			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + UtilityMethods.caseOnWords(string))) {
				if (event.getAction() == InventoryAction.PICKUP_ALL)
					new StatEdition(this, ItemStats.ARROW_PARTICLES, string).enable("在聊天栏中输入您想要的 " + string + " 数值.");

				if (event.getAction() == InventoryAction.PICKUP_HALF) {
					if (getEditedSection().contains("arrow-particles." + string)) {
						getEditedSection().set("arrow-particles." + string, null);
						registerTemplateEdition();
						player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置 " + string + ".");
					}
				}
			}
	}
}