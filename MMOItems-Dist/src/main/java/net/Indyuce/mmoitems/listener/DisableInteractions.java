package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Keyed;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DisableInteractions implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void itemDropping(PlayerDropItemEvent event) {
        ItemStack itemStack = event.getItemDrop().getItemStack();

        if (isDisabled(NBTItem.get(itemStack), "drop")) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void anvilInteractions(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (inv == null || inv.getType() != InventoryType.ANVIL || event.getSlotType() != SlotType.RESULT)
            return;

        if (isDisabled(NBTItem.get(event.getCurrentItem()), "repair"))
            event.setCancelled(true);
        else if (inv.getItem(1) != null && isDisabled(NBTItem.get(inv.getItem(1)), "repair"))
            event.setCancelled(true);
    }

    @EventHandler
    public void grindstoneInteractions(InventoryClickEvent event) {
        if (MythicLib.plugin.getVersion().isBelowOrEqual(1, 13))
            return;

        Inventory inv = event.getClickedInventory();
        if (inv == null || inv.getType() != InventoryType.GRINDSTONE || event.getSlotType() != SlotType.RESULT)
            return;

        if (isDisabled(NBTItem.get(inv.getItem(0)), "repair") || isDisabled(NBTItem.get(inv.getItem(1)), "repair"))
            event.setCancelled(true);
    }

    @EventHandler
    public void smithingTableInteractions(InventoryClickEvent event) {
        if (MythicLib.plugin.getVersion().isBelowOrEqual(1, 15))
            return;

        Inventory inv = event.getClickedInventory();
        if (inv == null || inv.getType() != InventoryType.SMITHING || event.getSlotType() != SlotType.RESULT)
            return;

        if (isDisabled(NBTItem.get(inv.getItem(0)), "smith") || isDisabled(NBTItem.get(inv.getItem(1)), "smith"))
            event.setCancelled(true);
    }

    @EventHandler
    public void enchantTablesInteractions(EnchantItemEvent event) {
        if (isDisabled(NBTItem.get(event.getItem()), "enchant"))
            event.setCancelled(true);
    }

    @EventHandler
    public void furnaceInteractions(FurnaceSmeltEvent event) {
        if (isDisabled(NBTItem.get(event.getSource()), "smelt"))
            event.setCancelled(true);
    }

    /**
     * Disables both click interactions if the corresponding
     * option was found on the item
     * <p>
     * Also prevents interactions with UNIDENTIFIED items
     */
    @EventHandler(priority = EventPriority.LOW)
    public void clickInteractions(PlayerInteractEvent event) {
        if (!event.hasItem())
            return;

        NBTItem item = NBTItem.get(event.getItem());
        if (item.getBoolean("MMOITEMS_DISABLE_INTERACTION") || item.hasTag("MMOITEMS_UNIDENTIFIED_ITEM"))
            event.setCancelled(true);
    }

    /**
     * Prevents unidentified tools from breaking blocks
     */
    @EventHandler(priority = EventPriority.LOW)
    public void miningInteractions(BlockBreakEvent event) {
        NBTItem item = NBTItem.get(event.getPlayer().getInventory().getItemInMainHand());
        if (item.hasTag("MMOITEMS_UNIDENTIFIED_ITEM"))
            event.setCancelled(true);
    }

    @EventHandler
    public void entityInteractions(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand)
            return;

        NBTItem item = NBTItem.get(event.getHand() == EquipmentSlot.OFF_HAND ? event.getPlayer().getInventory().getItemInOffHand()
                : event.getPlayer().getInventory().getItemInMainHand());
        if (item.getBoolean("MMOITEMS_DISABLE_INTERACTION"))
            event.setCancelled(true);
    }

    @EventHandler
    public void consumeInteractions(PlayerItemConsumeEvent event) {
        NBTItem item = NBTItem.get(event.getItem());
        if (item.getBoolean("MMOITEMS_DISABLE_INTERACTION"))
            event.setCancelled(true);
    }

    @EventHandler
    public void workbenchInteractions(CraftItemEvent event) {
        if (event.getRecipe() instanceof Keyed)
            if (((Keyed) event.getRecipe()).getKey().getNamespace().equals("mmoitems")) {
                String craftingPerm = NBTItem.get(event.getCurrentItem()).getString("MMOITEMS_CRAFT_PERMISSION");
                if (!craftingPerm.isEmpty() && !event.getWhoClicked().hasPermission(craftingPerm))
                    event.setCancelled(true);
                return;
            }

        for (ItemStack item : event.getInventory().getMatrix()) {
            if (isDisabled(NBTItem.get(item), "craft")) {
                event.setCancelled(true);
                return;
            }
        }

        if (MMOItems.plugin.getConfig().getStringList("disable-vanilla-recipes").contains(event.getCurrentItem().getType().name()))
            event.setCancelled(true);
    }

    @EventHandler
    public void shootBowInteractions(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player) || event.getBow() == null) return;

        /*
        Not needed as of Spigot 1.21.4, MI 6.10.1 dev builds
        DurabilityItem durItem = DurabilityItem.from(((Player) event.getEntity()).getPlayer(), event.getBow());
        if (durItem != null) {

            // Cannot shoot a broken bow
            if (durItem.isBroken())
                event.setCancelled(true);
        }
        */

        Player player = (Player) event.getEntity();
        ItemStack stack = firstArrow(player);
        if (stack == null) return;

        // Cannot shoot arrow?
        NBTItem arrow = NBTItem.get(stack);
        if (arrow.hasType() && (MMOItems.plugin.getConfig().getBoolean("disable-interactions.arrow-shooting")
                || arrow.getBoolean("MMOITEMS_DISABLE_ARROW_SHOOTING")))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void attackWithUnidentifiedItems(EntityDamageByEntityEvent event) {
        if (event.getDamage() == 0 || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof LivingEntity)
                || !(event.getDamager() instanceof Player) || event.getEntity().hasMetadata("NPC") || event.getDamager().hasMetadata("NPC"))
            return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        /*
        Not needed as of Spigot 1.21.4, MI 6.10.1 dev builds
        DurabilityItem durItem = DurabilityItem.from(player, item);
        if (durItem != null) {

            // If weapon is broken don't do damage
            if (durItem.isBroken())
                event.setCancelled(true);
        }
         */

        // Prevent unidentified weapons from being used
        if (NBTItem.get(item).hasTag("MMOITEMS_UNIDENTIFIED_ITEM"))
            event.setCancelled(true);
    }

    @Nullable
    private ItemStack firstArrow(Player player) {

        // Check offhand first
        if (player.getInventory().getItemInOffHand().getType().name().contains("ARROW"))
            return player.getInventory().getItemInOffHand();

        // Check for every slot
        ItemStack[] storage = player.getInventory().getStorageContents();
        for (ItemStack item : storage)
            if (item != null && item.getType().name().contains("ARROW")) return item;

        // No arrow to shoot
        return null;
    }

    private boolean isDisabled(NBTItem nbt, String type) {
        return nbt.hasType() && MMOItems.plugin.getConfig().getBoolean("disable-interactions." + type)
                || nbt.getBoolean("MMOITEMS_DISABLE_" + type.toUpperCase().replace("-", "_") + "ING");
    }
}
