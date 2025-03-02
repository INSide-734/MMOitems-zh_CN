package net.Indyuce.mmoitems.api.event.item;

import net.Indyuce.mmoitems.api.interaction.util.VanillaDurabilityItem;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class VanillaDurabilityDamage extends PlayerItemDamageEvent {
    private final VanillaDurabilityItem sourceItem;

    public VanillaDurabilityDamage(@NotNull VanillaDurabilityItem item, int impendingDamage) {
        super(Objects.requireNonNull(item.getPlayer()), item.getNBTItem().getItem(), impendingDamage, impendingDamage);

        sourceItem = item;
    }

    @NotNull
    public VanillaDurabilityItem getSourceItem() {
        return sourceItem;
    }
}
