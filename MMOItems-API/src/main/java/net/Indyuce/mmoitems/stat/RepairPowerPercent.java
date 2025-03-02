package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.type.ConsumableItemInteraction;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RepairPowerPercent extends DoubleStat implements ConsumableItemInteraction {
    public RepairPowerPercent() {
        super("REPAIR_PERCENT", Material.DAMAGED_ANVIL, "修复耐久百分比",
                new String[]{"物品可修复耐久度占总耐久度的百分比"},
                new String[]{"consumable"});
    }


    @Override
    public boolean handleConsumableEffect(@NotNull InventoryClickEvent event, @NotNull PlayerData playerData, @NotNull Consumable consumable, @NotNull NBTItem target, @Nullable Type targetType) {
        final double repairPower = consumable.getNBTItem().getStat(ItemStats.REPAIR_PERCENT.getId());
        if (repairPower <= 0)
            return false;

        return RepairPower.handleRepair(playerData, consumable, target, durItem -> {
            final double maxDurability = durItem.getMaxDurability();
            return (int) (repairPower * maxDurability / 100);
        });
    }
}
