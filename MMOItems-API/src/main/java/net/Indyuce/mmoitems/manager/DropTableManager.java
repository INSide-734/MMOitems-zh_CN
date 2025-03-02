package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.UtilityMethods;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.Indyuce.mmoitems.api.droptable.DropTable;
import net.Indyuce.mmoitems.api.event.ItemDropEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class DropTableManager implements Listener, Reloadable {
	private final Map<EntityType, DropTable> monsters = new HashMap<>();
	private final Map<Material, DropTable> blocks = new HashMap<>();
	private final Map<Integer, DropTable> customBlocks = new HashMap<>();

	public DropTableManager() {
		reload();
	}

	public void reload() {
		monsters.clear();
		blocks.clear();
		customBlocks.clear();

		FileConfiguration config = new ConfigFile("drops").getConfig();
		if (config.contains("monsters"))
			for (String key : config.getConfigurationSection("monsters").getKeys(false))
				try {
					EntityType type = EntityType.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					monsters.put(type, new DropTable(config.getConfigurationSection("monsters." + key)));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.SEVERE,
							"Could not read drop table with mob type '" + key + "': " + exception.getMessage());
				}

		if (config.contains("blocks"))
			for (String key : config.getConfigurationSection("blocks").getKeys(false))
				try {
					Material material = Material.valueOf(key.toUpperCase().replace("-", "_").replace(" ", "_"));
					blocks.put(material, new DropTable(config.getConfigurationSection("blocks." + key)));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.SEVERE,
							"Could not read drop table with material '" + key + "': " + exception.getMessage());
				}

		if (config.contains("customblocks"))
			for (String key : config.getConfigurationSection("customblocks").getKeys(false))
				try {
					int id = Integer.parseInt(key);
					customBlocks.put(id, new DropTable(config.getConfigurationSection("customblocks." + key)));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.SEVERE,
							"Could not read drop table with custom block '" + key + "': " + exception.getMessage());
				}
	}

	@EventHandler
	public void entityDrops(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		Player killer = entity.getKiller();
		if (killer != null && killer.hasMetadata("NPC"))
			return;

		if (monsters.containsKey(entity.getType())) {
			List<ItemStack> drops = monsters.get(entity.getType()).read(killer != null ? PlayerData.get(killer) : null, false);
			ItemDropEvent called = new ItemDropEvent(killer, drops, entity);
			Bukkit.getPluginManager().callEvent(called);
			if (called.isCancelled())
				return;

			event.getDrops().addAll(drops);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void blockDrops(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (player.getGameMode() == GameMode.CREATIVE) return;

		Block block = event.getBlock();
		Optional<CustomBlock> opt = MMOItems.plugin.getCustomBlocks().getFromBlock(block.getBlockData());

		// Custom block
		if (opt.isPresent()) {
			CustomBlock customBlock = opt.get();

			/*
			 * Check if corresponding custom block has a drop table registered,
			 * and only reads corresponding drop table if the tool has enough power
			 */
			if (customBlocks.containsKey(customBlock.getId()) && MMOUtils.getPickaxePower(player) >= customBlock.getRequiredPower()) {
				PlayerData playerData = PlayerData.get(player);
				List<ItemStack> drops = customBlocks.get(customBlock.getId()).read(playerData, hasSilkTouchTool(player));
				ItemDropEvent called = new ItemDropEvent(player, drops, customBlock);
				Bukkit.getPluginManager().callEvent(called);
				if (called.isCancelled())
					return;

				Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () -> {
					for (ItemStack drop : drops)
						UtilityMethods.dropItemNaturally(block.getLocation(), drop);
				}, 2);
			}
		}

		// Normal block
		else if (blocks.containsKey(block.getType())) {
			Material type = block.getType();
			List<ItemStack> drops = blocks.get(type).read(PlayerData.get(player), hasSilkTouchTool(player));
			ItemDropEvent called = new ItemDropEvent(player, drops, block);
			Bukkit.getPluginManager().callEvent(called);
			if (called.isCancelled())
				return;

			Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () -> {
				for (ItemStack drop : drops)
					UtilityMethods.dropItemNaturally(block.getLocation(), drop);
			}, 2);
		}
	}

	public boolean hasSilkTouchTool(Player player) {
		ItemStack item = player.getInventory().getItemInMainHand();
		return item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);
	}
}