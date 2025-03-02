package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.element.Element;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.ElementsEdition;
import net.Indyuce.mmoitems.stat.data.ElementListData;
import net.Indyuce.mmoitems.stat.data.random.RandomElementListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.Previewable;
import net.Indyuce.mmoitems.util.ElementStatType;
import net.Indyuce.mmoitems.util.Pair;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Elements extends ItemStat<RandomElementListData, ElementListData> implements Previewable<RandomElementListData, ElementListData> {
    public Elements() {
        super("ELEMENT", Material.SLIME_BALL, "元素", new String[]{"您的物品的元素"},
                new String[]{"equipment", "ornament", "gem_stone"});
    }

    @Override
    public RandomElementListData whenInitialized(Object object) {
        Validate.isTrue(object instanceof ConfigurationSection, "必须指定配置部分");
        return new RandomElementListData((ConfigurationSection) object);
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new ElementsEdition(inv.getNavigator(), inv.getEdited()).open(inv);

        if (event.getAction() == InventoryAction.PICKUP_HALF)
            if (inv.getEditedSection().contains("element")) {
                inv.getEditedSection().set("element", null);
                inv.registerTemplateEdition();
                inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "元素已成功删除");
            }
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        String elementPath = info[0].toString();

        NumericStatFormula formula = new NumericStatFormula(message);
        formula.fillConfigurationSection(inv.getEditedSection(), "element." + elementPath);

        // clear element config section
        String elementName = elementPath.split("\\.")[0];
        if (inv.getEditedSection().contains("element")) {
            if (inv.getEditedSection().getConfigurationSection("element").contains(elementName)
                    && inv.getEditedSection().getConfigurationSection("element." + elementName).getKeys(false).isEmpty())
                inv.getEditedSection().set("element." + elementName, null);
            if (inv.getEditedSection().getConfigurationSection("element").getKeys(false).isEmpty())
                inv.getEditedSection().set("element", null);
        }

        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + UtilityMethods.caseOnWords(elementPath.replace(".", " ")) + ChatColor.GRAY
                + " 成功更改为 " + ChatColor.GOLD + formula + ChatColor.GRAY + ".");
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<RandomElementListData> statData) {

        if (statData.isPresent()) {
            lore.add(ChatColor.GRAY + "当前值: ");
            RandomElementListData data = statData.get();
            data.getKeys().forEach(key -> lore.add(ChatColor.GRAY + "* " + key.getKey().getName() + " " + key.getValue().getName() + ": " + ChatColor.RED + data.getStat(key.getKey(), key.getValue())));

        } else
            lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "None");

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 单击可访问元素编辑菜单");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击删除所有元素");
    }

    @NotNull
    @Override
    public ElementListData getClearStatData() {
        return new ElementListData();
    }

    private Map<ElementStatType, String> statFormat = new HashMap<>();

    @Override
    public void loadConfiguration(@NotNull ConfigurationSection legacyLanguageFile, @NotNull Object configObject) {

        // LEGACY CODE
        if (configObject instanceof String) {
            for (ElementStatType statType : ElementStatType.values())
                statFormat.put(statType, legacyLanguageFile.getString("elemental-" + statType.lowerCaseName(), "<TranslationNotFound:" + statType.name().toLowerCase() + ">"));
        }

        // Up-to-date code
        else {
            Validate.isTrue(configObject instanceof ConfigurationSection, "Must be a config section");
            final ConfigurationSection config = (ConfigurationSection) configObject;
            for (ElementStatType statType : ElementStatType.values())
                statFormat.put(statType, config.getString(statType.lowerCaseName(), "<TranslationNotFound:" + statType.lowerCaseName() + ">"));
        }
    }

    @Override
    public String getLegacyTranslationPath() {
        // Arbitrary
        return "elemental-damage";
    }

    @NotNull
    private String formatForLore(Element element, ElementStatType statType) {
        return statFormat.get(statType)
                .replace("{color}", element.getColor())
                .replace("{icon}", element.getLoreIcon())
                .replace("{element}", element.getName());
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull ElementListData data) {

        List<String> lore = new ArrayList<>();

        // Write Lore
        for (Pair<Element, ElementStatType> pair : data.getKeys()) {
            final String format = formatForLore(pair.getKey(), pair.getValue());
            final double value = data.getStat(pair.getKey(), pair.getValue());
            lore.add(DoubleStat.formatPath("ELEMENTAL_STAT", format, true, value));
        }

        // Insert non-empty lore
        if (!lore.isEmpty())
            item.getLore().insert("elements", lore);

        // Addtags
        item.addItemTag(getAppliedNBT(data));
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull ElementListData data) {

        // Create Array
        ArrayList<ItemTag> ret = new ArrayList<>();
        for (Pair<Element, ElementStatType> pair : data.getKeys())
            ret.add(new ItemTag("MMOITEMS_" + pair.getValue().getConcatenatedTagPath(pair.getKey()), data.getStat(pair.getKey(), pair.getValue())));

        // Thats it
        return ret;
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Seek the relevant tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        for (Element element : Element.values())
            for (ElementStatType statType : ElementStatType.values()) {
                final String path = "MMOITEMS_" + statType.getConcatenatedTagPath(element);
                if (mmoitem.getNBT().hasTag(path))
                    relevantTags.add(ItemTag.getTagAtPath(path, mmoitem.getNBT(), SupportedNBTTagValues.DOUBLE));
            }

        // Generate Data
        StatData data = getLoadedNBT(relevantTags);

        // Found?
        if (data != null) {
            mmoitem.setData(this, data);
        }
    }

    @Nullable
    @Override
    public ElementListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // Create new
        ElementListData elements = new ElementListData();

        // Try to find every existing element
        for (Element element : Element.values())
            for (ElementStatType statType : ElementStatType.values()) {
                final String path = "MMOITEMS_" + statType.getConcatenatedTagPath(element);
                ItemTag tag = ItemTag.getTagAtPath(path, storedTags);
                if (tag != null)
                    elements.setStat(element, statType, (double) tag.getValue());
            }

        return elements.isEmpty() ? null : elements;
    }

    @Override
    public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull ElementListData currentData, @NotNull RandomElementListData templateData) throws IllegalArgumentException {
        Validate.isTrue(currentData instanceof ElementListData, "当前数据不是 ElementListData");
        Validate.isTrue(templateData instanceof RandomElementListData, "模板数据不是 RandomElementListData");

        List<String> elementLore = new ArrayList<>();

        // Examine every element stat possible
        for (Element element : Element.values())
            for (ElementStatType statType : ElementStatType.values()) {

                NumericStatFormula nsf = templateData.getStat(element, statType);

                // Get Value
                final double techMinimum = nsf.calculate(0, NumericStatFormula.FormulaInputType.LOWER_BOUND);
                final double techMaximum = nsf.calculate(0, NumericStatFormula.FormulaInputType.UPPER_BOUND);

                // Display if not ZERO
                if (techMinimum != 0 || techMaximum != 0) {
                    final String builtRange = DoubleStat.formatPath(statType.getConcatenatedTagPath(element), formatForLore(element, statType), true, techMinimum, techMaximum);
                    elementLore.add(builtRange);
                }
            }

        if (!elementLore.isEmpty()) item.getLore().insert("elements", elementLore);

        // Add tags
        item.addItemTag(getAppliedNBT(currentData));
    }
}
