package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.version.VInventoryView;
import io.lumine.mythic.lib.version.VersionUtils;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import net.Indyuce.mmoitems.listener.reforging.*;
import net.Indyuce.mmoitems.reforge.ReforgeReason;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemListener implements Listener {
    public ItemListener() {

        // Register Reforger Listeners
        Bukkit.getPluginManager().registerEvents(new RFGKeepName(), MMOItems.plugin);
        Bukkit.getPluginManager().registerEvents(new RFGKeepLore(), MMOItems.plugin);
        Bukkit.getPluginManager().registerEvents(new RFGKeepEnchantments(), MMOItems.plugin);
        Bukkit.getPluginManager().registerEvents(new RFGKeepExternalSH(), MMOItems.plugin);
        Bukkit.getPluginManager().registerEvents(new RFGKeepGems(), MMOItems.plugin);
        Bukkit.getPluginManager().registerEvents(new RFGKeepModifications(), MMOItems.plugin);
        Bukkit.getPluginManager().registerEvents(new RFGKeepSoulbound(), MMOItems.plugin);
        Bukkit.getPluginManager().registerEvents(new RFGKeepUpgrades(), MMOItems.plugin);
        Bukkit.getPluginManager().registerEvents(new RFGKeepRNG(), MMOItems.plugin);

        // Amount ones
        Bukkit.getPluginManager().registerEvents(new RFGKeepDurability(), MMOItems.plugin);
        Bukkit.getPluginManager().registerEvents(new RFFKeepAmount(), MMOItems.plugin);
        Bukkit.getPluginManager().registerEvents(new RFFKeepSkins(), MMOItems.plugin);
    }

    @EventHandler
    private void onItemCraftRepair(PrepareItemCraftEvent event) {
        final VInventoryView view = VersionUtils.getView(event);
        if (!(view.getPlayer() instanceof Player) || !event.isRepair())
            return;

        final Player player = (Player) view.getPlayer();
        final CraftingInventory inv = event.getInventory();
        final ItemStack air = new ItemStack(Material.AIR);
        final ItemStack originalResult = inv.getResult();

        inv.setResult(air);
        Bukkit.getScheduler().runTaskLater(MMOItems.plugin, () -> {
            List<ItemStack> items = Arrays.stream(inv.getMatrix())
                    .filter(Objects::nonNull)
                    .filter(itemStack -> !itemStack.getType().isAir())
                    .collect(Collectors.toList());
            long mmoItemsCount = items.stream()
                    .filter(itemStack -> NBTItem.get(itemStack).hasTag("MMOITEMS_ITEM_ID"))
                    .count();
            // If there are no MMOItems in the crafting matrix, do nothing
            if (mmoItemsCount == 0) {
                inv.setResult(originalResult);
                player.updateInventory();
                return;
            }

            // If both items are not MMO items or if they don't have the same id return
            if (mmoItemsCount == 1
                    || !NBTItem.get(items.get(0)).getString("MMOITEMS_ITEM_ID").equals(NBTItem.get(items.get(1)).getString("MMOITEMS_ITEM_ID"))) {
                inv.setResult(air);
                player.updateInventory();
                return;
            }
            inv.setResult(originalResult);

            // Is repair disabled in config?
            boolean repairDisabled = items.stream()
                    .allMatch(itemStack -> {
                        final NBTItem nbtItem = NBTItem.get(itemStack);
                        return nbtItem.hasTag("MMOITEMS_DISABLE_REPAIRING") && nbtItem.getBoolean("MMOITEMS_DISABLE_REPAIRING");
                    });

            // Does the item have a MMO durability tag?
            boolean hasCustomDurability = items.stream().allMatch(itemStack -> DurabilityItem.from(player, itemStack) != null);

            if (repairDisabled)
                inv.setResult(air);
            else if (hasCustomDurability) {
                DurabilityItem durabilityItem = DurabilityItem.from(player, items.get(0));
                int summedDurability = items.stream()
                        .map(itemStack -> DurabilityItem.from(player, itemStack))
                        .map(DurabilityItem::getDurability)
                        .reduce(0, Integer::sum);
                int finalDurability = durabilityItem.getMaxDurability() - Math.min(durabilityItem.getMaxDurability(), summedDurability);
                if (finalDurability > 0)
                    durabilityItem.addDurability(finalDurability);
                inv.setResult(durabilityItem.toItem());
            }
            player.updateInventory();
        }, 1);
    }

    @EventHandler(ignoreCancelled = true)
    private void itemPickup(EntityPickupItemEvent event) {
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        ItemStack newItem = modifyItem(event.getItem().getItemStack(), (Player) event.getEntity(), ReforgeReason.PICKUP);
        if (newItem != null) event.getItem().setItemStack(newItem);
    }

    @EventHandler(ignoreCancelled = true)
    private void itemCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        ItemStack newItem = modifyItem(event.getCurrentItem(), (Player) event.getWhoClicked(), ReforgeReason.CRAFT);
        if (newItem != null) event.setCurrentItem(newItem);
    }

    @EventHandler(ignoreCancelled = true)
    private void inventoryMove(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.CRAFTING || !(event.getWhoClicked() instanceof Player))
            return;
        ItemStack newItem = modifyItem(event.getCurrentItem(), (Player) event.getWhoClicked(), ReforgeReason.CLICK);
        if (newItem != null) event.setCurrentItem(newItem);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoin(PlayerJoinEvent event) {
        if (!MythicLib.plugin.hasProfiles()) updateInventory(event.getPlayer());
    }

    public static void updateInventory(Player player) {
        ItemStack newItem = modifyItem(player.getEquipment().getHelmet(), player, ReforgeReason.JOIN);
        if (newItem != null) player.getEquipment().setHelmet(newItem);
        newItem = modifyItem(player.getEquipment().getChestplate(), player, ReforgeReason.JOIN);
        if (newItem != null) player.getEquipment().setChestplate(newItem);
        newItem = modifyItem(player.getEquipment().getLeggings(), player, ReforgeReason.JOIN);
        if (newItem != null) player.getEquipment().setLeggings(newItem);
        newItem = modifyItem(player.getEquipment().getBoots(), player, ReforgeReason.JOIN);
        if (newItem != null) player.getEquipment().setBoots(newItem);

        for (int j = 0; j < 9; j++) {
            newItem = modifyItem(player.getInventory().getItem(j), player, ReforgeReason.JOIN);
            if (newItem != null) player.getInventory().setItem(j, newItem);
        }

        newItem = modifyItem(player.getEquipment().getItemInOffHand(), player, ReforgeReason.JOIN);
        if (newItem != null) player.getEquipment().setItemInOffHand(newItem);
    }

    @Nullable
    private static ItemStack modifyItem(@Nullable ItemStack stack, @NotNull Player player, @NotNull ReforgeReason reason) {

        // Sleep on metaless stacks
        if (stack == null || !stack.hasItemMeta())
            return null;

        // Create a reforger to look at it
        MMOItemReforger mod = new MMOItemReforger(stack);
        if (!mod.hasTemplate())
            return null;

        // Its not GooP Converter's VANILLA is it?
        if ("VANILLA".equals(mod.getNBTItem().getString("MMOITEMS_ITEM_ID")))
            return null;

        // Disabled in config?
        if (MMOItems.plugin.getConfig().getBoolean("item-revision.disable-on." + reason.name().toLowerCase()))
            return null;

        // Greater RevID in template? Go ahead, update!
        int templateRevision = mod.getTemplate().getRevisionId();
        int mmoitemRevision = (mod.getNBTItem().hasTag(ItemStats.REVISION_ID.getNBTPath()) ? mod.getNBTItem().getInteger(ItemStats.REVISION_ID.getNBTPath()) : 1);
        if (templateRevision <= mmoitemRevision)
            return null;

        // All right update then
        if (!mod.reforge(MMOItems.plugin.getLanguage().revisionOptions, player))
            return null;

        // Drop all those items
        for (ItemStack drop : player.getInventory().addItem(
                mod.getReforgingOutput().toArray(new ItemStack[0])).values()) {

            // Not air right
            if (SilentNumbers.isAir(drop))
                continue;

            // Drop to the world
            player.getWorld().dropItem(player.getLocation(), drop);
        }

        // That's it
        return mod.getResult();
    }
}
