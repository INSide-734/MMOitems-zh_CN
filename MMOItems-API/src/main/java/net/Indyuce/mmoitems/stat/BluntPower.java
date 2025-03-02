package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.WeaponBaseStat;
import org.bukkit.Material;

public class BluntPower extends WeaponBaseStat {
    public BluntPower() {
        super("BLUNT_POWER",
                Material.IRON_AXE,
                "Blunt Power",
                new String[]{"The radius of the AoE attack.", "If set to 2.0, enemies within 2 blocks", "around your target will take damage.", "&9This stat only applies to Blunt weapons."}                );
    }
}
