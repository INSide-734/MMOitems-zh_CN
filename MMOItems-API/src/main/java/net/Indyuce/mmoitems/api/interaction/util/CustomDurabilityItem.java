package net.Indyuce.mmoitems.api.interaction.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.event.item.CustomDurabilityDamage;
import net.Indyuce.mmoitems.api.event.item.ItemCustomRepairEvent;
import net.Indyuce.mmoitems.api.item.util.LoreUpdate;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class which handles custom durability; you can add or remove
 * some durability from an item and generate the new version.
 * <p>
 * This does update the item lore dynamically. However due to the current
 * implementation of {@link LoreUpdate}, if other plugins edit the line
 * corresponding to durability, MMOItems won't be able to detect it again.
 *
 * @author indyuce
 */
public class CustomDurabilityItem extends DurabilityItem {
    private final int maxDurability, initialDurability;
    private final boolean barHidden;

    private int durability;

    /**
     * Use to handle durability changes for MMOItems
     * without using heavy MMOItem class methods
     *
     * @param player  Player holding the item
     * @param nbtItem Item with durability
     * @param slot    Slot of equipment of said item
     */
    public CustomDurabilityItem(@Nullable Player player, @NotNull NBTItem nbtItem, @Nullable EquipmentSlot slot) {
        super(player, nbtItem, slot);

        maxDurability = nbtItem.getInteger("MMOITEMS_MAX_DURABILITY");
        Validate.isTrue(maxDurability > 0, "No custom durability");
        initialDurability = nbtItem.hasTag("MMOITEMS_DURABILITY") ? nbtItem.getInteger("MMOITEMS_DURABILITY") : maxDurability;
        durability = initialDurability;
        barHidden = nbtItem.getBoolean("MMOITEMS_DURABILITY_BAR");

        // TODO technically this call is needed but who would do that?
        //Validate.isTrue(!item.getItemMeta().isUnbreakable(), "Item is unbreakable");
    }

    @Override
    public int getMaxDurability() {
        return maxDurability;
    }

    @Override
    public int getDurability() {
        return durability;
    }

    public boolean isBarHidden() {
        return barHidden;
    }

    /**
     * @return If both this is a VALID custom durability item and if the item is broken.
     *         This will return <code>false</code> if it is not a valid item
     */
    public boolean isBroken() {
        return maxDurability > 0 && durability <= 0;
    }

    @Override
    public void onDurabilityAdd(int gain) {

        ItemCustomRepairEvent event = new ItemCustomRepairEvent(this, gain);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        durability = Math.min(durability + gain, maxDurability);
    }

    @Override
    public void onDurabilityDecrease(int loss) {

        CustomDurabilityDamage event = new CustomDurabilityDamage(this, loss);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        loss = event.getDurabilityDecrease();
        durability = Math.max(0, Math.min(durability - loss, maxDurability));
    }

    @NotNull
    @Override
    protected ItemStack applyChanges() {

        // No modification needs to be done
        if (durability == initialDurability) return nbtItem.getItem();

        // Apply the NBT tag
        ItemStack item = nbtItem.addTag(new ItemTag("MMOITEMS_DURABILITY", durability)).toItem();

        /*
         * Cross multiplication to display the current item durability on the
         * item durability bar. (1 - ratio) because minecraft works with item
         * damage, and item damage is the complementary of the remaining
         * durability.
         *
         * Make sure the vanilla bar displays at least 1 damage for display
         * issues. Also makes sure the item can be mended using the vanilla
         * enchant.
         */
        if (!barHidden && item.getType().getMaxDurability() > 0) {
            final ItemMeta meta = item.getItemMeta();
            final int maxDamage = retrieveMaxVanillaDurability(item, meta);
            final int damage = durability == maxDurability ? 0 : Math.max(1, (int) ((1. - ((double) durability / maxDurability)) * maxDamage));
            ((Damageable) meta).setDamage(damage);
            item.setItemMeta(meta);
        }

        // Item lore update
        final String format = MythicLib.inst().parseColors(ItemStats.ITEM_DAMAGE.getGeneralStatFormat().replace("{max}", String.valueOf(maxDurability)));
        final String old = format.replace("{current}", String.valueOf(initialDurability));
        final String replaced = format.replace("{current}", String.valueOf(durability));
        return new LoreUpdate(item, old, replaced).updateLore();
    }
}
