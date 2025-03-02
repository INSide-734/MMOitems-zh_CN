package net.Indyuce.mmoitems.api.player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.handler.StatHandler;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.player.modifier.ModifierType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.WeaponBaseStat;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlayerStats {
    private final PlayerData playerData;

    public PlayerStats(PlayerData playerData) {
        this.playerData = playerData;
    }

    public PlayerData getData() {
        return playerData;
    }

    @NotNull
    public StatMap getMap() {
        return playerData.getMMOPlayerData().getStatMap();
    }

    public double getStat(@NotNull ItemStat<?, ?> stat) {
        return getMap().getStat(stat.getId());
    }

    @NotNull
    public StatInstance getInstance(@NotNull ItemStat<?, ?> stat) {
        return getMap().getInstance(stat.getId());
    }

    /**
     * Used to cache stats when a player casts a skill so that if the player
     * swaps items or changes any of his stat value before the end of the
     * spell duration, the stat value is not updated.
     *
     * @param castSlot Every stat modifier with the opposite modifier
     *                 source will NOT be taken into account for stat calculation
     * @return
     */
    @NotNull
    public PlayerMetadata newTemporary(@NotNull EquipmentSlot castSlot) {
        return playerData.getMMOPlayerData().getStatMap().cache(castSlot);
    }

    public void updateStats() {
        for (ItemStat<?, ?> stat : MMOItems.plugin.getStats().getNumericStats()) {

            // Let MMOItems first add stat modifiers, and then update the stat instance
            StatInstance statInstance = getInstance(stat);
            StatInstance.ModifierPacket packet = statInstance.newPacket();

            // Remove previous potential modifiers
            packet.removeIf(name -> name.startsWith("MMOItem"));

            // Add set bonuses
            if (playerData.hasSetBonuses() && playerData.getSetBonuses().hasStat(stat))
                packet.addModifier(new StatModifier("MMOItemSetBonus", stat.getId(), playerData.getSetBonuses().getStat(stat), ModifierType.FLAT, EquipmentSlot.OTHER, ModifierSource.OTHER));

            // The index of the mmoitem stat modifier being added
            int index = 0;

            for (EquippedItem item : playerData.getInventory().getEquipped()) {
                double value = item.getNBT().getStat(stat.getId());

                if (value != 0) {
                    final ModifierSource source = item.getCached().getType().getModifierSource();

                    // Apply hand weapon stat offset
                    if (source.isWeapon() && stat instanceof WeaponBaseStat)
                        value = fixWeaponBase(statInstance, stat, value);

                    packet.addModifier(new StatModifier("MMOItem-" + index++, stat.getId(), value, ModifierType.FLAT, item.getSlot(), source));
                }
            }

            // Finally run a stat update after all modifiers have been gathered in the packet
            packet.update();
        }
    }

    private double fixWeaponBase(StatInstance statInstance, ItemStat<?, ?> stat, double statValue) {
        @NotNull Optional<StatHandler> opt = MythicLib.plugin.getStats().getHandler(stat.getId());
        return opt.map(statHandler -> statValue - statHandler.getBaseValue(statInstance)).orElse(statValue);
    }
}
