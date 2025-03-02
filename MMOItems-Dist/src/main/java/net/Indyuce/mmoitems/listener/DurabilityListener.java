package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.version.Sounds;
import net.Indyuce.mmoitems.api.event.item.VanillaDurabilityDamage;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.interaction.util.VanillaDurabilityItem;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class DurabilityListener implements Listener {

    /**
     * Bukkit-MMOItems interface
     * <p>
     * Handles custom durability loss for ANY damageable items.
     */
    @EventHandler(ignoreCancelled = true)
    public void itemDamage(PlayerItemDamageEvent event) {

        // Ignore events called by MMOItems
        if (event instanceof VanillaDurabilityDamage) return;

        // Only apply to custom durability
        final DurabilityItem item = DurabilityItem.custom(event.getPlayer(), event.getItem());
        if (item == null) return;

        // Calculate item durability loss
        item.onDurabilityDecrease(event.getDamage());

        /*
         * If the item is broken and if it is meant to be lost when broken,
         * do NOT cancel the event and make sure the item is destroyed
         */
        final ItemStack newVersion = item.toItem();
        if (newVersion == null) {
            event.setDamage(BIG_DAMAGE);
        } else {
            event.setCancelled(true);
            event.getItem().setItemMeta(newVersion.getItemMeta());
        }
    }

    private static final int BIG_DAMAGE = 1000000;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void vanillaItemDamage(PlayerItemDamageEvent event) {

        // Ignore events called by MMOItems
        if (event instanceof VanillaDurabilityDamage) return;

        // Only apply to custom durability
        final DurabilityItem item = DurabilityItem.vanilla(event.getPlayer(), event.getItem());
        if (item == null) return;

        if (((VanillaDurabilityItem) item).wouldBreak(event.getDamage()) && !item.isLostWhenBroken()) {
            event.setCancelled(true);
            item.decreaseDurability(BIG_DAMAGE).updateInInventory();
        }
    }

    private static final List<DamageCause> IGNORED_CAUSES = Arrays.asList(DamageCause.DROWNING, DamageCause.SUICIDE, DamageCause.FALL, DamageCause.VOID,
            DamageCause.FIRE_TICK, DamageCause.SUFFOCATION, DamageCause.POISON, DamageCause.WITHER, DamageCause.STARVATION, DamageCause.MAGIC);
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /*
     * Using priority HIGHEST to run after the attack event which takes
     * place at priority HIGH
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void playerDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER || IGNORED_CAUSES.contains(event.getCause()))
            return;

        Player player = (Player) event.getEntity();
        int damage = Math.max((int) event.getDamage() / 4, 1);
        for (EquipmentSlot slot : ARMOR_SLOTS)
            if (hasItem(player, slot))
                handleCustomDurability(player, player.getInventory().getItem(slot), slot, damage);
    }

    /*
     * Using priority HIGHEST to run after the attack event which takes
     * place at priority HIGH
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void playerMeleeAttack(EntityDamageByEntityEvent event) {
        if (event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof LivingEntity)
                || !(event.getDamager() instanceof Player) || event.getEntity().hasMetadata("NPC") || event.getDamager().hasMetadata("NPC"))
            return;
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        handleCustomDurability(player, item, EquipmentSlot.HAND, 1);
    }

    /*
    All bows are damageable so this event should be 100% useless.
    Seems to work as of MI 6.10.1 snapshots, Spigot 1.21.4

    @EventHandler(ignoreCancelled = true)
    public void playerBowAttack(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack item = event.getBow();
        EquipmentSlot slotUsed = MMOUtils.getHand(event, player);
        handleUndamageableItem(item, player, slotUsed, 1);
    }
    */

    /**
     * Bukkit-MMOItems interface
     * <p>
     * Enables Mending event to work on custom-durability items.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void mendEvent(PlayerItemMendEvent event) {

        // Useless repair amount
        if (event.getRepairAmount() <= 0) return;

        DurabilityItem durItem = DurabilityItem.custom(null, event.getItem());
        if (durItem != null) {
            event.setCancelled(true); // Cancel event
            durItem.addDurability(event.getRepairAmount()); // Mend
            durItem.updateInInventory(); // Update inventory
        }
    }

    /**
     * This method is for all the items which have 0 max durability i.e
     * which are not breakable hence the {@link Material#getMaxDurability()}
     */
    private void handleCustomDurability(Player player, ItemStack stack, EquipmentSlot slot, int damage) {
        final DurabilityItem item = DurabilityItem.custom(player, slot, stack);
        if (item == null) return;

        item.decreaseDurability(damage);

        if (item.updateInInventory().toItem() == null) {
            // Play break sound
            player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ITEM_BREAK, 1, 1);
        }
    }

    private boolean hasItem(Player player, EquipmentSlot slot) {
        ItemStack found = player.getInventory().getItem(slot);
        return found != null && found.getType() != Material.AIR;
    }
}
