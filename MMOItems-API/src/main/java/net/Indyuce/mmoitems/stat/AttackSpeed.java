package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.annotation.HasCategory;
import net.Indyuce.mmoitems.stat.type.WeaponBaseStat;
import org.bukkit.Material;

@HasCategory(cat = "vanilla_attribute")
public class AttackSpeed extends WeaponBaseStat {
    public AttackSpeed() {
        super("ATTACK_SPEED",
                Material.LIGHT_GRAY_DYE,
                "攻击速度",
                new String[]{"武器的攻击速度 攻击/秒"});
    }
}
