package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.annotation.HasCategory;
import net.Indyuce.mmoitems.stat.annotation.VersionDependant;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.NotNull;

@VersionDependant(version = {1, 21, 2})
public class EquippableSlot extends StringStat implements GemStoneStat {
    public EquippableSlot() {
        super("EQUIPPABLE_SLOT", Material.LEATHER_LEGGINGS, "装备槽位",
                new String[]{"可以装备物品的槽位。", "支持的值为 FEET、LEGS BODY、HEAD、HAND、OFFHAND", "仅在 MC 1.21.2+ 上可用"}, new String[0]);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        EquippableComponent comp = item.getMeta().getEquippable();
        EquipmentSlot slot = MMOUtils.friendlyValueOf(EquipmentSlot::valueOf, data.getString(), "找不到 ID 为 '%s' 的槽位");
        comp.setSlot(slot);
        item.getMeta().setEquippable(comp);
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        ItemMeta meta = mmoitem.getNBT().getItem().getItemMeta();
        if (!meta.hasEquippable()) return;

        EquippableComponent comp = mmoitem.getNBT().getItem().getItemMeta().getEquippable();
        mmoitem.setData(this, new StringData(comp.getSlot().name()));
    }
}
