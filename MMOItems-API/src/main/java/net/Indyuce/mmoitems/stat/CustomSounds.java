package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.Sounds;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.CustomSound;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.SoundsEdition;
import net.Indyuce.mmoitems.stat.data.SoundData;
import net.Indyuce.mmoitems.stat.data.SoundListData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.PlayerConsumable;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomSounds extends ItemStat<SoundListData, SoundListData> implements GemStoneStat, PlayerConsumable {
	public CustomSounds() {
		super("SOUNDS", Material.JUKEBOX, "自定义声音", new String[]{"您的物品将使用的自定义声音"},
		        new String[0]);
	}

	@Override
	public SoundListData whenInitialized(Object object) {
		Validate.isTrue(object instanceof ConfigurationSection, "必须指定配置部分");
		ConfigurationSection config = (ConfigurationSection) object;

		SoundListData sounds = new SoundListData();

		for (CustomSound sound : CustomSound.values()) {
			String path = sound.name().replace("_", "-").toLowerCase();
			if (config.contains(path))
				sounds.set(sound, new SoundData(config.get(path)));
		}

		return sounds;
	}

	@Override
	public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new SoundsEdition(inv.getNavigator(), inv.getEdited()).open(inv);

		if (event.getAction() == InventoryAction.PICKUP_HALF)
			if (inv.getEditedSection().contains("sounds")) {
				inv.getEditedSection().set("sounds", null);
				inv.registerTemplateEdition();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "自定义声音已成功删除");
			}
	}

	@Override
	public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
		String soundsPath = (String) info[0];
		String[] split = message.split(" ");
		Validate.isTrue(split.length == 3, message + " 不是有效的 [SOUND NAME] [VOLUME] [PITCH].");

		String soundName = split[0].replace("-", "_");
		double volume = MMOUtils.parseDouble(split[1]);
		double pitch = MMOUtils.parseDouble(split[2]);

		inv.getEditedSection().set("sounds." + soundsPath + ".sound", soundName);
		inv.getEditedSection().set("sounds." + soundsPath + ".volume", volume);
		inv.getEditedSection().set("sounds." + soundsPath + ".pitch", pitch);

		inv.registerTemplateEdition();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + UtilityMethods.caseOnWords(soundsPath.replace(".", " ")) + ChatColor.GRAY
				+ " 成功更改为 '" + soundName + "'.");
	}

	@Override
	public void whenDisplayed(List<String> lore, Optional<SoundListData> statData) {

		if (statData.isPresent()) {
			lore.add(ChatColor.GRAY + " 当前值: ");
			SoundListData data = (SoundListData) statData.get();
			data.mapData()
					.forEach((sound,
							soundData) -> lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN
									+ UtilityMethods.caseOnWords(sound.getName().toLowerCase().replace("-", " ").replace("_", " ")) + ChatColor.GRAY + ": "
									+ ChatColor.RED + soundData.getVolume() + " " + soundData.getPitch()));
		} else
			lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "None");

		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 单击可访问声音编辑菜单");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击可删除所有自定义声音");
	}

	@Override
	public @NotNull SoundListData getClearStatData() {
		return new SoundListData();
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull SoundListData data) { item.addItemTag(getAppliedNBT(data)); }

	@Override
	public @NotNull ArrayList<ItemTag> getAppliedNBT(@NotNull SoundListData data) {

		// Make Array
		ArrayList<ItemTag> ret = new ArrayList<>();

		// Well that
		SoundListData sounds = (SoundListData) data;

		// Add
		sounds.getCustomSounds().forEach(sound -> {
			SoundData value = sounds.get(sound);

			ret.add(new ItemTag("MMOITEMS_SOUND_" + sound.name(), value.getSound()));
			ret.add(new ItemTag("MMOITEMS_SOUND_" + sound.name() + "_VOL", value.getVolume()));
			ret.add(new ItemTag("MMOITEMS_SOUND_" + sound.name() + "_PIT", value.getPitch()));
		});

		// Yes
		return ret;
	}

	public @Nullable SoundListData getLoadedNBT(@NotNull ArrayList<ItemTag> data) {

		// Something to build
		SoundListData sounds = new SoundListData();

		// For each value
		for (CustomSound sound : CustomSound.values()) {

			// Find tag?
			ItemTag soundTag = ItemTag.getTagAtPath("MMOITEMS_SOUND_" + sound.name(), data);
			ItemTag volumeTag = ItemTag.getTagAtPath("MMOITEMS_SOUND_" + sound.name() + "_VOL", data);
			ItemTag pitchTag = ItemTag.getTagAtPath("MMOITEMS_SOUND_" + sound.name() + "_PIT", data);

			// Not null righ
			if (soundTag != null && volumeTag != null && pitchTag != null) {

				// Get as String
				String soundName = (String) soundTag.getValue();
				Double volume = (Double) volumeTag.getValue();
				Double pitch = (Double) pitchTag.getValue();

				// If valid
				if (!soundName.isEmpty()) {

					// Set
					sounds.set(sound, new SoundData(soundName, volume, pitch));
				}
			}
		}

		// Return that amount
		if (sounds.getCustomSounds().size() > 0) {

			// Yes
			return sounds;
		}

		// Failure: No sounds
		return null;
	}

	@Override
	public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

		// Get tags
		ArrayList<ItemTag> relevantTags = new ArrayList<>();

		for (CustomSound sound : CustomSound.values()) {

			// Find tag?
			ItemTag soundTag = null;
			ItemTag volumeTag = null;
			ItemTag pitchTag = null;

			if (mmoitem.getNBT().hasTag("MMOITEMS_SOUND_" + sound.name()))
				soundTag = ItemTag.getTagAtPath("MMOITEMS_SOUND_" + sound.name(), mmoitem.getNBT(), SupportedNBTTagValues.STRING);

			if (mmoitem.getNBT().hasTag("MMOITEMS_SOUND_" + sound.name() + "_VOL"))
				volumeTag = ItemTag.getTagAtPath("MMOITEMS_SOUND_" + sound.name() + "_VOL", mmoitem.getNBT(), SupportedNBTTagValues.DOUBLE);

			if (mmoitem.getNBT().hasTag("MMOITEMS_SOUND_" + sound.name() + "_PIT"))
				pitchTag = ItemTag.getTagAtPath("MMOITEMS_SOUND_" + sound.name() + "_PIT", mmoitem.getNBT(), SupportedNBTTagValues.DOUBLE);

			// All or none
			if (soundTag != null && volumeTag != null && pitchTag != null) {

				// Include
				relevantTags.add(soundTag);
				relevantTags.add(volumeTag);
				relevantTags.add(pitchTag);
			}
		}

		// Use that
		SoundListData sounds = (SoundListData) getLoadedNBT(relevantTags);

		// Valid?
		if (sounds != null) {

			// Set
			mmoitem.setData(ItemStats.CUSTOM_SOUNDS, sounds);
		}
	}

	@Override
	public void onConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player, boolean vanillaEating) {

		// No sound, straight up default-yo
		if (!mmo.hasData(ItemStats.CUSTOM_SOUNDS)) {
			playDefaultSound(player);
			return;
		}

		// Find data
		SoundListData slData = (SoundListData) mmo.getData(ItemStats.CUSTOM_SOUNDS);
		SoundData cs = slData.get(CustomSound.ON_CONSUME);

		// Default sound :sleep:
		if (cs == null) playDefaultSound(player);

			// Play custom sound lets go
		else {
			String fixedSoundName = cs.getSound().toLowerCase().replace("_", ".");
			player.getWorld().playSound(player.getLocation(), fixedSoundName, (float) cs.getVolume(), (float) cs.getPitch());
		}
	}

	void playDefaultSound(@NotNull Player player) { player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_GENERIC_EAT, 1, 1); }
}
