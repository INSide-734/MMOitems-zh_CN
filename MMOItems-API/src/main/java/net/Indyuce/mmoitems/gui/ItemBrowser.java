package net.Indyuce.mmoitems.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.gui.Navigator;
import io.lumine.mythic.lib.version.Sounds;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.NewItemEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import net.Indyuce.mmoitems.stat.BrowserDisplayIDX;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemBrowser extends MMOItemsInventory {
    private final Map<String, ItemStack> cached = new LinkedHashMap<>();

    @NotNull
    private final Type type;
    private boolean deleteMode;

    // Slots used to display items based on the item type explored
    private static final int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private static final int[] slotsAlt = {1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    public ItemBrowser(Navigator navigator, Type type) {
        super(navigator);

        this.type = type;
    }

    public static ItemBrowser of(Player player, Type type) {
        TypeBrowser prev = TypeBrowser.of(player);
        return new ItemBrowser(prev.getNavigator(), type);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 54, (deleteMode ? "删除模式: " : "物品类型浏览: ") + MythicLib.plugin.getAdventureParser().stripColors(type.getName()));

        /*
         * Build cool Item Stacks for buttons and sh
         */
        ItemStack error = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta errorMeta = error.getItemMeta();
        errorMeta.setDisplayName(ChatColor.RED + "- 错误 -");
        List<String> errorLore = new ArrayList<>();
        errorLore.add("\u00a7\u00a7o 发生错误");
        errorLore.add("\u00a7\u00a7o 尝试生成该物品");
        errorMeta.setLore(errorLore);
        error.setItemMeta(errorMeta);

        ItemStack noItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta noItemMeta = noItem.getItemMeta();
        noItemMeta.setDisplayName(ChatColor.RED + "- 没有物品 -");
        noItem.setItemMeta(noItemMeta);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "下一页");
        next.setItemMeta(nextMeta);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GREEN + AltChar.rightArrow + "返回");
        back.setItemMeta(backMeta);

        ItemStack create = new ItemStack(new ItemStack(Material.WRITABLE_BOOK));
        ItemMeta createMeta = create.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "创建");
        create.setItemMeta(createMeta);

        ItemStack delete = new ItemStack(new ItemStack(Material.CAULDRON));
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.RED + (deleteMode ? "取消删除" : "删除物品"));
        delete.setItemMeta(deleteMeta);

        ItemStack previous = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName(ChatColor.GREEN + "上一页");
        previous.setItemMeta(previousMeta);

        if (type == Type.BLOCK) {
            ItemStack downloadPack = new ItemStack(Material.HOPPER);
            ItemMeta downloadMeta = downloadPack.getItemMeta();
            downloadMeta.setDisplayName(ChatColor.GREEN + "下载默认资源包");
            downloadMeta.setLore(Arrays.asList(ChatColor.LIGHT_PURPLE + "只看到石头？", "",
                    ChatColor.RED + "通过下载默认资源包, 您可以", ChatColor.RED + "根据需要编辑块",
                    ChatColor.RED + "您仍然需要将其添加到您的服务器！"));
            downloadPack.setItemMeta(downloadMeta);
            inv.setItem(45, downloadPack);
        }

        // Get templates of this type
        HashMap<Double, ArrayList<MMOItemTemplate>> templates = BrowserDisplayIDX.select(MMOItems.plugin.getTemplates().getTemplates(type));

        /*
         *  -----------
         *    CALCULATE GUI BOUNDS AND PAGE SIZES
         *
         *      Each display index claims the entire column of items, such that there will be
         *      empty spaces added to fill the inventories.
         *
         *      In Four GUI mode, columns are four slots tall, else they are three slots tall.
         *  -----------
         */
        int[] usedSlots = type.isFourGUIMode() ? slotsAlt : slots;
        int min = (page - 1) * usedSlots.length;
        int max = page * usedSlots.length;
        int n = 0;

        int sc = type.isFourGUIMode() ? 4 : 3;
        int totalSpaceCount = 0;

        for (Map.Entry<Double, ArrayList<MMOItemTemplate>> indexTemplates : templates.entrySet()) {

            // Claim columns
            int totalSpaceAdd = indexTemplates.getValue().size();
            while (totalSpaceAdd > 0) {
                totalSpaceCount += sc;
                totalSpaceAdd -= sc;
            }
        }

        /*
         * Over the page-range currently in use...
         */
        for (int j = min; j < Math.min(max, totalSpaceCount); j++) {
            MMOItemTemplate template = BrowserDisplayIDX.getAt(j, templates);

            // No template here?
            if (template == null) {

                // Set Item
                inv.setItem(usedSlots[n], noItem);

                /*
                 *      Calculate next n from the slots.
                 *
                 *      #1 Adding 7 will give you the slot immediately under
                 *
                 *      #2 If it overflows, subtract 7sc (column space * 7)
                 *         and add one
                 */
                n += 7;
                if (n >= usedSlots.length) {
                    n -= 7 * sc;
                    n++;
                }
                continue;
            }

            // Build item -> any errors?
            final ItemStack item = template.newBuilder(PlayerData.get(playerData).getRPG(), true).build().newBuilder().build();
            if (item == null || item.getType().isAir() || !item.getType().isItem() || item.getItemMeta() == null) {

                // Set Item
                cached.put(template.getId(), error);
                inv.setItem(usedSlots[n], error);

                /*
                 *      Calculate next n from the slots.
                 *
                 *      #1 Adding 7 will give you the slot immediately under
                 *
                 *      #2 If it overflows, subtract 7sc (column space * 7)
                 *         and add one
                 */
                n += 7;
                if (n >= usedSlots.length) {
                    n -= 7 * sc;
                    n++;
                }
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add("");

            // Deleting lore?
            if (deleteMode) {
                lore.add(ChatColor.RED + AltChar.cross + "点击删除" + AltChar.cross);
                meta.setDisplayName(ChatColor.RED + "删除:" + (meta.hasDisplayName() ? meta.getDisplayName() : MMOUtils.getDisplayName(item)));

                // Editing lore?
            } else {
                lore.add(ChatColor.YELLOW + AltChar.smallListDash + " 左键单击即可获得该物品");
                lore.add(ChatColor.YELLOW + AltChar.smallListDash + " 右键单击可编辑此物品");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            // Set item
            cached.put(template.getId(), item);
            inv.setItem(usedSlots[n], cached.get(template.getId()));

            /*
             *      Calculate next n from the slots.
             *
             *      #1 Adding 7 will give you the slot immediately under
             *
             *      #2 If it overflows, subtract 7sc (column space * 7)
             *         and add one
             */
            n += 7;
            if (n >= usedSlots.length) {
                n -= 7 * sc;
                n++;
            }
        }

        // Put the buttons
        if (!deleteMode) {
            inv.setItem(51, create);
        }
        inv.setItem(47, delete);
        inv.setItem(49, back);
        inv.setItem(18, page > 1 ? previous : null);
        inv.setItem(26, max >= totalSpaceCount ? null : next);
        for (int i : usedSlots) {
            if (SilentNumbers.isAir(inv.getItem(i))) {
                inv.setItem(i, noItem);
            }
        }
        return inv;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void whenClicked(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getInventory() != event.getClickedInventory())
            return;

        ItemStack item = event.getCurrentItem();
        if (MMOUtils.isMetaItem(item, false)) {
            if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "下一页")) {
                page++;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "上一页")) {
                page--;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.rightArrow + " 返回"))
                getNavigator().popOpen();

            else if (item.getItemMeta().getDisplayName().equals(ChatColor.RED + "取消删除")) {
                deleteMode = false;
                open();
            } else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "创建"))
                new NewItemEdition(this).enable("在聊天中输入您想要的文字");

            else if ( item.getItemMeta().getDisplayName().equals(ChatColor.RED + "删除物品")) {
                deleteMode = true;
                open();
            }
        }

        String id = NBTItem.get(item).getString("MMOITEMS_ITEM_ID");
        if (id.equals(""))
            return;

        if (deleteMode) {
            MMOItems.plugin.getTemplates().deleteTemplate(type, id);
            deleteMode = false;
            open();

        } else {
            if (event.getAction() == InventoryAction.PICKUP_ALL) {
                getPlayer().getInventory().addItem(MMOItems.plugin.getItem(type, id, PlayerData.get(player)));
                getPlayer().playSound(getPlayer().getLocation(), Sounds.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            }

            if (event.getAction() == InventoryAction.PICKUP_HALF)
                new ItemEdition(getNavigator(), MMOItems.plugin.getTemplates().getTemplate(type, id)).open();
        }
    }
}
