package net.Indyuce.mmoitems.api.interaction;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.util.SmartGive;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.ConsumableConsumedEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.item.util.LoreUpdate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import net.Indyuce.mmoitems.stat.type.PlayerConsumable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class Consumable extends UseItem {

    @Deprecated
    public Consumable(Player player, NBTItem item) {
        super(player, item);
    }

    public Consumable(PlayerData player, NBTItem item) {
        super(player, item);
    }

    @Override
    public boolean checkItemRequirements() {
        return MythicLib.plugin.getFlags().isFlagAllowed(player, CustomFlag.MI_CONSUMABLES) && playerData.getRPG().canUse(getNBTItem(), true);
    }

    /**
     * Applies a consumable onto an item
     *
     * @param event  The click event
     * @param target The item on which the consumable is being applied
     * @return If the consumable was successfully applied on the item
     */
    public boolean useOnItem(@NotNull InventoryClickEvent event, @NotNull NBTItem target) {
        if (event.getClickedInventory() != event.getWhoClicked().getInventory())
            return false;

        // Make sure it is an MMOItem
        Type targetType = MMOItems.getType(target);

        for (ConsumableItemInteraction action : MMOItems.plugin.getStats().getConsumableActions())
            if (action.handleConsumableEffect(event, playerData, this, target, targetType))
                return true;

        return false;
    }

    /**
     * @param vanillaEating See {@link PlayerConsumable#onConsume(VolatileMMOItem, Player, boolean)}
     * @return If the item should be consumed
     */
    public ConsumableConsumeResult useOnPlayer(EquipmentSlot handUsed, boolean vanillaEating) {
        NBTItem nbtItem = getNBTItem();

        // Inedible stat cancels this operation from the beginning
        if (nbtItem.getBoolean(ItemStats.INEDIBLE.getNBTPath()))
            return ConsumableConsumeResult.CANCEL;

        // So a consumable is being consumed, eh
        ConsumableConsumedEvent event = new ConsumableConsumedEvent(playerData, mmoitem, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return ConsumableConsumeResult.CANCEL;

        final boolean disableRightClickConsume = nbtItem.getBoolean(ItemStats.DISABLE_RIGHT_CLICK_CONSUME.getNBTPath());
        final boolean consumeItem; // Should item be consumed

        if (!disableRightClickConsume) {

            /*
             * If the item does not have a maximum amount of uses, this will always
             * return 0 and that portion will just skip.
             * If the item does have a max amount of uses but it's the last
             * use, this portion will skip and the item will be consumed anyways.
             */
            int usesLeft = nbtItem.getInteger(ItemStats.MAX_CONSUME.getNBTPath());

            // Decrease amount of uses
            if (usesLeft > 1) {

                // TODO weird MI7 stuff to fix using PDC's
                ItemStack oldItem = nbtItem.getItem();
                oldItem.setItemMeta(oldItem.getItemMeta().clone());

                usesLeft -= 1;
                nbtItem.addTag(new ItemTag(ItemStats.MAX_CONSUME.getNBTPath(), usesLeft));

                // Dynamic lore update
                final String format = MythicLib.inst().parseColors(ItemStats.MAX_CONSUME.getGeneralStatFormat());
                final String old = format.replace("{value}", String.valueOf(usesLeft + 1));
                final String replaced = format.replace("{value}", String.valueOf(usesLeft));
                ItemStack newItem = new LoreUpdate(nbtItem.toItem(), old, replaced).updateLore();

                // This fixes the issue when players right click stacked consumables
                if (oldItem.getAmount() > 1) {
                    newItem.setAmount(1);
                    player.getInventory().setItem(handUsed, newItem);
                    oldItem.setAmount(oldItem.getAmount() - 1);
                    new SmartGive(player).give(oldItem);
                }

                // Player holding non-stacked item
                else player.getInventory().setItem(handUsed, newItem);

                consumeItem = event.isConsume(false);
            }

            // Consume item as it is the last use
            else consumeItem = event.isConsume(true);
        }

        // Never consume item
        else consumeItem = event.isConsume(false);

        // Run through all consumable effects
        final boolean vanillaConsumption = vanillaEating && consumeItem;
        for (PlayerConsumable sc : MMOItems.plugin.getStats().getPlayerConsumables())
            sc.onConsume(mmoitem, player, vanillaConsumption);

        return consumeItem ? ConsumableConsumeResult.CONSUME : ConsumableConsumeResult.NOT_CONSUME;
    }

    /**
     * @return If an item should be eaten not when right clicked, but after
     *         the eating animation. This does check if the item is actually edible
     */
    public boolean hasVanillaEating() {
        return (getItem().getType().isEdible() || getItem().getType() == Material.POTION || getItem().getType() == Material.MILK_BUCKET)
                && getNBTItem().hasTag("MMOITEMS_VANILLA_EATING");
    }

    public static enum ConsumableConsumeResult {

        /**
         * No consumable effects or command or cooldowns are applied at all.
         * <p>
         * This is also used by non edible consumables.
         */
        CANCEL,

        /**
         * Consume was a success and the item needs to be removed
         * from the player's inventory.
         */
        CONSUME,

        /**
         * Consume was a success but the item should not be consumed.
         * This is used to handle the 'Max Consumes' item stat.
         * consumables with "Disable right click consume' also use this.
         * <p>
         * Using {@link ConsumableConsumedEvent#setConsumed(boolean)}, external plugins can also make it
         * so the consumable is not consumed but its effects are applied.
         */
        NOT_CONSUME;
    }
}
