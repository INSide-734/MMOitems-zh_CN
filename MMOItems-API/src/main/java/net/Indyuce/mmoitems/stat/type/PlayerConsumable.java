package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Self consumables just means that the player eats this
 * item itself, rather than using it on another item.
 * <br>
 * Any food is a self consumable, since you eat them to
 * restore hunger or health or whatever, while upgrading
 * consumables are not self consumables, as the are used
 * on other items and cannot be consumed by themselves.
 *
 * @author Gunging
 */
public interface PlayerConsumable {

    /**
     * Called when the item is being consumed directly by a player.
     *
     * @since 6.7 You need to specify if the item is being eaten the vanilla
     *         way or not. This is used to fix an issue where when eating through the
     *         vanilla eating animation, the default food and saturation modifier is
     *         applied so MMOItems needs to apply some offset to food/saturation.
     */
    void onConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player, boolean vanillaConsumption);
}
