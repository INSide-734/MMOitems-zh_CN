package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.WeaponBaseStat;
import org.bukkit.Material;

public class BluntRating extends WeaponBaseStat {
    public BluntRating() {
        super("BLUNT_RATING",
                Material.BRICK,
                "Blunt Rating",
                new String[]{"The force of the blunt attack.", "If set to 50%, enemies hit by the attack", "will take 50% of the initial damage.", "&9This stat only applies to Blunt weapons."});
    }
}
