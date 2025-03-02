package net.Indyuce.mmoitems.api;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.player.cooldown.CooldownObject;
import io.lumine.mythic.lib.player.modifier.ModifierSource;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.util.PostLoadAction;
import io.lumine.mythic.lib.util.PreloadedObject;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.interaction.*;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.Lute;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.Musket;
import net.Indyuce.mmoitems.api.item.util.identify.UnidentifiedItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.manager.TypeManager;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public class Type implements CooldownObject, PreloadedObject {

    // Slashing
    public static final Type SWORD = new Type("SWORD", ModifierSource.MELEE_WEAPON);

    // Piercing
    public static final Type DAGGER = new Type("DAGGER", ModifierSource.MELEE_WEAPON);
    public static final Type SPEAR = new Type("SPEAR", ModifierSource.MELEE_WEAPON);

    // Blunt
    public static final Type HAMMER = new Type("HAMMER", ModifierSource.MELEE_WEAPON);
    public static final Type GAUNTLET = new Type("GAUNTLET", ModifierSource.MELEE_WEAPON);

    // Range
    public static final Type WHIP = new Type("WHIP", ModifierSource.RANGED_WEAPON);
    public static final Type STAFF = new Type("STAFF", ModifierSource.RANGED_WEAPON);
    public static final Type BOW = new Type("BOW", ModifierSource.RANGED_WEAPON);
    public static final Type CROSSBOW = new Type("CROSSBOW", ModifierSource.RANGED_WEAPON);
    public static final Type MUSKET = new Type("MUSKET", ModifierSource.RANGED_WEAPON, Musket::new);
    public static final Type LUTE = new Type("LUTE", ModifierSource.RANGED_WEAPON, Lute::new);

    // Other weapons
    public static final Type TOOL = new Type("TOOL", ModifierSource.MELEE_WEAPON, Tool::new);

    // Hand Accessories
    public static final Type CATALYST = new Type("CATALYST", ModifierSource.HAND_ITEM);
    public static final Type OFF_CATALYST = new Type("OFF_CATALYST", ModifierSource.OFFHAND_ITEM);
    public static final Type MAIN_CATALYST = new Type("MAIN_CATALYST", ModifierSource.MAINHAND_ITEM);

    // Other
    public static final Type ORNAMENT = new Type("ORNAMENT", ModifierSource.VOID);
    public static final Type ARMOR = new Type("ARMOR", ModifierSource.ARMOR);
    public static final Type CONSUMABLE = new Type("CONSUMABLE", ModifierSource.MAINHAND_ITEM, Consumable::new);
    public static final Type MISCELLANEOUS = new Type("MISCELLANEOUS", ModifierSource.MAINHAND_ITEM);
    public static final Type GEM_STONE = new Type("GEM_STONE", ModifierSource.VOID, GemStone::new);
    public static final Type SKIN = new Type("SKIN", ModifierSource.VOID, ItemSkin::new);
    public static final Type ACCESSORY = new Type("ACCESSORY", ModifierSource.ACCESSORY);
    public static final Type BLOCK = new Type("BLOCK", ModifierSource.VOID);

    private final String id;
    private final ModifierSource modifierSource;
    private final BiFunction<PlayerData, NBTItem, UseItem> interactionProvider;

    private String name;

    @Nullable
    private String loreFormat;

    @NotNull
    private String attackCooldownKey;

    /**
     * Used to display the item in the item explorer and in the item recipes
     * list in the advanced workbench. can also be edited using the config
     * files.
     */
    private ItemStack item;

    /**
     * The parent item type determines:
     * - the item options compatible with this new item type
     * - if this item type is a weapon or not
     * - types of modifiers an item of this type would give
     * - the lore format
     */
    private final Type parent;

    private UnidentifiedItem unidentifiedTemplate;

    private SkillHandler<?> onLeftClick, onRightClick, onAttack, onEntityInteract;

    private boolean meleeAttacks, hideInGame;

    /**
     * Cached list of stats which can be applied onto an item with this type
     */
    private final List<ItemStat> available = new ArrayList<>();

    private final PostLoadAction postLoadAction = new PostLoadAction(true, config -> {
        onLeftClick = config.contains("on-left-click") ? MythicLib.plugin.getSkills().loadSkillHandler(config.get("on-left-click")) : null;
        onRightClick = config.contains("on-right-click") ? MythicLib.plugin.getSkills().loadSkillHandler(config.get("on-right-click")) : null;
        onAttack = config.contains("on-attack") ? MythicLib.plugin.getSkills().loadSkillHandler(config.get("on-attack")) : null;
        onEntityInteract = config.contains("on-entity-interact") ? MythicLib.plugin.getSkills().loadSkillHandler(config.get("on-entity-interact")) : null;
    });

    public Type(@NotNull String id, @NotNull ModifierSource modifierSource) {
        this(id, modifierSource, modifierSource.isWeapon() ? Weapon::new : UseItem::new);
    }

    /**
     * Hard-coded type with given parameters. Can be used
     * by other plugins to create types using MMOItems API.
     */
    public Type(@NotNull String id, @NotNull ModifierSource modifierSource, @NotNull BiFunction<PlayerData, NBTItem, UseItem> interactionProvider) {
        this.id = UtilityMethods.enumName(id);
        this.modifierSource = modifierSource;
        this.interactionProvider = interactionProvider;
        this.parent = null;
    }

    /**
     * Load custom type from a configuration file
     */
    public Type(@NotNull TypeManager manager, @NotNull ConfigurationSection config) {
        id = UtilityMethods.enumName(config.getName());
        Validate.isTrue(config.contains("parent"), "Custom types require a parent type");
        parent = manager.getOrThrow(UtilityMethods.enumName(config.getString("parent")));
        modifierSource = config.contains("modifier-source") ? ModifierSource.valueOf(UtilityMethods.enumName(config.getString("modifier-source"))) : (parent != null ? parent.modifierSource : ModifierSource.OTHER);
        interactionProvider = parent.interactionProvider;
    }

    public void load(@NotNull ConfigurationSection config) {
        postLoadAction.cacheConfig(config);

        name = config.getString("name", name);
        item = MMOUtils.readIcon(config.getString("display", item == null ? Material.STONE.toString() : item.getType().toString()));
        unidentifiedTemplate = new UnidentifiedItem(config.getConfigurationSection("unident-item"));
        loreFormat = config.getString("LoreFormat", (parent != null ? parent.loreFormat : null));
        attackCooldownKey = config.getString("attack-cooldown-key", "default");
        meleeAttacks = !config.getBoolean("disable-melee-attacks");
        hideInGame = config.getBoolean("hide-in-game");
    }

    @NotNull
    @Override
    public PostLoadAction getPostLoadAction() {
        return postLoadAction;
    }

    public boolean isDisplayed() {
        return !hideInGame;
    }

    @NotNull
    public UseItem toUseItem(@NotNull Player player, @NotNull NBTItem item) {
        return toUseItem(PlayerData.get(player), item);
    }

    @NotNull
    public UseItem toUseItem(@NotNull PlayerData playerData, @NotNull NBTItem item) {
        return interactionProvider.apply(playerData, item);
    }

    /**
     * Example <code>OFF_CATALYST</code>
     *
     * @return Internal name in uppercase and no spaces.
     */
    public String getId() {
        return id;
    }

    public boolean isWeapon() {
        return modifierSource.isWeapon();
    }

    public boolean hasMeleeAttacks() {
        return meleeAttacks;
    }

    public String getName() {
        return name;
    }

    @NotNull
    public ModifierSource getModifierSource() {
        return modifierSource;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    @Nullable
    public SkillHandler<?> onLeftClick() {
        return onLeftClick;
    }

    @Override
    public String getCooldownPath() {
        return "mmoitems_weapons:" + attackCooldownKey;
    }

    @Nullable
    public SkillHandler<?> onRightClick() {
        return onRightClick;
    }

    @Nullable
    public SkillHandler<?> onAttack() {
        return onAttack;
    }

    @Nullable
    public SkillHandler<?> onEntityInteract() {
        return onEntityInteract;
    }

    /**
     * @return Does it display as four rows in /mmoitems browse?
     */
    public boolean isFourGUIMode() {
        return getSupertype().modifierSource == ModifierSource.ARMOR;
    }

    /**
     * @return Default lore format used by this Type
     */
    @Nullable
    public String getLoreFormat() {
        return loreFormat;
    }

    /**
     * @return The highest parent of this type, or itself, if it has no parent.
     */
    @NotNull
    public Type getSupertype() {
        Type parentMost = this;
        while (parentMost.parent != null)
            parentMost = parentMost.parent;
        return parentMost;
    }

    /**
     * @return Either if the two types are the same,
     * or if this type is a subtype of the given type.
     */
    public boolean corresponds(Type type) {
        return getSupertype().equals(type);
    }

    /**
     * @return The collection of all stats which can be applied onto this
     * specific item type. This list is cached when types are being
     * loaded and is a PRETTY GOOD performance improvement.
     */
    public List<ItemStat> getAvailableStats() {
        return available;
    }

    /**
     * @return Finds the /item config file corresponding to the item type and
     * loads it
     */
    public ConfigFile getConfigFile() {
        return new ConfigFile("/item", getId().toLowerCase());
    }

    public UnidentifiedItem getUnidentifiedTemplate() {
        return unidentifiedTemplate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return id.equals(type.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Type{" +
                "id='" + id + '\'' +
                '}';
    }

    /**
     * Reads an ItemStack in hopes for finding its MMOItem Type.
     *
     * @param item The item to retrieve the type from
     * @return The type of the item, if it has a type.
     */
    @Nullable
    public static Type get(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }
        return get(NBTItem.get(item).getType());
    }

    /**
     * Used in command executors and completions for easier manipulation
     *
     * @param id The type id
     * @return The type or null if it couldn't be found
     */
    @Nullable
    public static Type get(@Nullable String id) {
        return id == null ? null : MMOItems.plugin.getTypes().get(UtilityMethods.enumName(id));
    }

    @Nullable
    public static Type get(@NotNull NBTItem item) {
        return get(item.getType());
    }

    /**
     * Used in command executors and completions for easier manipulation
     *
     * @param id The type id
     * @return If a registered type with this ID could be found
     */
    public static boolean isValid(@Nullable String id) {
        return id != null && MMOItems.plugin.getTypes().has(id.toUpperCase().replace("-", "_").replace(" ", "_"));
    }

    //region Deprecated API

    /**
     * @param stat The stat to check
     * @return If the stat can be handled by this type of item
     * @deprecated Use ItemStat.isCompatible(Type) instead
     */
    @Deprecated
    public boolean canHave(ItemStat stat) {
        return stat.isCompatible(this);
    }

    /**
     * @deprecated Use {@link #getSupertype()}
     */
    @Deprecated
    public boolean isSubtype() {
        return parent != null;
    }

    /**
     * @deprecated Use {@link #getSupertype()}
     */
    @Deprecated
    public Type getParent() {
        return parent;
    }

    /**
     * @deprecated 'weapon' boolean is now automatically inferred from the modifierSource
     */
    @Deprecated
    public Type(@NotNull String id, boolean weapon, @NotNull ModifierSource modSource) {
        this(id, modSource);
    }

    @Deprecated
    public void postload(ConfigurationSection config) {
        postLoadAction.cacheConfig(config);
        postLoadAction.performAction();
    }

    /**
     * @deprecated Type is no longer an enum so that external plugins
     * can register their own types. Use getId() instead
     */
    @Deprecated
    public String name() {
        return getId();
    }

    //endregion
}
