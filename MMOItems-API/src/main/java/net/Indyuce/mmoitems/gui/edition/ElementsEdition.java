package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.data.random.RandomElementListData;
import net.Indyuce.mmoitems.util.ElementStatType;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ElementsEdition extends EditionInventory {
    private final List<Element> elements = new ArrayList<>();
    private final int maxPage;
    private final Map<Integer, Pair<Element, ElementStatType>> editableStats = new HashMap<>();

    private int page = 1;

    private static final int[] INIT_SLOTS = {19, 28, 37};
    private static final int ELEMENTS_PER_PAGE = 3;

    public ElementsEdition(Navigator navigator, MMOItemTemplate template) {
        super(navigator, template);

        elements.addAll(MythicLib.plugin.getElements().getAll());
        maxPage = 1 + (MythicLib.plugin.getElements().getAll().size() - 1) / ELEMENTS_PER_PAGE;
    }

    @Override
    public String getName() {
        return "元素: " + template.getId();
    }

    @Override
    public void arrangeInventory() {
        final Optional<RandomElementListData> statData = getEventualStatData(ItemStats.ELEMENTS);

        ItemStack prevPage = new ItemStack(Material.ARROW);
        ItemMeta prevPageMeta = prevPage.getItemMeta();
        prevPageMeta.setDisplayName(ChatColor.GREEN + "上一页");
        prevPage.setItemMeta(prevPageMeta);
        inventory.setItem(25, prevPage);

        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nextPageMeta = nextPage.getItemMeta();
        nextPageMeta.setDisplayName(ChatColor.GREEN + "下一页");
        nextPage.setItemMeta(nextPageMeta);
        inventory.setItem(43, nextPage);

        editableStats.clear();

        final int startingIndex = (page - 1) * ELEMENTS_PER_PAGE;
        for (int i = 0; i < ELEMENTS_PER_PAGE; i++) {
            final int index = startingIndex + i;
            if (index >= elements.size())
                break;

            Element element = elements.get(index);
            int k = 0;
            for (ElementStatType statType : ElementStatType.values()) {
                ItemStack statItem = new ItemStack(element.getIcon());
                ItemMeta statMeta = statItem.getItemMeta();
                statMeta.setDisplayName(ChatColor.GREEN + element.getName() + " " + statType.getName());
                List<String> statLore = new ArrayList<>();
                statLore.add(ChatColor.GRAY + "当前值: " + ChatColor.GREEN +
                        (statData.isPresent() && statData.get().hasStat(element, statType)
                                ? statData.get().getStat(element, statType)
                                : "---"));
                statLore.add("");
                statLore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击更改此值");
                statLore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击可删除该值");
                statMeta.setLore(statLore);
                statItem.setItemMeta(statMeta);

                final int slot = INIT_SLOTS[i] + k;
                inventory.setItem(slot, statItem);
                editableStats.put(slot, Pair.of(element, statType));
                k++;
            }
        }
    }

    @Override
    public void whenClicked(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        event.setCancelled(true);
        if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
            return;

        if (page > 1 && item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "上一页")) {
            page--;
            refreshInventory();
            return;
        }

        if (page < maxPage && item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "下一页")) {
            page++;
            refreshInventory();
            return;
        }

        Pair<Element, ElementStatType> edited = editableStats.get(event.getSlot());
        if (edited == null)
            return;

        final String elementPath = edited.getValue().getConcatenatedConfigPath(edited.getKey());

        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(this, ItemStats.ELEMENTS, elementPath).enable("输入您想要的值.");

        else if (event.getAction() == InventoryAction.PICKUP_HALF) {
            getEditedSection().set("element." + elementPath, null);

            // Clear element config section
            String elementName = edited.getKey().getId();
            if (getEditedSection().contains("element." + elementName)
                    && getEditedSection().getConfigurationSection("element." + elementName).getKeys(false).isEmpty()) {
                getEditedSection().set("element." + elementName, null);
                if (getEditedSection().getConfigurationSection("element").getKeys(false).isEmpty())
                    getEditedSection().set("element", null);
            }

            registerTemplateEdition();
            player.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + edited.getKey().getName() + " " + edited.getValue().getName() + ChatColor.GRAY + " 成功删除");
        }
    }
}