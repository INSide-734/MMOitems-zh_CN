package net.Indyuce.mmoitems.gui.edition;

import io.lumine.mythic.lib.api.util.ItemFactory;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.util.MMOItemReforger;
import net.Indyuce.mmoitems.stat.RevisionID;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Inventory displayed when enabling the item updater.
 * @see RevisionID
 * @see MMOItemReforger
 *
 * @author Gunging
 */
public class RevisionInventory extends EditionInventory {

    static ItemStack name;
    static ItemStack lore;
    static ItemStack enchantments;
    static ItemStack upgrades;
    static ItemStack gemstones;
    static ItemStack soulbind;
    static ItemStack external;

    static ItemStack revisionID;

    static final String REVISION = "\u00a76修改 ID";

    public RevisionInventory(@NotNull Navigator navigator, @NotNull MMOItemTemplate template) {
        super(navigator, template);

        // If null
        if (revisionID == null) {

            name = ItemFactory.of(Material.NAME_TAG).name("\u00a73名称").lore(SilentNumbers.chop(
                    "旧物品的显示名称将转移到新物品"
                    , 40, "\u00a77")).build();

            lore = ItemFactory.of(Material.WRITABLE_BOOK).name("\u00a7dLore 标注").lore(SilentNumbers.chop(
                    "特别保留以 \u00a7n&7 颜色代码开头的 Lore 标注行"
                    , 40, "\u00a77")).build();

            enchantments = ItemFactory.of(Material.EXPERIENCE_BOTTLE).name("\u00a7b附魔").lore(SilentNumbers.chop(
                    "这保留了升级中未考虑的特殊附魔或宝石 (可能是由玩家添加的 ) "
                    , 40, "\u00a77")).build();

            upgrades = ItemFactory.of(Material.NETHER_STAR).name("\u00a7a升级").lore(SilentNumbers.chop(
                    "更新后该物品会保留升级等级吗？仅保留升级级别 (只要不超过新的最大值 ) "
                    , 40, "\u00a77")).build();

            gemstones = ItemFactory.of(Material.EMERALD).name("\u00a7e宝石").lore(SilentNumbers.chop(
                    "更新时该物品会保留其宝石吗？  (请注意, 这会允许宝石溢出, 即使您减少了宝石插槽, 也会保留所有旧宝石 ) "
                    , 40, "\u00a77")).build();

            soulbind = ItemFactory.of(Material.ENDER_EYE).name("\u00a7c灵魂绑定").lore(SilentNumbers.chop(
                    "如果旧物品是灵魂绑定的, 更新会将灵魂绑定转移到新物品上"
                    , 40, "\u00a77")).build();

            external = ItemFactory.of(Material.SPRUCE_SIGN).name("\u00a79外部 SH(其他插件的兼容)").lore(SilentNumbers.chop(
                    "通过外部插件（如 GemStones 但不可删除）注册到项目的 StatHistory 上的数据"
                    , 40, "\u00a77")).build();


            // Fill stack
            revisionID = ItemFactory.of(Material.ITEM_FRAME).name(REVISION).lore(SilentNumbers.chop(
                    "更新器始终处于激活状态，增加该数字将更新该 MMOItem 的物品所有属性，无需进一步操作."
                    , 40, "\u00a77")).build();
        }
    }

    @Override
    public String getName() {
        return "修改管理器";
    }

    @Override
    public void arrangeInventory() {
        // Place corresponding item stacks in there
        for (int i = 0; i < inventory.getSize(); i++) {

            // What item to even put here
            ItemStack which = null;
            Boolean enable = null;
            Integer id = null;
            switch (i) {
                case 19:
                    which = name.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepName();
                    break;
                case 20:
                    which = lore.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepLore();
                    break;
                case 21:
                    which = enchantments.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepEnchantments();
                    break;
                case 22:
                    which = external.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepExternalSH();
                    break;
                case 28:
                    which = upgrades.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepUpgrades();
                    break;
                case 29:
                    which = gemstones.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepGemStones();
                    break;
                case 30:
                    which = soulbind.clone();
                    enable = MMOItems.plugin.getLanguage().revisionOptions.shouldKeepSoulBind();
                    break;
                case 33:
                    id = getEditedSection().getInt(ItemStats.REVISION_ID.getPath(), 1);
                    which = revisionID.clone();
                    break;
                default:
                    break;
            }

            // If an item corresponds to this slot
            if (which != null) {

                // If it is defined to be enabled
                if (enable != null) {

                    // Add mentioning if enabled
                    inventory.setItem(i, addLore(which, "", "\u00a78启用 (在配置中)? \u00a76" + enable.toString()));

                // If ID is enabled
                } else if (id != null) {

                    // Add mentioning if enabled
                    inventory.setItem(i, addLore(which, "", "\u00a78当前值: \u00a76" + id));

                // Neither enable nor ID are defined
                } else {

                    // Add
                    inventory.setItem(i, which);
                }
            }
        }
    }

    @Override
    public void whenClicked(InventoryClickEvent event) {
        // Get the clicked item
        ItemStack item = event.getCurrentItem();
        event.setCancelled(true);

        // If the click did not happen in the correct inventory, or the item is not clickable
        if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false)) { return; }

        // Is the player clicking the revision ID thing?
        if (item.getItemMeta().getDisplayName().equals(REVISION)) {

            // Get the current ID
            int id = getEditedSection().getInt(ItemStats.REVISION_ID.getPath(), 1);

            // If right click
            if (event.getAction() == InventoryAction.PICKUP_HALF) {

                // Decrease by 1, but never before 1
                id = Math.max(id - 1, 1);

            // Any other click
            } else {

                // Increase by 1 until the ultimate maximum
                id = Math.min(id + 1, Integer.MAX_VALUE);

            }

            // Register edition
            getEditedSection().set(ItemStats.REVISION_ID.getPath(), id);
            registerTemplateEdition();

            // Update ig
            event.setCurrentItem(addLore(revisionID.clone(), "", "\u00a78当前物品ID值: \u00a76" + id));
        }
    }

    @NotNull ItemStack addLore(@NotNull ItemStack iSource, String... extraLines){

        // Get its lore
        ArrayList<String> iLore = new ArrayList<>();
        ItemMeta iMeta = iSource.getItemMeta();
        if (iMeta != null && iMeta.getLore() != null) {iLore = new ArrayList<>(iMeta.getLore()); }

        // Add lines
        iLore.addAll(Arrays.asList(extraLines));

        // Put lore
        iMeta.setLore(iLore);

        // Yes
        iSource.setItemMeta(iMeta);

        // Yes
        return iSource;
    }
}
