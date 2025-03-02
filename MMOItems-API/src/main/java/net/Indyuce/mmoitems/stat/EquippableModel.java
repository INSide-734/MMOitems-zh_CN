package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.annotation.VersionDependant;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.NotNull;

@VersionDependant(version = {1, 21, 2})
public class EquippableModel extends StringStat implements GemStoneStat {
    public EquippableModel() {
        super("EQUIPPABLE_MODEL", Material.LEATHER_CHESTPLATE, "Equippable Model",
                new String[]{"Name-spaced key of model used when the item is equipped.", "Available only on MC 1.21.2+"}, new String[0]);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        EquippableComponent comp = item.getMeta().getEquippable();
        comp.setModel(NamespacedKey.fromString(data.getString()));
        item.getMeta().setEquippable(comp);
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        ItemMeta meta = mmoitem.getNBT().getItem().getItemMeta();
        if (!meta.hasEquippable()) return;

        EquippableComponent comp = mmoitem.getNBT().getItem().getItemMeta().getEquippable();
        mmoitem.setData(this, new StringData(comp.getModel().toString()));
    }
}
