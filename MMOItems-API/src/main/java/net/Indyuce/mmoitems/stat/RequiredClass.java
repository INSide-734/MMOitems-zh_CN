package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class RequiredClass extends StringListStat implements ItemRestriction, GemStoneStat {
    public RequiredClass() {
        super("REQUIRED_CLASS", Material.WRITABLE_BOOK, "所需 Class",
                new String[]{"需要特定 Class才能使用物品"}, new String[]{"!block", "all"});
    }

    @Override
    @SuppressWarnings("unchecked")
    public StringListData whenInitialized(Object object) {
        Validate.isTrue(object instanceof List<?>, "必须指定一个字符串列表");
        return new StringListData((List<String>) object);
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(inv, this).enable("在聊天栏中输入您希望使用物品的 Class");

        if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().getKeys(false).contains("required-class")) {
            List<String> supportedClasses = inv.getEditedSection().getStringList("required-class");
            if (supportedClasses.size() < 1)
                return;

            String last = supportedClasses.remove(supportedClasses.size() - 1);
            inv.getEditedSection().set(getPath(), supportedClasses.size() == 0 ? null : supportedClasses);
            inv.registerTemplateEdition();
            inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "已成功删除 " + last + ".");
        }
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        List<String> lore = (inv.getEditedSection().getKeys(false).contains("required-class") ? inv.getEditedSection().getStringList("required-class")
                : new ArrayList<>());
        lore.add(message);
        inv.getEditedSection().set(getPath(), lore);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "所需 Class 已成功添加");
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Find tag
        ArrayList<ItemTag> rtags = new ArrayList<>();
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            rtags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

        // Build
        StatData data = getLoadedNBT(rtags);

        // Success?
        if (data != null) {
            mmoitem.setData(this, data);
        }
    }

    @Nullable
    @Override
    public StringListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // Get it
        ItemTag listTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found?
        if (listTag != null) {

            // Create String List Data
            return new StringListData(((String) listTag.getValue()).split(Pattern.quote(", ")));
        }

        // No correct tags
        return null;
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<StringListData> statData) {

        if (statData.isPresent()) {
            lore.add(ChatColor.GRAY + "当前值:");
            StringListData data = (StringListData) statData.get();
            data.getList().forEach(el -> lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + el));

        } else
            lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "None");

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 单击以添加 Class.");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击以删除最后一类");
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StringListData data) {

        // Make the result list
        ArrayList<ItemTag> ret = new ArrayList<>();

        // Add the Json Array
        ret.add(new ItemTag(getNBTPath(), String.join(", ", ((StringListData) data).getList())));

        // Ready.
        return ret;
    }

    @Override
    public boolean canUse(RPGPlayer player, NBTItem item, boolean message) {
        String requiredClass = item.getString(ItemStats.REQUIRED_CLASS.getNBTPath());
        if (!requiredClass.equals("") && !hasRightClass(player, requiredClass) && !player.getPlayer().hasPermission("mmoitems.bypass.class")) {
            if (message) {
                Message.WRONG_CLASS.format(ChatColor.RED).send(player.getPlayer());
                player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.5f);
            }
            return false;
        }
        return true;
    }

    private boolean hasRightClass(RPGPlayer player, String requiredClass) {
        String name = ChatColor.stripColor(player.getClassName());

        for (String found : requiredClass.split(Pattern.quote(", ")))
            if (found.equalsIgnoreCase(name))
                return true;

        return false;
    }
}
