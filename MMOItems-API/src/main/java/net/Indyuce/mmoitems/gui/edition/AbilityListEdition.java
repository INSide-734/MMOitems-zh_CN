package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.skill.RegisteredSkill;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class AbilityListEdition extends EditionInventory {
	private static final int[] slots = { 19, 20, 21, 22, 23, 24, 25 };
	private static final NamespacedKey CONFIG_KEY = new NamespacedKey(MMOItems.plugin, "ConfigKey");

	public AbilityListEdition(Navigator navigator, MMOItemTemplate template) {
		super(navigator, template);
	}

	@Override
	public String getName() {
		return "技能列表";
	}

	@Override
	public void arrangeInventory() {
		int n = 0;

		if (getEditedSection().contains("ability"))
			for (String key : getEditedSection().getConfigurationSection("ability").getKeys(false)) {
				String abilityFormat = getEditedSection().getString("ability." + key + ".type");
				RegisteredSkill ability = abilityFormat != null
						&& MMOItems.plugin.getSkills().hasSkill(abilityFormat = abilityFormat.toUpperCase().replace(" ", "_").replace("-", "_"))
								? MMOItems.plugin.getSkills().getSkill(abilityFormat)
								: null;

                TriggerType castMode;
                try {
                    castMode = TriggerType.valueOf(UtilityMethods.enumName(getEditedSection().getString("ability." + key + ".mode")));
                } catch (RuntimeException exception) {
                    castMode = null;
                }

				final ItemStack abilityItem = new ItemStack(Material.BLAZE_POWDER);
				ItemMeta abilityItemMeta = abilityItem.getItemMeta();
				abilityItemMeta.setDisplayName(ability != null ? ChatColor.GREEN + ability.getName() : ChatColor.RED + "! 没有选择技能 !");
				List<String> abilityItemLore = new ArrayList<>();
				abilityItemLore.add("");
				abilityItemLore.add(
						ChatColor.GRAY + "投射模式: " + (castMode != null ? ChatColor.GOLD + castMode.getName() : ChatColor.RED + "未选择"));
				abilityItemLore.add("");

				boolean check = false;
				if (ability != null)
					for (String modifier : getEditedSection().getConfigurationSection("ability." + key).getKeys(false))
						if (!modifier.equals("type") && !modifier.equals("mode") && ability.getHandler().getModifiers().contains(modifier))
							try {
								abilityItemLore.add(
										ChatColor.GRAY + "* " + UtilityMethods.caseOnWords(modifier.toLowerCase().replace("-", " ")) + ": " + ChatColor.GOLD
												+ new NumericStatFormula(getEditedSection().get("ability." + key + "." + modifier)).toString());
								check = true;
							} catch (IllegalArgumentException exception) {
								abilityItemLore.add(ChatColor.GRAY + "* " + UtilityMethods.caseOnWords(modifier.toLowerCase().replace("-", " ")) + ": "
										+ ChatColor.GOLD + "无法读取技能");
							}
				if (check)
					abilityItemLore.add("");

				abilityItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击进行编辑");
				abilityItemLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击即可删除");
				abilityItemMeta.setLore(abilityItemLore);
				abilityItemMeta.getPersistentDataContainer().set(CONFIG_KEY, PersistentDataType.STRING, key);
				abilityItem.setItemMeta(abilityItemMeta);

				inventory.setItem(slots[n++], abilityItem);
			}

		ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta glassMeta = glass.getItemMeta();
		glassMeta.setDisplayName(ChatColor.RED + "- 无技能 -");
		glass.setItemMeta(glassMeta);

		ItemStack add = new ItemStack(Material.WRITABLE_BOOK);
		ItemMeta addMeta = add.getItemMeta();
		addMeta.setDisplayName(ChatColor.GREEN + "添加技能...");
		add.setItemMeta(addMeta);

		inventory.setItem(40, add);
		while (n < slots.length)
			inventory.setItem(slots[n++], glass);
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
			return;

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "添加技能...")) {
			if (!getEditedSection().contains("ability")) {
				getEditedSection().createSection("ability.ability1");
				registerTemplateEdition();
				return;
			}

			if (getEditedSection().getConfigurationSection("ability").getKeys(false).size() > 6) {
				player.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "您已达到每件物品 7 个技能的限制");
				return;
			}

			for (int j = 1; j < 8; j++)
				if (!getEditedSection().getConfigurationSection("ability").contains("ability" + j)) {

                    // (Fixes MMOItems#1575) Initialize sample ability to avoid console logs
                    final String tag = "ability" + j;
                    getEditedSection().set("ability." + tag + ".type", "FIREBOLT");
                    getEditedSection().set("ability." + tag + ".mode", "RIGHT_CLICK");
                    registerTemplateEdition();
					return;
				}
		}

		final String tag = item.getItemMeta().getPersistentDataContainer().get(CONFIG_KEY, PersistentDataType.STRING);
		if (tag == null || tag.equals("")) return;

		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new AbilityEdition(navigator, template, tag).open(this);

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (getEditedSection().contains("ability") && getEditedSection().getConfigurationSection("ability").contains(tag)) {
				getEditedSection().set("ability." + tag, null);
				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + "成功移除 " + ChatColor.GOLD + tag + ChatColor.DARK_GRAY
						+ " (内部 ID)" + ChatColor.GRAY + ".");
			}
		}
	}
}