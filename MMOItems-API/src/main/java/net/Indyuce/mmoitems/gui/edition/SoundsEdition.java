package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.CustomSound;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundsEdition extends EditionInventory {
	public static final Map<Integer, String> CORRESPONDING_SLOT = new HashMap<>();

	static {
		for (CustomSound sound : CustomSound.values())
			CORRESPONDING_SLOT.put(sound.getSlot(), sound.name().replace("_", "-").toLowerCase());
	}

	public SoundsEdition(Navigator navigator, MMOItemTemplate template) {
		super(navigator, template);
	}

	@Override
	public String getName() {
		return "自定义声音: " + template.getId();
	}

	@Override
	public void arrangeInventory() {
		for (CustomSound sound : CustomSound.values()) {
			ItemStack soundEvent = sound.getItem().clone();
			ItemMeta soundEventMeta = soundEvent.getItemMeta();
			soundEventMeta.addItemFlags(ItemFlag.values());
			soundEventMeta.setDisplayName(ChatColor.GREEN + sound.getName());
			List<String> eventLore = new ArrayList<>();
			for (String lore : sound.getLore())
				eventLore.add(ChatColor.GRAY + lore);
			eventLore.add("");
			String configSoundName = sound.getName().replace(" ", "-").toLowerCase();
			String value = getEditedSection().getString("sounds." + configSoundName + ".sound");
			if (value != null) {
				eventLore.add(ChatColor.GRAY + "当前值:");
				eventLore.add(ChatColor.GRAY + " - 声音名称: '" + ChatColor.GREEN
						+ getEditedSection().getString("sounds." + configSoundName + ".sound") + ChatColor.GRAY + "'");
				eventLore.add(
						ChatColor.GRAY + " - 音量: " + ChatColor.GREEN + getEditedSection().getDouble("sounds." + configSoundName + ".volume"));
				eventLore.add(ChatColor.GRAY + " - 音调: " + ChatColor.GREEN + getEditedSection().getDouble("sounds." + configSoundName + ".pitch"));
			} else
				eventLore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "无");
			eventLore.add("");
			eventLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击进行选择");
			eventLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击可删除该值");
			soundEventMeta.setLore(eventLore);
			soundEvent.setItemMeta(soundEventMeta);

			inventory.setItem(sound.getSlot(), soundEvent);
		}
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
			return;

		if (CORRESPONDING_SLOT.containsKey(event.getSlot())) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStats.CUSTOM_SOUNDS, CORRESPONDING_SLOT.get(event.getSlot())).enable("在聊天中输入您要添加的自定义声音名称",
						ChatColor.AQUA + "格式: [SOUND NAME] [VOLUME] [PITCH]",
						ChatColor.AQUA + "示例: entity.generic.drink 1 1");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				String soundPath = CORRESPONDING_SLOT.get(event.getSlot());
				getEditedSection().set("sounds." + soundPath, null);

				// clear sound config section
				if (getEditedSection().contains("sounds." + soundPath)
						&& getEditedSection().getConfigurationSection("sounds." + soundPath).getKeys(false).isEmpty()) {
					getEditedSection().set("sounds." + soundPath, null);
					if (getEditedSection().getConfigurationSection("sounds").getKeys(false).isEmpty())
						getEditedSection().set("sounds", null);
				}

				registerTemplateEdition();
				player.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + UtilityMethods.caseOnWords(soundPath.replace("-", " ")) + " 声音"
						+ ChatColor.GRAY + " 成功删除");
			}
		}
	}
}