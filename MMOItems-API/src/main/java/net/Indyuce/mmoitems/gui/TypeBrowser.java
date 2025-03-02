package net.Indyuce.mmoitems.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.util.AdventureUtils;
import io.lumine.mythic.lib.version.VersionUtils;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class TypeBrowser extends MMOItemsInventory {
    private final List<Type> itemTypes;

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private static final NamespacedKey TYPE_ID_KEY = new NamespacedKey(MMOItems.plugin, "type_id");
    private static final String CUSTOM_RP_DOWNLOAD_LINK = "https://www.dropbox.com/s/90w9pvdbfeyxu94/MICustomBlockPack.zip?dl=1";

    public TypeBrowser(Navigator navigator) {
        super(navigator);

        this.itemTypes = MMOItems.plugin.getTypes().getAll().stream().filter(Type::isDisplayed).collect(Collectors.toList());
    }

    public static TypeBrowser of(Player player) {
        return new TypeBrowser(new Navigator(player));
    }

    @Override
    public Inventory getInventory() {
        int[] usedSlots = SLOTS;
        int min = (page - 1) * usedSlots.length;
        int max = page * usedSlots.length;
        int n = 0;

        // Create inventory
        Inventory inv = Bukkit.createInventory(null, 54, "Type Browser");

        // Fetch the list of types
        for (int j = min; j < Math.min(max, itemTypes.size()); j++) {

            // Current type to display into the GUI
            Type currentType = itemTypes.get(j);

            // Get number of items
            int maxStackSize = MythicLib.plugin.getVersion().isAbove(1, 20, 5) ? 99 : 64;
            int items = MMOItems.plugin.getTemplates().getTemplates(currentType).size();

            // Display how many items are in the type
            final ItemStack item = currentType.getItem();
            item.setAmount(Math.max(1, Math.min(maxStackSize, items)));
            ItemMeta meta = item.getItemMeta();
            if (MythicLib.plugin.getVersion().isAbove(1, 20, 5)) {
                VersionUtils.addEmptyAttributeModifier(meta);
                meta.setMaxStackSize(maxStackSize);
            }
            AdventureUtils.setDisplayName(meta, String.format("&a%s&8 (click to browse)", currentType.getName()));
            meta.addItemFlags(ItemFlag.values());
            List<String> lore = new ArrayList<>();
            lore.add(String.format("&7&oThere %s %s &7&oitem%s in this type.", items == 1 ? "is" : "are", items < 1 ? "&c&ono" : "&6&o" + items, items == 1 ? "" : "s"));
            AdventureUtils.setLore(meta, lore);
            meta.getPersistentDataContainer().set(TYPE_ID_KEY, PersistentDataType.STRING, currentType.getId());
            item.setItemMeta(meta);

            // Set item
            inv.setItem(SLOTS[n++], item);
        }

        // Fill remainder slots with 'No Type' notice
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(ChatColor.RED + "- No type -");
        glass.setItemMeta(glassMeta);

        // Next Page
        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
        next.setItemMeta(nextMeta);

        // Previous Page
        ItemStack previous = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
        previous.setItemMeta(previousMeta);

        // Fill
        while (n < SLOTS.length) {
            inv.setItem(SLOTS[n++], glass);
        }
        inv.setItem(18, page > 1 ? previous : null);
        inv.setItem(26, max >= MMOItems.plugin.getTypes().getAll().size() ? null : next);

        // Done
        return inv;
    }

    @Override
    public void whenClicked(InventoryClickEvent event) {

        event.setCancelled(true);
        if (event.getInventory() != event.getClickedInventory())
            return;

        ItemStack item = event.getCurrentItem();
        if (MMOUtils.isMetaItem(item, false)) {
            if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
                page++;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Previous Page")) {
                page--;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Download Default Resourcepack")) {
                MythicLib.plugin.getVersion().getWrapper().sendJson(getPlayer(),
                        "[{\"text\":\"Click to download!\",\"color\":\"green\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + CUSTOM_RP_DOWNLOAD_LINK + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":[\"\",{\"text\":\"Click to download via Dropbox\",\"italic\":true,\"color\":\"white\"}]}}]");
                getPlayer().closeInventory();
            }

            final String typeId = item.getItemMeta().getPersistentDataContainer().get(TYPE_ID_KEY, PersistentDataType.STRING);
            if (typeId == null || typeId.isEmpty()) return;

            new ItemBrowser(getNavigator(), MMOItems.plugin.getTypes().get(typeId)).open();
        }
    }
}

