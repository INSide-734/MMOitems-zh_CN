package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ReforgeOptions;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.GemUpgradeScaling;
import net.Indyuce.mmoitems.stat.LuteAttackEffectStat.LuteAttackEffect;
import net.Indyuce.mmoitems.util.LanguageFile;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Handles configuration options and language. Since MMOItems 6.9+
 * language is handled by Crowdin which automatically pushes commits.
 */
public class ConfigManager implements Reloadable {

    // cached config files
    private ConfigFile loreFormat, dynLore;

    // Language
    private final Map<TriggerType, String> triggerTypeNames = new HashMap<>();
    private final Map<PotionEffectType, String> potionNames = new HashMap<>();

    // Cached config options
    public boolean replaceMushroomDrops, worldGenEnabled, upgradeRequirementsCheck, keepSoulboundOnDeath, rerollOnItemUpdate, opStatsEnabled, disableRemovedItems;
    public double soulboundBaseDamage, soulboundPerLvlDamage, levelSpread;
    public NumericStatFormula defaultItemCapacity;
    public ReforgeOptions revisionOptions, gemRevisionOptions, phatLootsOptions;
    public final List<String> opStats = new ArrayList<>();
    public String itemTypeLoreTag, gemStoneLoreTag, defaultTierName;
    private final Map<Material, Integer> defaultPickaxePower = new HashMap<>();

    public ConfigManager() {
        mkdir("item");
        mkdir("language");
        mkdir("language/lore-formats");

        final File modifiersFolder = new File(MMOItems.plugin.getDataFolder() + "/modifiers");
        if (!modifiersFolder.exists()) {
            mkdir("modifiers");
            DefaultFile.EXAMPLE_MODIFIERS.checkFile();
        }

        File craftingStationsFolder = new File(MMOItems.plugin.getDataFolder() + "/crafting-stations");
        if (!craftingStationsFolder.exists()) {
            if (craftingStationsFolder.mkdir()) {
                try {
                    JarFile jarFile = new JarFile(MMOItems.plugin.getJarFile());
                    for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                        String name = entries.nextElement().getName();
                        if (name.startsWith("default/crafting-stations/") && name.length() > "default/crafting-stations/".length())
                            Files.copy(MMOItems.plugin.getResource(name),
                                    new File(MMOItems.plugin.getDataFolder() + "/crafting-stations", name.split("/")[2]).toPath());
                    }
                    jarFile.close();
                } catch (IOException exception) {
                    MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not load default crafting stations.");
                }
            } else MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not create directory!");
        }

        // Load files with default configuration
        for (DefaultFile file : DefaultFile.values())
            if (file.isAutomatic())
                file.checkFile();

        /*
         * Setup /item files after generating the default /item files otherwise
         * they will be empty!
         */
        MMOItems.plugin.getTypes().getAll().forEach(type -> type.getConfigFile().setup());

        /*
         * Only load config files after they have been initialized (above) so
         * they do not crash the first time they generate and so we do not have
         * to restart the server
         */
        reload();
    }

    /**
     * This does both:
     * - check that all fields are input in the config file
     * - loads fields that it could find into MMOItems
     * <p>
     * These two steps are necessary for smooth language updates
     */
    private void loadTranslations() {

        // TODO messages
        final LanguageFile messages = new LanguageFile("messages");
        for (Message message : Message.values()) {
            String path = message.name().toLowerCase().replace("_", "-");
            if (!messages.getConfig().contains(path))
                messages.getConfig().set(path, message.getDefault());

            message.setRaw(messages.getConfig().getString(path));
        }
        messages.save();

        // Special lore tags that do not fit any stat.
        final ConfigurationSection statsConfig = new ConfigFile("/language", "stats").getConfig();
        itemTypeLoreTag = statsConfig.getString("item-type", "");
        gemStoneLoreTag = statsConfig.getString("gem-stone-lore", "");

        // Potion effects
        final LanguageFile potionEffects = new LanguageFile("potion-effects");
        for (PotionEffectType effect : PotionEffectType.values())
            potionNames.put(effect, potionEffects.computeTranslation(effect.getName().toLowerCase().replace("_", "-"),
                    () -> UtilityMethods.caseOnWords(effect.getName().toLowerCase().replace("_", " "))));
        potionEffects.save();

        // Staff spirits
        final LanguageFile attackEffects = new LanguageFile("attack-effects");
     /*   for (StaffSpirit sp : StaffSpirit.values())
            sp.setName(attackEffects.computeTranslation("staff-spirit." + sp.name().toLowerCase().replace("_", "-"),
                    () -> UtilityMethods.caseOnWords(sp.name().toLowerCase().replace("_", " "))));*/

        // Lute attack effects
        for (LuteAttackEffect eff : LuteAttackEffect.values())
            eff.setName(attackEffects.computeTranslation("lute-attack." + eff.name().toLowerCase().replace("_", "-"),
                    () -> UtilityMethods.caseOnWords(eff.name().toLowerCase().replace("_", " "))));
        attackEffects.save();

        // Trigger types
        triggerTypeNames.clear();
        final FileConfiguration abilities = new ConfigFile("/language", "abilities").getConfig();
        for (TriggerType type : TriggerType.values())
            triggerTypeNames.put(type, abilities.getString("cast-mode." + type.getLowerCaseId(), type.getName()));
    }

    public void reload() {
        MMOItems.plugin.reloadConfig();

        loreFormat = new ConfigFile("/language", "lore-format");

        dynLore = new ConfigFile("/language", "dynamic-lore");

        loadTranslations();

        /*
         * Reload cached config options for quicker access - these options are
         * used in runnables, it is thus better to cache them
         */
        replaceMushroomDrops = MMOItems.plugin.getConfig().getBoolean("custom-blocks.replace-mushroom-drops");
        worldGenEnabled = MMOItems.plugin.getConfig().getBoolean("custom-blocks.enable-world-gen");

        soulboundBaseDamage = MMOItems.plugin.getConfig().getDouble("soulbound.damage.base");
        soulboundPerLvlDamage = MMOItems.plugin.getConfig().getDouble("soulbound.damage.per-lvl");
        upgradeRequirementsCheck = MMOItems.plugin.getConfig().getBoolean("item-upgrade-requirements-check");
        GemUpgradeScaling.defaultValue = MMOItems.plugin.getConfig().getString("gem-upgrade-default", GemUpgradeScaling.SUBSEQUENT.getId());
        keepSoulboundOnDeath = MMOItems.plugin.getConfig().getBoolean("soulbound.keep-on-death");
        rerollOnItemUpdate = MMOItems.plugin.getConfig().getBoolean("item-revision.reroll-when-updated");
        levelSpread = MMOItems.plugin.getConfig().getDouble("item-level-spread");
        disableRemovedItems = MMOItems.plugin.getConfig().getBoolean("disable-removed-items");
        defaultTierName = MMOItems.plugin.getConfig().getString("default-tier-name");

        NumericStatFormula.RELATIVE_SPREAD = !MMOItems.plugin.getConfig().getBoolean("additive-spread-formula", false);

        opStatsEnabled = MMOItems.plugin.getConfig().getBoolean("op-item-stats.enabled");
        opStats.clear();
        for (String key : MMOItems.plugin.getConfig().getStringList("op-item-stats.stats"))
            opStats.add(UtilityMethods.enumName(key));

        ConfigurationSection keepData = MMOItems.plugin.getConfig().getConfigurationSection("item-revision.keep-data");
        ConfigurationSection phatLoots = MMOItems.plugin.getConfig().getConfigurationSection("item-revision.phat-loots");
        ConfigurationSection gemKeepData = MMOItems.plugin.getConfig().getConfigurationSection("item-revision.keep-gem-data");
        ReforgeOptions.dropRestoredGems = MMOItems.plugin.getConfig().getBoolean("item-revision.drop-extra-gems", true);
        revisionOptions = keepData != null ? new ReforgeOptions(keepData) : new ReforgeOptions();
        gemRevisionOptions = gemKeepData != null ? new ReforgeOptions(gemKeepData) : new ReforgeOptions();
        phatLootsOptions = phatLoots != null ? new ReforgeOptions(phatLoots) : new ReforgeOptions();

        List<String> exemptedPhatLoots = MMOItems.plugin.getConfig().getStringList("item-revision.disable-phat-loot");
        for (String epl : exemptedPhatLoots)
            phatLootsOptions.addToBlacklist(epl);

        try {
            defaultItemCapacity = new NumericStatFormula(MMOItems.plugin.getConfig().getConfigurationSection("default-item-capacity"));
        } catch (IllegalArgumentException exception) {
            defaultItemCapacity = new NumericStatFormula(5, .05, .1, .3);
            MMOItems.plugin.getLogger().log(Level.SEVERE,
                    "An error occurred while trying to load default capacity formula for the item generator, using default: "
                            + exception.getMessage());
        }

        // Default pickaxe power for vanilla/MI items
        for (String key : MMOItems.plugin.getConfig().getConfigurationSection("default-pickaxe-power").getKeys(false))
            try {
                Material mat = Material.valueOf(UtilityMethods.enumName(key));
                int pickPower = MMOItems.plugin.getConfig().getInt("default-pickaxe-power." + key);
                defaultPickaxePower.put(mat, pickPower);
            } catch (Exception exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, String.format("Could not load pickaxe power for '%s': %s", key, exception.getMessage()));
            }
    }

    public int getDefaultPickaxePower(@NotNull ItemStack item) {
        return defaultPickaxePower.getOrDefault(item.getType(), 0);
    }

    /**
     * @return Can this block material be broken by tool mechanics
     *         like 'Bouncing Crack'
     */
    public boolean isBlacklisted(@NotNull Material material) {
        return MMOItems.plugin.getConfig().getStringList("block-blacklist").contains(material.name());
    }

    /**
     * @deprecated Will be removed in the future.
     */
    @NotNull
    @Deprecated
    public String getStatFormat(String path) {
        final ConfigurationSection config = new ConfigFile("/language", "stats").getConfig();
        final String found = config.getString(path);
        return found == null ? "<TranslationNotFound:" + path + ">" : found;
    }

    @Deprecated
    public String getMessage(String path) {
        return Message.valueOf(UtilityMethods.enumName(path)).getFormatted();
    }

    @Deprecated
    @NotNull
    public String getCastingModeName(@NotNull TriggerType triggerType) {
        return getTriggerTypeName(triggerType);
    }

    @NotNull
    public String getTriggerTypeName(@NotNull TriggerType triggerType) {
        return Objects.requireNonNull(triggerTypeNames.get(triggerType), "Trigger type name for '" + triggerType.name() + "' not found");
    }

    @Deprecated
    public String getModifierName(String path) {
        return UtilityMethods.caseOnWords(path.toLowerCase().replace("-", " ").replace("_", " "));
    }

    @NotNull
    public List<String> getDefaultLoreFormat() {
        return loreFormat.getConfig().getStringList("lore-format");
    }

    @NotNull
    public String getPotionEffectName(PotionEffectType type) {
        return Objects.requireNonNull(potionNames.get(type), "Potion effect name for '" + type.getName() + "' not found");
    }

    @Deprecated
    public String getLuteAttackEffectName(LuteAttackEffect effect) {
        return effect.getName();
    }
/*
    @Deprecated
    public String getStaffSpiritName(StaffSpirit spirit) {
        return spirit.getName();
    }*/

    /**
     * @deprecated See {@link net.Indyuce.mmoitems.api.item.util.LoreUpdate}
     */
    @Deprecated
    public String getDynLoreFormat(String input) {
        return dynLore.getConfig().getString("format." + input);
    }

    /**
     * Creates an empty directory in the MMOItems plugin folder if it does not
     * exist
     *
     * @param path The path of your folder
     */
    private void mkdir(String path) {
        File folder = new File(MMOItems.plugin.getDataFolder() + "/" + path);
        if (!folder.exists())
            if (!folder.mkdir())
                MMOItems.plugin.getLogger().log(Level.SEVERE, "Could not create directory!");
    }

    /**
     * All config files that have a default configuration
     * are stored here, they get copied into the plugin
     * folder when the plugin enables.
     */
    public enum DefaultFile {

        // Default general config files -> /MMOItems
        ITEM_TIERS("", "item-tiers"),
        ITEM_TYPES("", "item-types", true),
        DROPS("", "drops"),
        ITEM_SETS("", "item-sets"),
        GEN_TEMPLATES("", "gen-templates"),
        UPGRADE_TEMPLATES("", "upgrade-templates"),
        EXAMPLE_MODIFIERS("modifiers", "example_modifiers", true),
        CUSTOM_STATS("", "custom-stats"),

        // Default EN language files
        ABILITIES("language", "abilities"),
        ATTACK_EFFECTS("language", "attack-effects"),
        CRAFTING_STATIONS("language", "crafting-stations"),
        LORE_FORMAT("language", "lore-format"),
        MESSAGES("language", "messages"),
        POTION_EFFECTS("language", "potion-effects"),
        STATS("language", "stats"),

        // Default item config files -> /MMOItems/item
        ARMOR,
        AXE,
        BLOCK,
        BOW,
        CATALYST,
        CONSUMABLE,
        CROSSBOW,
        DAGGER,
        GAUNTLET,
        GEM_STONE,
        GREATAXE,
        GREATHAMMER,
        GREATSTAFF,
        GREATSWORD,
        HALBERD,
        HAMMER,
        KATANA,
        LANCE,
        LONG_SWORD,
        MATERIAL,
        MISCELLANEOUS,
        MUSKET,
        OFF_CATALYST,
        ORNAMENT,
        SHIELD,
        SPEAR,
        STAFF,
        SWORD,
        TALISMAN,
        THRUSTING_SWORD,
        TOME,
        TOOL,
        WAND,
        WHIP;

        private final String folderPath, fileName;

        /**
         * Allows to use the checkFile() method while not
         * loading it automatically e.g item-types.yml
         */
        private final boolean manual;

        DefaultFile() {
            this.fileName = name().toLowerCase() + ".yml";
            this.folderPath = "item";
            this.manual = false;
        }

        DefaultFile(String folderPath, String fileName) {
            this(folderPath, fileName, false);
        }

        DefaultFile(String folderPath, String fileName, boolean manual) {
            this.folderPath = folderPath;
            this.fileName = fileName + ".yml";
            this.manual = manual;
        }

        public boolean isAutomatic() {
            return !manual;
        }

        public File getFile() {
            return new File(MMOItems.plugin.getDataFolder() + (folderPath.isEmpty() ? "" : '/' + folderPath), fileName);
        }

        public void checkFile() {
            File file = getFile();
            if (file.exists())
                return;

            try {
                if (!new YamlConverter(file).convert()) {
                    String resourcePath = (folderPath.isEmpty() ? "" : folderPath + '/') + fileName;
                    InputStream localFile = MMOItems.plugin.getResource("default/" + resourcePath);
                    Validate.notNull(localFile, String.format("Could not find default file %s", resourcePath));
                    Files.copy(localFile, file.getAbsoluteFile().toPath());
                }
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    public static class YamlConverter {
        private final File file;

        private final String fileName;

        public YamlConverter(File newConfig) {
            this.file = newConfig;
            this.fileName = newConfig.getName();
        }

        public boolean convert() throws IOException {
            if (!file.exists())
                if (fileName.equalsIgnoreCase("block.yml") && new File(MMOItems.plugin.getDataFolder(), "custom-blocks.yml").exists()) {
                    // creates the file
                    if (file.createNewFile()) {
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(MMOItems.plugin.getDataFolder(), "custom-blocks.yml"));

                        for (String id : config.getKeys(false)) {
                            ConfigurationSection section = config.getConfigurationSection(id);
                            section.set("material", "STONE"); // adds material
                            section.set("block-id", Integer.parseInt(id)); // adds
                            // block
                            // id
                            for (String node : section.getKeys(false)) {
                                Object value = section.get(node);
                                if (node.equalsIgnoreCase("display-name")) { // converts
                                    // name
                                    // format
                                    section.set("display-name", null);
                                    section.set("name", value);
                                }
                            }
                        }
                        config.save(file);
                        MMOItems.plugin.getLogger().log(Level.CONFIG, "Successfully converted custom-blocks.yml");
                        return true;
                    }
                }
            return false;
        }
    }
}
