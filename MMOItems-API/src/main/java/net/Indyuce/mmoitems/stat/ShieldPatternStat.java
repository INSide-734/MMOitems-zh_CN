package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ShieldPatternData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ShieldPatternStat extends ItemStat<ShieldPatternData, ShieldPatternData> {

    @BackwardsCompatibility(version = "1.21.2")
    private final Method PatternType__valueOf, PatternType__name;

    public ShieldPatternStat() {
        super("SHIELD_PATTERN", Material.SHIELD, "盾牌图案", new String[]{"修改盾牌的颜色和图案"},
                new String[0], Material.SHIELD);

        try {
            PatternType__valueOf = PatternType.class.getDeclaredMethod("valueOf", String.class);
            PatternType__name = PatternType.class.getMethod("name");
        } catch (Exception exception) {
            throw new RuntimeException("反射错误: " + exception.getMessage());
        }
    }

    private PatternType PatternType__valueOf(String str) {
        try {
            return (PatternType) PatternType__valueOf.invoke(null, str);
        } catch (Exception exception) {
            throw new RuntimeException("反射异常", exception);
        }
    }

    private String PatternType__name(PatternType type) {
        try {
            return (String) PatternType__name.invoke(type);
        } catch (Exception exception) {
            throw new RuntimeException("反射异常", exception);
        }
    }

    @Override
    public ShieldPatternData whenInitialized(Object object) {
        Validate.isTrue(object instanceof ConfigurationSection, "必须指定配置部分");
        ConfigurationSection config = (ConfigurationSection) object;

        ShieldPatternData shieldPattern = new ShieldPatternData(
                config.contains("color") ? DyeColor.valueOf(config.getString("color").toUpperCase().replace("-", "_").replace(" ", "_")) : null);

        // Apply patterns
        for (String key : config.getKeys(false))
            if (!key.equalsIgnoreCase("color")) {
                String format = config.getString(key + ".pattern").toUpperCase().replace("-", "_").replace(" ", "_");
                PatternType type = PatternType__valueOf(format);

                format = config.getString(key + ".color").toUpperCase().replace("-", "_").replace(" ", "_");
                DyeColor color = DyeColor.valueOf(format);

                shieldPattern.add(new Pattern(color, type));
            }

        return shieldPattern;
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull ShieldPatternData data) {
        BlockStateMeta meta = (BlockStateMeta) item.getMeta();
        Banner banner = (Banner) meta.getBlockState();
        banner.setBaseColor(data.getBaseColor());
        banner.setPatterns(data.getPatterns());
        ((BlockStateMeta) item.getMeta()).setBlockState(banner);
        item.getMeta().addItemFlags(HidePotionEffects.ITEM_FLAG);
    }

    /**
     * This stat is saved not as a custom tag, but as the vanilla pattenrs themselves.
     * Alas this is an empty array
     */
    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull ShieldPatternData data) {
        return new ArrayList<>();
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(inv, ItemStats.SHIELD_PATTERN, 0).enable("在聊天中输入你盾牌的颜色");

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            inv.getEditedSection().set("shield-pattern.color", null);

            inv.registerTemplateEdition();
            inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "成功重置盾牌颜色");
        }

        if (event.getAction() == InventoryAction.PICKUP_HALF)
            new StatEdition(inv, ItemStats.SHIELD_PATTERN, 1).enable("在聊天中输入您要添加的格式",
                    ChatColor.AQUA + "格式: [PATTERN_TYPE] [DYE_COLOR]");

        if (event.getAction() == InventoryAction.DROP_ONE_SLOT && inv.getEditedSection().contains("shield-pattern")) {
            Set<String> set = inv.getEditedSection().getConfigurationSection("shield-pattern").getKeys(false);
            String last = new ArrayList<>(set).get(set.size() - 1);
            if (last.equalsIgnoreCase("color"))
                return;

            inv.getEditedSection().set("shield-pattern." + last, null);
            inv.registerTemplateEdition();
            inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "成功移除最后一个格式");
        }
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        int editedStatData = (int) info[0];

        if (editedStatData == 1) {
            String[] split = message.split(" ");
            Validate.isTrue(split.length == 2, message + " 不是有效的格式 [PATTERN_TYPE] [DYE_COLOR]");

            PatternType patternType = PatternType__valueOf(split[0].toUpperCase().replace("-", "_").replace(" ", "_"));
            DyeColor dyeColor = DyeColor.valueOf(split[1].toUpperCase().replace("-", "_").replace(" ", "_"));

            int availableKey = getNextAvailableKey(inv.getEditedSection().getConfigurationSection("shield-pattern"));
            Validate.isTrue(availableKey >= 0, "单个物品上可以有 100 多个盾牌图案");

            inv.getEditedSection().set("shield-pattern." + availableKey + ".pattern", PatternType__name(patternType));
            inv.getEditedSection().set("shield-pattern." + availableKey + ".color", dyeColor.name());
            inv.registerTemplateEdition();
            inv.getPlayer().sendMessage(
                    MMOItems.plugin.getPrefix() + UtilityMethods.caseOnWords(PatternType__name(patternType).toLowerCase().replace("_", " ")) + " 添加成功");
            return;
        }

        DyeColor color = DyeColor.valueOf(message.toUpperCase().replace("-", "_").replace(" ", "_"));
        inv.getEditedSection().set("shield-pattern.color", color.name());
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "盾牌颜色更改成功");
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<ShieldPatternData> statData) {

        if (statData.isPresent()) {
            lore.add(ChatColor.GRAY + "当前值:");
            ShieldPatternData data = statData.get();
            lore.add(ChatColor.GRAY + "* 基础颜色: "
                    + (data.getBaseColor() != null
                    ? ChatColor.GREEN + UtilityMethods.caseOnWords(data.getBaseColor().name().toLowerCase().replace("_", " "))
                    : ChatColor.RED + "None"));
            data.getPatterns().forEach(pattern -> lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + PatternType__name(pattern.getPattern()) + ChatColor.GRAY
                    + " - " + ChatColor.GREEN + pattern.getColor().name()));

        } else
            lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "None");

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击可更改盾牌颜色");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Shift + 左键可重置盾牌颜色");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击以添加图案");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 放下以删除最后一个图案");
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        if (mmoitem.getNBT().getItem().getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) mmoitem.getNBT().getItem().getItemMeta()).hasBlockState()
                && ((BlockStateMeta) mmoitem.getNBT().getItem().getItemMeta()).getBlockState() instanceof Banner) {
            Banner banner = (Banner) ((BlockStateMeta) mmoitem.getNBT().getItem().getItemMeta()).getBlockState();

            ShieldPatternData shieldPattern = new ShieldPatternData(banner.getBaseColor());
            shieldPattern.addAll(banner.getPatterns());
            mmoitem.setData(ItemStats.SHIELD_PATTERN, shieldPattern);
        }
    }

    @NotNull
    @Override
    public ShieldPatternData getClearStatData() {
        return new ShieldPatternData(DyeColor.YELLOW);
    }

    /**
     * This stat is saved not as a custom tag, but as the vanilla Pattern itself.
     * Alas this method returns null.
     */
    @Nullable
    @Override
    public ShieldPatternData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        return null;
    }

    private int getNextAvailableKey(ConfigurationSection section) {
        for (int j = 0; j < 100; j++)
            if (!section.contains("" + j))
                return j;
        return -1;
    }
}
