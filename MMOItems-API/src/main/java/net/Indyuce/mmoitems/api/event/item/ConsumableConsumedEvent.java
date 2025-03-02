package net.Indyuce.mmoitems.api.event.item;

import net.Indyuce.mmoitems.api.event.PlayerDataEvent;
import net.Indyuce.mmoitems.api.interaction.Consumable;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * When a player eats a consumable
 *
 * @author Gunging
 */
public class ConsumableConsumedEvent extends PlayerDataEvent {
    @NotNull
    private final VolatileMMOItem mmoitem;
    @NotNull
    private final Consumable useItem;

    @Nullable
    private Boolean consumed = true;

    private static final HandlerList handlers = new HandlerList();

    public ConsumableConsumedEvent(@NotNull PlayerData player, @NotNull VolatileMMOItem mmo, @NotNull Consumable useItem) {
        super(player);

        this.mmoitem = mmo;
        this.useItem = useItem;
    }

    @Deprecated
    public ConsumableConsumedEvent(VolatileMMOItem mmo, Player player, Consumable useItem) {
        this(PlayerData.get(player), mmo, useItem);
    }

    @NotNull
    public VolatileMMOItem getMMOItem() {
        return mmoitem;
    }

    @NotNull
    public Consumable getUseItem() {
        return useItem;
    }

    public Boolean isConsume() {
        return consumed;
    }

    public boolean isConsume(boolean defaultValue) {
        return consumed != null ? consumed : defaultValue;
    }

    public void setConsume(@Nullable Boolean consume) {
        this.consumed = consume;
    }

    @Deprecated
    public boolean isConsumed() {
        return consumed;
    }

    @Deprecated
    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
