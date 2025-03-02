package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.version.VersionUtils;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.ItemBrowser;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemEdition extends EditionInventory {
    private static final int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private static final NamespacedKey STAT_ID_KEY = new NamespacedKey(MMOItems.plugin, "StatId");

    public ItemEdition(Navigator navigator, MMOItemTemplate template) {
        super(navigator, template);
    }

    public static ItemEdition of(Player player, MMOItemTemplate template) {
        ItemBrowser browser = ItemBrowser.of(player, template.getType());
        return new ItemEdition(browser.getNavigator(), template);
    }


    @Override
    public String getName() {
        return "编辑物品: " + getEdited().getId();
    }

    @Override
    public void arrangeInventory() {
        int min = (page - 1) * slots.length;
        int max = page * slots.length;
        int n = 0;

        /*
         * it has to determine what stats can be applied first because otherwise
         * the for loop will just let some slots empty
         */
        List<ItemStat> appliable = new ArrayList<>(getEdited().getType().getAvailableStats()).stream()
                .filter(stat -> stat.hasValidMaterial(getCachedItem()) && !(stat instanceof InternalStat)).collect(Collectors.toList());

        for (int j = min; j < Math.min(appliable.size(), max); j++) {
            ItemStat stat = appliable.get(j);
            ItemStack item = new ItemStack(stat.getDisplayMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.values());
            VersionUtils.addEmptyAttributeModifier(meta);
            meta.setDisplayName(ChatColor.GREEN + stat.getName());
            List<String> lore = MythicLib.plugin.parseColors(Arrays.stream(stat.getLore()).map(s -> ChatColor.GRAY + s).collect(Collectors.toList()));
            lore.add("");
            if (stat.getCategory() != null) {
                lore.add(0, "");
                lore.add(0, ChatColor.BLUE + stat.getCategory().getLoreTag());
            }

            stat.whenDisplayed(lore, getEventualStatData(stat));

            meta.getPersistentDataContainer().set(STAT_ID_KEY, PersistentDataType.STRING, stat.getId());
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(slots[n++], item);
        }

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(ChatColor.RED + "- 无物品编号 -");
        glass.setItemMeta(glassMeta);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "下一页");
        next.setItemMeta(nextMeta);

        ItemStack previous = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName(ChatColor.GREEN + "上一页");
        previous.setItemMeta(previousMeta);

        while (n < slots.length)
            inventory.setItem(slots[n++], glass);
        inventory.setItem(27, page > 1 ? previous : null);
        inventory.setItem(35, appliable.size() > max ? next : null);
    }

    @Override
    public void whenClicked(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getInventory() != event.getClickedInventory())
            return;

        ItemStack item = event.getCurrentItem();
        if (!MMOUtils.isMetaItem(item, false) || event.getInventory().getItem(4) == null)
            return;

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "下一页")) {
            page++;
            refreshInventory();
        }

        if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "上一页")) {
            page--;
            refreshInventory();
        }

        final String tag = item.getItemMeta().getPersistentDataContainer().get(STAT_ID_KEY, PersistentDataType.STRING);
        if (tag == null || tag.isEmpty()) return;

        // Check for OP stats
        final ItemStat edited = MMOItems.plugin.getStats().get(tag);
        if (MMOItems.plugin.hasPermissions() && MMOItems.plugin.getLanguage().opStatsEnabled
                && MMOItems.plugin.getLanguage().opStats.contains(edited.getId())
                && !MMOItems.plugin.getVault().getPermissions().has((Player) event.getWhoClicked(), "mmoitems.edit.op")) {
            event.getWhoClicked().sendMessage(ChatColor.RED + "您没有编辑此属性数据的权限.");
            return;
        }

        edited.whenClicked(this, event);
    }

    @Deprecated
    public ItemEdition onPage(int value) {
        page = value;
        return this;
    }
}