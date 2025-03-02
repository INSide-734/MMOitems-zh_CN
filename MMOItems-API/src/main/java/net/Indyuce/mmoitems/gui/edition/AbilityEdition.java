package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AbilityEdition extends EditionInventory {
	private final String configKey;

	private RegisteredSkill ability;

	private static final DecimalFormat MODIFIER_FORMAT = new DecimalFormat("0.###");
	private static final int[] slots = { 23, 24, 25, 32, 33, 34, 41, 42, 43, 50, 51, 52 };
	private static final NamespacedKey ABILITY_MOD_KEY = new NamespacedKey(MMOItems.plugin, "AbilityModifier");

	public AbilityEdition(Navigator navigator, MMOItemTemplate template, String configKey) {
		super(navigator, template);

		this.configKey = configKey;
	}

	@Override
	public String getName() {
		return "技能编辑";
	}

	@Override
	public void arrangeInventory() {
		int n = 0;

		String configString = getEditedSection().getString("ability." + configKey + ".type");
		String format = configString == null ? "" : configString.toUpperCase().replace(" ", "_").replace("-", "_").replaceAll("[^A-Z_]", "");
		ability = MMOItems.plugin.getSkills().hasSkill(format) ? MMOItems.plugin.getSkills().getSkill(format) : null;

		ItemStack abilityItem = new ItemStack(Material.BLAZE_POWDER);
		ItemMeta abilityItemMeta = abilityItem.getItemMeta();
		abilityItemMeta.setDisplayName(ChatColor.GREEN + "技能");
		List<String> abilityItemLore = new ArrayList<>();
		abilityItemLore.add(ChatColor.GRAY + "选择你的武器将绑定的技能");
		abilityItemLore.add("");
		abilityItemLore.add(
				ChatColor.GRAY + "当前值: " + (ability == null ? ChatColor.RED + "没有选择技能" : ChatColor.GOLD + ability.getName()));
		abilityItemLore.add("");
		abilityItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击选择");
		abilityItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击重置");
		abilityItemMeta.setLore(abilityItemLore);
		abilityItem.setItemMeta(abilityItemMeta);

		if (ability != null) {
			String castModeConfigString = getEditedSection().getString("ability." + configKey + ".mode");
			String castModeFormat = castModeConfigString == null ? ""
					: castModeConfigString.toUpperCase().replace(" ", "_").replace("-", "_").replaceAll("[^A-Z0-9_]", "");
			TriggerType castMode;
			try {
				castMode = TriggerType.valueOf(castModeFormat);
			} catch (RuntimeException exception) {
				castMode = null;
			}

			ItemStack castModeItem = new ItemStack(Material.ARMOR_STAND);
			ItemMeta castModeItemMeta = castModeItem.getItemMeta();
			castModeItemMeta.setDisplayName(ChatColor.GREEN + "触发器(具体看wiki)");
			List<String> castModeItemLore = new ArrayList<>();
			castModeItemLore.add(ChatColor.GRAY + "选择玩家需要执行什么操作");
			castModeItemLore.add(ChatColor.GRAY + "才能释放物品上的技能 (触发技能)");
			castModeItemLore.add("");
			castModeItemLore.add(ChatColor.GRAY + "当前值: "
					+ (castMode == null ? ChatColor.RED + "未选择触发器" : ChatColor.GOLD + castMode.getName()));
			castModeItemLore.add("");
			castModeItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击选择");
			castModeItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击重置");
			castModeItemMeta.setLore(castModeItemLore);
			castModeItem.setItemMeta(castModeItemMeta);

			inventory.setItem(30, castModeItem);
		}

		if (ability != null) {
			ConfigurationSection section = getEditedSection().getConfigurationSection("ability." + configKey);
			for (String modifier : ability.getHandler().getModifiers()) {
				final ItemStack modifierItem = new ItemStack(Material.GRAY_DYE);
				ItemMeta modifierItemMeta = modifierItem.getItemMeta();
				modifierItemMeta.setDisplayName(ChatColor.GREEN + UtilityMethods.caseOnWords(modifier.toLowerCase().replace("-", " ")));
				List<String> modifierItemLore = new ArrayList<>();
				modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "这是一个技能修饰符更改此值");
				modifierItemLore.add("" + ChatColor.GRAY + ChatColor.ITALIC + "可自定义技能的伤害冷却等等.");
				modifierItemLore.add("");

				try {
					modifierItemLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GOLD
							+ (section.contains(modifier) ? new NumericStatFormula(section.get(modifier)).toString()
									: MODIFIER_FORMAT.format(ability.getDefaultModifier(modifier))));
				} catch (IllegalArgumentException exception) {
					modifierItemLore.add(ChatColor.GRAY + "无法读取, 使用默认值");
				}

				modifierItemLore.add(ChatColor.GRAY + "默认值: " + ChatColor.GOLD + MODIFIER_FORMAT.format(ability.getDefaultModifier(modifier)));
				modifierItemLore.add("");
				modifierItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击选择");
				modifierItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击重置");
				modifierItemMeta.setLore(modifierItemLore);
				modifierItemMeta.getPersistentDataContainer().set(ABILITY_MOD_KEY, PersistentDataType.STRING, modifier);
				modifierItem.setItemMeta(modifierItemMeta);

				inventory.setItem(slots[n++], modifierItem);
			}
		}

		ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta glassMeta = glass.getItemMeta();
		glassMeta.setDisplayName(ChatColor.RED + "- 无修饰符 -");
		glass.setItemMeta(glassMeta);

		while (n < slots.length)
			inventory.setItem(slots[n++], glass);

		addEditionItems();
		inventory.setItem(28, abilityItem);
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
			return;

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.rightArrow + " 返回")) {
			new AbilityListEdition(getNavigator(), template).open(this);
			return;
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "技能")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.ABILITIES, configKey, "ability").enable("在聊天栏中输入您想要的技能名称.",
						"您可以通过输入该指令插件技能列表 " + ChatColor.AQUA + "/mi list ability");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				if (getEditedSection().contains("ability." + configKey + ".type")) {
					getEditedSection().set("ability." + configKey, null);

					if (getEditedSection().contains("ability") && getEditedSection().getConfigurationSection("ability").getKeys(false).size() == 0)
						getEditedSection().set("ability", null);

					registerTemplateEdition();
					player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置技能.");
				}
			}
			return;
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "触发器(具体看wiki)")) {
			if (event.getAction() == InventoryAction.PICKUP_ALL) {
				new StatEdition(this, ItemStats.ABILITIES, configKey, "mode").enable();

				player.sendMessage("");
				player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "可用触发器");
				for (TriggerType castMode : TriggerType.values())
					player.sendMessage("* " + ChatColor.GREEN + castMode.name());
			}

			if (event.getAction() == InventoryAction.PICKUP_HALF && getEditedSection().contains("ability." + configKey + ".mode")) {
				getEditedSection().set("ability." + configKey + ".mode", null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "成功重置技能触发器.");
			}
			return;
		}

		final String tag = item.getItemMeta().getPersistentDataContainer().get(ABILITY_MOD_KEY, PersistentDataType.STRING);
		if (tag == null || tag.equals("")) return;

		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(this, ItemStats.ABILITIES, configKey, tag).enable("在聊天栏中输入您想要的值.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (getEditedSection().contains("ability." + configKey + "." + tag)) {
				getEditedSection().set("ability." + configKey + "." + tag, null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "重置成功" + ChatColor.GOLD + UtilityMethods.caseOnWords(tag.replace("-", " "))
						+ ChatColor.GRAY + ".");
			}
		}
	}
}