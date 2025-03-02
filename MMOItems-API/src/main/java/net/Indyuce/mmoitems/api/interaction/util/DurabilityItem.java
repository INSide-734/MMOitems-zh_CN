package net.Indyuce.mmoitems.api.interaction.util;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.gson.JsonParser;
import io.lumine.mythic.lib.gson.JsonSyntaxException;
import io.lumine.mythic.lib.util.lang3.Validate;
import io.lumine.mythic.lib.version.Sounds;
import io.lumine.mythic.lib.version.VEnchantment;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Handles durability decreasing logic for both vanilla and MMOItems custom
 * durability systems in an interface friendly way. See inheritors of this
 * class to check how custom and vanilla durability are handled respectively.
 *
 * @author jules
 */
public abstract class DurabilityItem {
    protected final ItemStack item;
    protected final NBTItem nbtItem;
    @Nullable
    protected final Player player;
    @Nullable
    protected final EquipmentSlot slot;
    private final int unbreakingLevel;

    @Nullable
    private ItemStack itemOutput;

    protected static final Random RANDOM = new Random();

    protected DurabilityItem(@Nullable Player player, @NotNull NBTItem nbtItem, @Nullable EquipmentSlot slot) {
        this.nbtItem = nbtItem;
        this.item = nbtItem.getItem();
        this.player = player;
        this.slot = slot;

        this.unbreakingLevel = MMOUtils.getLevel(nbtItem.getItem(), VEnchantment.UNBREAKING.get());
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public NBTItem getNBTItem() {
        return nbtItem;
    }

    @NotNull
    public DurabilityItem addDurability(int gain) {
        Validate.isTrue(itemOutput == null, "Item already generated");

        if (gain > 0) onDurabilityAdd(gain);
        return this;
    }

    @NotNull
    public DurabilityItem decreaseDurability(int loss) {
        Validate.isTrue(itemOutput == null, "Item already generated");

        // This happens when Unbreaking applies for a damageable item
        if (loss == 0) return this;

        /*
         * Calculate the chance of the item not losing any durability because of
         * the vanilla unbreaking enchantment ; an item with unbreaking X has 1
         * 1 chance out of (X + 1) to lose a durability point, that's 50% chance
         * -> 33% chance -> 25% chance -> 20% chance...
         */
        if (rollUnbreaking()) return this;

        // Apply durability decrease
        onDurabilityDecrease(loss);

        return this;
    }

    @Nullable
    public ItemStack toItem() {

        // Cache result
        if (itemOutput != null) return itemOutput;

        if (isBroken()) {

            // Lost when broken
            if (isLostWhenBroken()) {

                // Play sound when item breaks
                if (player != null) {
                    player.getWorld().playSound(player.getLocation(), Sounds.ENTITY_ITEM_BREAK, 1, 1);
                    PlayerData.get(player).getInventory().scheduleUpdate();
                }

                return itemOutput = null;
            }

            // Checks for possible downgrade
            if (isDowngradedWhenBroken()) {
                ItemTag uTag = ItemTag.getTagAtPath(ItemStats.UPGRADE.getNBTPath(), getNBTItem(), SupportedNBTTagValues.STRING);
                if (uTag != null) try {
                    UpgradeData data = new UpgradeData(JsonParser.parseString((String) uTag.getValue()).getAsJsonObject());

                    // If it cannot be downgraded (reached min), DEATH
                    if (data.getLevel() <= data.getMin()) return null;

                    // Downgrade and FULLY repair item
                    LiveMMOItem mmo = new LiveMMOItem(getNBTItem());
                    //mmo.setData(ItemStats.CUSTOM_DURABILITY, new DoubleData(maxDurability));
                    mmo.getUpgradeTemplate().upgradeTo(mmo, data.getLevel() - 1);
                    NBTItem nbtItem = mmo.newBuilder().buildNBT();

                    // Fully repair item
                    DurabilityItem item = DurabilityItem.from(player, nbtItem);
                    Validate.notNull(item, "Internal error");
                    item.addDurability(item.getMaxDurability());

                    // Return
                    return itemOutput = item.toItem();

                } catch (JsonSyntaxException | IllegalStateException ignored) {
                    // Nothing
                }
            }
        }

        return itemOutput = applyChanges();
    }

    public boolean isLostWhenBroken() {
        return nbtItem.getBoolean("MMOITEMS_WILL_BREAK");
    }

    private boolean isDowngradedWhenBroken() {
        return nbtItem.getBoolean("MMOITEMS_BREAK_DOWNGRADE");
    }

    @NotNull
    protected abstract ItemStack applyChanges();

    public abstract boolean isBroken();

    public abstract int getDurability();

    public abstract int getMaxDurability();

    public abstract void onDurabilityAdd(int gain);

    public abstract void onDurabilityDecrease(int loss);

    @NotNull
    public DurabilityItem updateInInventory() {
        ItemStack resultingItem = toItem();

        // No player is provided, just update the item and inshallah
        if (player == null || slot == null) {
            Validate.notNull(resultingItem, "Null item, no slot/player provided");
            this.item.setItemMeta(resultingItem.getItemMeta());
        }

        // Place item
        else {
            player.getInventory().setItem(slot, resultingItem);
        }

        return this;
    }

    private boolean rollUnbreaking() {
        return unbreakingLevel > 0 && RANDOM.nextInt(unbreakingLevel + 1) != 0;
    }

    protected int retrieveMaxVanillaDurability(@NotNull ItemStack item, @NotNull ItemMeta meta) {
        if (MythicLib.plugin.getVersion().isAbove(1, 20, 5) && meta instanceof Damageable && ((Damageable) meta).hasMaxDamage()) {
            int maxDamage = ((Damageable) meta).getMaxDamage();
            if (maxDamage > 0) return maxDamage;
        }
        return item.getType().getMaxDurability();
    }

    @Nullable
    public static DurabilityItem vanilla(@Nullable Player player, @NotNull ItemStack item) {
        try {
            return new VanillaDurabilityItem(player, NBTItem.get(item), null);
        } catch(Throwable ignored) {
            return null;
        }
    }

    @Nullable
    public static DurabilityItem custom(@Nullable Player player, @NotNull ItemStack item) {
        return custom(player, null, item);
    }

    @Nullable
    public static DurabilityItem custom(@Nullable Player player, @Nullable EquipmentSlot slot, @NotNull ItemStack item) {
        NBTItem nbtItem = NBTItem.get(item);
        return nbtItem.hasTag(ItemStats.MAX_DURABILITY.getNBTPath()) ? new CustomDurabilityItem(player, nbtItem, slot) : null;
    }

    @Nullable
    public static DurabilityItem from(@Nullable Player player, @NotNull ItemStack item) {
        return from(player, item, null, null);
    }

    @Nullable
    public static DurabilityItem from(@Nullable Player player, @NotNull NBTItem item) {
        return from(player, item.getItem(), item, null);
    }

    @Nullable
    public static DurabilityItem from(@Nullable Player player, @NotNull NBTItem item, @Nullable EquipmentSlot slot) {
        return from(player, item.getItem(), item, slot);
    }

    @Nullable
    public static DurabilityItem from(@Nullable Player player, @NotNull ItemStack item, @Nullable EquipmentSlot slot) {
        return from(player, item, null, slot);
    }

    @Nullable
    public static DurabilityItem from(@Nullable Player player, @NotNull ItemStack item, @Nullable NBTItem nbtItem, @Nullable EquipmentSlot slot) {

        // No durability applied in creative mode
        if (player != null && player.getGameMode() == GameMode.CREATIVE) return null;

        if (nbtItem == null) nbtItem = NBTItem.get(item);

        // Check for custom durability
        if (nbtItem.hasTag(ItemStats.MAX_DURABILITY.getNBTPath()))
            return new CustomDurabilityItem(player, nbtItem, slot);

        // Try vanilla durability item
        try {
            return new VanillaDurabilityItem(player, nbtItem, slot);
        } catch (Exception exception) {
            // No max durability
        }

        return null;
    }
}
