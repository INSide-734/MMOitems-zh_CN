package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.version.Sounds;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.annotation.VersionDependant;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

@VersionDependant(version = {1, 20, 5})
public class MaxItemDamage extends DoubleStat implements GemStoneStat, ItemRestriction {
    public MaxItemDamage() {
        super("MAX_ITEM_DAMAGE", Material.DAMAGED_ANVIL, "物品耐久（原版）", new String[]{"仅适用于 1.20.5+ 物品的最大耐久。使用原版耐久实现（比自定义耐久稳定得多）"}, new String[]{"all"});
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {
        Validate.isTrue(item.getMeta() instanceof Damageable, "物品不会损坏");

        final int value = (int) data.getValue();
        Validate.isTrue(value > 0, "最大耐久性必须为正值");

        // Edit meta
        ((Damageable) item.getMeta()).setMaxDamage((int) data.getValue());

        // Save in NBT
        item.addItemTag(getAppliedNBT(data));
    }

    @Override
    public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull DoubleData
            currentData, @NotNull NumericStatFormula templateData) throws IllegalArgumentException {
        whenApplied(item, currentData);
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull DoubleData data) {

        // Make new ArrayList
        ArrayList<ItemTag> ret = new ArrayList<>();

        // Add Integer
        ret.add(new ItemTag(getNBTPath(), (int) data.getValue()));

        // Return thay
        return ret;
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Get Relevant tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.INTEGER));

        // Attempt to build data
        StatData data = getLoadedNBT(relevantTags);

        // Success?
        if (data != null) {
            mmoitem.setData(this, data);
        }
    }

    @Nullable
    @Override
    public DoubleData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // Find Tag
        ItemTag cmd = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found?
        if (cmd != null) {

            // Well thats it
            return new DoubleData((Integer) cmd.getValue());
        }

        return null;
    }

    @Override
    public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
        if (item.getItem().getItemMeta() instanceof Damageable) {
            Damageable meta = ((Damageable) item.getItem().getItemMeta());
            int maxDamage = meta.hasMaxDamage() ? meta.getMaxDamage() : item.getItem().getType().getMaxDurability();

            // Some "non-damageable" item metas appear to be Damageable in recent versions
            if (maxDamage == 0) return true;

            if (meta.getDamage() >= maxDamage) {
                if (message) {
                    Message.ZERO_DURABILITY.format(ChatColor.RED).send(player.getPlayer());
                    player.getPlayer().playSound(player.getPlayer().getLocation(), Sounds.ENTITY_VILLAGER_NO, 1, 1.5f);
                }
                return false;
            }
        }
        return true;
    }
}
