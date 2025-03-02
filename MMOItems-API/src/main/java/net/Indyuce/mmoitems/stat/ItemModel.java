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
import org.jetbrains.annotations.NotNull;

@VersionDependant(version = {1, 21, 2})
public class ItemModel extends StringStat implements GemStoneStat {
    public ItemModel() {
        super("MODEL", Material.PAINTING, "Item Model",
                new String[]{"Model to be used to render the item. It is", "meant to replace the CustomModelData tag in 1.21.2+. It", "allows to modify the item texture, model and UI icon", "without having to use CustomModelData.", "Available only on MC 1.21.2+"}, new String[0]);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        item.getMeta().setItemModel(NamespacedKey.fromString(data.getString()));
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        ItemMeta meta = mmoitem.getNBT().getItem().getItemMeta();
        if (meta.hasItemModel()) mmoitem.setData(this, new StringData(meta.getItemModel().asString()));
    }
}
