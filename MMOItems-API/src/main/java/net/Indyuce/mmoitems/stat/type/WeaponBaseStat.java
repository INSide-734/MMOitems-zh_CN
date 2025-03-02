package net.Indyuce.mmoitems.stat.type;

import org.bukkit.Material;

/**
 * A special case of stats which should take off the stat base value
 * from the player's final stat value. Some examples:
 * - attack damage (base value/bare hands is 1-2)
 * - attack speed (base value/bare hands is 4)
 * - blunt power
 * - blunt force
 *
 * @author jules
 */
public abstract class WeaponBaseStat extends DoubleStat {
    public WeaponBaseStat(String id, Material mat, String name, String[] lore) {
        super(id, mat, name, lore, new String[]{"weapon", "gem_stone"});
    }
}
