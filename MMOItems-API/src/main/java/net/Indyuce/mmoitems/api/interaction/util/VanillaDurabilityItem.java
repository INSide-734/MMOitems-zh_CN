package net.Indyuce.mmoitems.api.interaction.util;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.event.item.VanillaDurabilityDamage;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VanillaDurabilityItem extends DurabilityItem {
    private final int maxDamage, initialDamage;
    private final ItemMeta meta;

    private int damage;

    protected VanillaDurabilityItem(@Nullable Player player, @NotNull NBTItem nbtItem, @Nullable EquipmentSlot slot) {
        super(player, nbtItem, slot);

        meta = item.getItemMeta();
        Validate.isTrue(meta instanceof Damageable, "Item is not damageable");
        maxDamage = retrieveMaxVanillaDurability(item, meta);
        Validate.isTrue(maxDamage > 0, "No max damage");
        initialDamage = ((Damageable) meta).getDamage();
        damage = initialDamage;
        Validate.isTrue(!meta.isUnbreakable(), "Item is unbreakable");
    }

    @NotNull
    @Override
    protected ItemStack applyChanges() {

        if (damage == initialDamage) return item;

        // #setDamage throws an error if damage is too high
        ((Damageable) meta).setDamage(Math.min(damage, maxDamage));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public boolean isBroken() {
        return damage >= maxDamage;
    }

    public boolean wouldBreak(int extraDamage) {
        return damage + extraDamage >= maxDamage;
    }

    @Override
    public int getDurability() {
        return maxDamage - damage;
    }

    @Override
    public int getMaxDurability() {
        return maxDamage;
    }

    @Override
    public void onDurabilityAdd(int gain) {
        // TODO call event
        damage -= gain;
    }

    @Override
    public void onDurabilityDecrease(int loss) {

        if (player != null) {
            VanillaDurabilityDamage called = new VanillaDurabilityDamage(this, loss);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled()) return;
            loss = called.getDamage();
        }

        damage += loss;
    }
}
