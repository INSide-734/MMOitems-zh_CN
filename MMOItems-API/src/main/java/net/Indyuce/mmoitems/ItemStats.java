package net.Indyuce.mmoitems;

import io.lumine.mythic.lib.version.VMaterial;
import net.Indyuce.mmoitems.stat.*;
import net.Indyuce.mmoitems.stat.annotation.DeprecatedStat;
import net.Indyuce.mmoitems.stat.block.*;
import net.Indyuce.mmoitems.stat.type.*;
import org.bukkit.Material;

/**
 * A central file for the plugin since new stats are added ALL the time.
 * It is also much safer to initialize class objects OUTSIDE of the field type class.
 */
@SuppressWarnings("unused")
public class ItemStats {

    // Main Item Stats
    public static final ItemStat
            REVISION_ID = new RevisionID(),
            MATERIAL = new MaterialStat(),
            SKULL_TEXTURE = new SkullTextureStat(),
            ITEM_DAMAGE = new ItemDamage(),
            CUSTOM_MODEL_DATA = new CustomModelData(),
            MODEL = new ItemModel(),
            EQUIPPABLE_SLOT = new EquippableSlot(),
            EQUIPPABLE_MODEL = new EquippableModel(),
            MAX_DURABILITY = new MaximumDurability(),
            MAX_ITEM_DAMAGE = new MaxItemDamage(),
            WILL_BREAK = new LostWhenBroken(),
            NAME = new DisplayName(),
            LORE = new Lore(),
            NBT_TAGS = new NBTTags(),
            MAX_STACK_SIZE = new MaxStackSize(),
            CAN_BREAK = new CanBreak(),
            LORE_FORMAT = new LoreFormat(),
            TOOLTIP = new TooltipStat(),

    // Block Specific Stats
    BLOCK_ID = new BlockID(),
            REQUIRED_POWER = new RequiredPower(),
            REQUIRE_POWER_TO_BREAK = new RequirePowerToBreak(),
            MIN_XP = new MinXP(),
            MAX_XP = new MaxXP(),
            GEN_TEMPLATE = new GenTemplate(),

    // Misc Stats
    DISPLAYED_TYPE = new DisplayedType(),
            ENCHANTS = new Enchants(),
            HIDE_ENCHANTS = new HideEnchants(),
            PERMISSION = new Permission(),
            ITEM_PARTICLES = new ItemParticles(),
            ARROW_PARTICLES = new ArrowParticles(),
            PROJECTILE_PARTICLES = new ProjectileParticles(),

    //   Disable Interaction Stats
    DISABLE_INTERACTION = new DisableStat("INTERACTION", VMaterial.GRASS_BLOCK.get(), "禁用交互", new String[]{"!block", "all"}, "禁止任何不必要的交互:", "方块放置、物品使用..."),
            DISABLE_CRAFTING = new DisableStat("CRAFTING", Material.CRAFTING_TABLE, "禁用合成", "玩家无法在合成时使用此物品。"), DISABLE_SMELTING = new DisableStat("SMELTING", Material.FURNACE, "禁用熔炼", "玩家不能在熔炉中使用该物品。"),
            DISABLE_SMITHING = new DisableStat("SMITHING", Material.DAMAGED_ANVIL, "禁用锻造", "玩家无法在锻造台中锻造此物品。"),
            DISABLE_ENCHANTING = new DisableStat("ENCHANTING", Material.ENCHANTING_TABLE, "禁用附魔", new String[]{"!block", "all"}, "玩家无法对此物品进行附魔。"),
            DISABLE_REPAIRING = new DisableStat("REPAIRING", Material.ANVIL, "禁用修复", new String[]{"!block", "all"}, "玩家无法在铁砧中使用此物品进行修复。"),
            DISABLE_ARROW_SHOOTING = new DisableStat("ARROW_SHOOTING", Material.ARROW, "禁用箭射击", new Material[]{Material.ARROW}, "玩家无法使用弓射击此", "物品。"),
            DISABLE_DROP = new DisableStat("DROPING", Material.LAVA_BUCKET, "禁用物品丢弃", new String[]{"all"}, "禁止丢弃此物品！"),
            DISABLE_ARROW_CONSUMPTION = new DisableStat("ARROW_CONSUMPTION", Material.ARROW, "禁用箭消耗", new String[]{"crossbow"}, "禁用箭的需求和消耗。"),

    //   Special item interactions/skills
    LEFT_CLICK_SCRIPT = new ActionLeftClick(),
            RIGHT_CLICK_SCRIPT = new ActionRightClick(),
            DISABLE_ATTACK_PASSIVE = new DisableStat("ATTACK_PASSIVE", Material.BARRIER, "禁用击中效果", new String[]{"weapon"}, "禁止物品类型授予的击中效果。"),

    // RPG Stats
    REQUIRED_LEVEL = new RequiredLevel(),
            REQUIRED_CLASS = new RequiredClass(),
            ATTACK_DAMAGE = new AttackDamage(),
            ATTACK_SPEED = new AttackSpeed(),
            CRITICAL_STRIKE_CHANCE = new DoubleStat("CRITICAL_STRIKE_CHANCE", Material.NETHER_STAR, "暴击概率", new String[]{"暴击造成更多伤害。", "以百分比的几率触发。"}, new String[]{"!miscellaneous", "!block", "all"}),
            CRITICAL_STRIKE_POWER = new DoubleStat("CRITICAL_STRIKE_POWER", Material.NETHER_STAR, "暴击威力", new String[]{"武器暴击额外造成的伤害。", "(与默认值叠加)", "以百分比计算。"}, new String[]{"!miscellaneous", "!block", "all"}),
            SKILL_CRITICAL_STRIKE_CHANCE = new DoubleStat("SKILL_CRITICAL_STRIKE_CHANCE", Material.NETHER_STAR, "技能暴击概率", new String[]{"增加技能暴击的触发几率（以百分比计算）。"}, new String[]{"!miscellaneous", "!block", "all"}),
            SKILL_CRITICAL_STRIKE_POWER = new DoubleStat("SKILL_CRITICAL_STRIKE_POWER", Material.NETHER_STAR, "技能暴击威力", new String[]{"技能暴击造成的额外伤害（以百分比计算）。", "(与默认值叠加)", "以百分比计算。"}, new String[]{"!miscellaneous", "!block", "all"}),
            BLOCK_POWER = new DoubleStat("BLOCK_POWER", Material.IRON_HELMET, "格挡强度", new String[]{"你的护甲/盾牌能够格挡的伤害百分比。", "默认：25%"}, new String[]{"!miscellaneous", "!block", "all"}),
            BLOCK_RATING = new DoubleStat("BLOCK_RATING", Material.IRON_HELMET, "格挡几率", new String[]{"你的一件护甲有多大几率", "格挡任何生物的攻击。"}, new String[]{"!miscellaneous", "!block", "all"}),
            BLOCK_COOLDOWN_REDUCTION = new DoubleStat("BLOCK_COOLDOWN_REDUCTION", Material.IRON_HELMET, "格挡冷却时间缩减", new String[]{"减少格挡的冷却时间（%）。"}, new String[]{"!miscellaneous", "!block", "all"}),
            DODGE_RATING = new DoubleStat("DODGE_RATING", Material.FEATHER, "闪避几率", new String[]{"闪避攻击的几率。", "闪避完全抵消攻击伤害。"}, new String[]{"!miscellaneous", "!block", "all"}),
            DODGE_COOLDOWN_REDUCTION = new DoubleStat("DODGE_COOLDOWN_REDUCTION", Material.FEATHER, "躲闪冷却时间缩减", new String[]{"减少躲避的冷却时间（%）。"}, new String[]{"!miscellaneous", "!block", "all"}),
            PARRY_RATING = new DoubleStat("PARRY_RATING", Material.BUCKET, "招架几率", new String[]{"招架攻击的几率。", "招架抵消伤害并击退攻击者。"}, new String[]{"!miscellaneous", "!block", "all"}),
            PARRY_COOLDOWN_REDUCTION = new DoubleStat("PARRY_COOLDOWN_REDUCTION", Material.BUCKET, "招架冷却时间缩减", new String[]{"减少招架的冷却时间（%）。"}, new String[]{"!miscellaneous", "!block", "all"}),
            COOLDOWN_REDUCTION = new DoubleStat("COOLDOWN_REDUCTION", Material.BOOK, "冷却时间缩减", new String[]{"减少物品和玩家技能的冷却时间（%）。"}),
            RANGE = new DoubleStat("RANGE", Material.STICK, "攻击范围", new String[]{"你的物品攻击的攻击范围。"}, new String[]{"staff", "whip", "wand", "musket", "gem_stone"}),
            MANA_COST = new ManaCost(),
            STAMINA_COST = new DoubleStat("STAMINA_COST", Material.LIGHT_GRAY_DYE, "耐力消耗", new String[]{"武器使用时消耗的耐力。"}, new String[]{"weapon"}),
            ARROW_VELOCITY = new DoubleStat("ARROW_VELOCITY", Material.ARROW, "箭矢速度", new String[]{"决定你的武器可以射击的距离。", "默认：1.0"}, new String[]{"gem_stone", "bow", "crossbow"}),
            ARROW_POTION_EFFECTS = new ArrowPotionEffects(),
            PVE_DAMAGE = new DoubleStat("PVE_DAMAGE", Material.PORKCHOP, "PvE 伤害", new String[]{"对非人类实体造成的额外伤害。"}, new String[]{"equipment", "gem_stone"}),
            PVP_DAMAGE = new DoubleStat("PVP_DAMAGE", Material.SKELETON_SKULL, "PvP 伤害", new String[]{"对玩家造成的额外伤害。"}, new String[]{"equipment", "gem_stone"}),
            BLUNT_POWER = new BluntPower(),
            BLUNT_RATING = new BluntRating(),
            WEAPON_DAMAGE = new DoubleStat("WEAPON_DAMAGE", Material.IRON_SWORD, "武器伤害", new String[]{"额外的击中武器伤害（以百分比计算）。"}),
            SKILL_DAMAGE = new DoubleStat("SKILL_DAMAGE", Material.BOOK, "技能伤害", new String[]{"额外的技能伤害（以百分比计算）。"}),
            PROJECTILE_DAMAGE = new DoubleStat("PROJECTILE_DAMAGE", Material.ARROW, "弹射物伤害", new String[]{"额外的技能/武器弹道伤害。"}),
            MAGIC_DAMAGE = new DoubleStat("MAGIC_DAMAGE", Material.MAGMA_CREAM, "魔法伤害", new String[]{"额外的魔法技能伤害（以百分比计算）。"}),
            PHYSICAL_DAMAGE = new DoubleStat("PHYSICAL_DAMAGE", Material.IRON_AXE, "物理伤害", new String[]{"额外的技能/武器物理伤害。"}),
            DEFENSE = new DoubleStat("DEFENSE", Material.SHIELD, "防御", new String[]{"减少来自任何来源的伤害。", "公式可在MMOLib配置中设置。"}),
            DAMAGE_REDUCTION = new DoubleStat("DAMAGE_REDUCTION", Material.IRON_CHESTPLATE, "伤害减免", new String[]{"减少来自任何来源的伤害。", "以百分比计算。"}),
            FALL_DAMAGE_REDUCTION = new DoubleStat("FALL_DAMAGE_REDUCTION", Material.FEATHER, "坠落伤害减免", new String[]{"减少坠落伤害。", "以百分比计算。"}),
            PROJECTILE_DAMAGE_REDUCTION = new DoubleStat("PROJECTILE_DAMAGE_REDUCTION", Material.SNOWBALL, "弹道伤害减免", new String[]{"减少弹道伤害。", "以百分比计算。"}),
            PHYSICAL_DAMAGE_REDUCTION = new DoubleStat("PHYSICAL_DAMAGE_REDUCTION", Material.LEATHER_CHESTPLATE, "物理伤害减免", new String[]{"减少物理伤害。", "以百分比计算。"}),
            FIRE_DAMAGE_REDUCTION = new DoubleStat("FIRE_DAMAGE_REDUCTION", Material.BLAZE_POWDER, "火焰伤害减免", new String[]{"减少火焰伤害。", "以百分比计算。"}),
            MAGIC_DAMAGE_REDUCTION = new DoubleStat("MAGIC_DAMAGE_REDUCTION", Material.POTION, "魔法伤害减免", new String[]{"减少药水造成的魔法伤害。", "以百分比计算。"}),
            PVE_DAMAGE_REDUCTION = new DoubleStat("PVE_DAMAGE_REDUCTION", Material.PORKCHOP, "PvE 伤害减免", new String[]{"减少怪物造成的伤害。", "以百分比计算。"}),
            PVP_DAMAGE_REDUCTION = new DoubleStat("PVP_DAMAGE_REDUCTION", Material.SKELETON_SKULL, "PvP 伤害减免", new String[]{"减少玩家造成的伤害。", "以百分比计算。"}),
            UNDEAD_DAMAGE = new DoubleStat("UNDEAD_DAMAGE", Material.SKELETON_SKULL, "不死生物伤害", new String[]{"对不死生物造成额外伤害。", "以百分比计算。"}),
            LIFESTEAL = new DoubleStat("LIFESTEAL", Material.REDSTONE, "吸血", new String[]{"当造成武器伤害时，你获得的", "生命值百分比。"}),
            SPELL_VAMPIRISM = new DoubleStat("SPELL_VAMPIRISM", Material.REDSTONE, "法术吸血", new String[]{"当施放技能伤害时，你获得的", "生命值百分比。"}),

    // Extra Stats
    UNBREAKABLE = new Unbreakable(),
            TIER = new ItemTierStat(),
            SET = new ItemSetStat(),
            ARMOR = new Armor(),
            ARMOR_TOUGHNESS = new ArmorToughness(),
            MAX_HEALTH = new MaxHealth(),
            UNSTACKABLE = new Unstackable(),
            MAX_MANA = new DoubleStat("MAX_MANA", Material.LAPIS_LAZULI, "最大法力值", new String[]{"增加最大法力值的魔力"}),
            KNOCKBACK_RESISTANCE = new KnockbackResistance(),
            MOVEMENT_SPEED = new MovementSpeed(),
            TWO_HANDED = new BooleanStat("TWO_HANDED", Material.IRON_INGOT, "双手持握", new String[]{"若设为true, 则玩家持有两个物品时", "其中一个为双手武器时会显著降低速度"}, new String[]{"handheld"}),
            REQUIRED_BIOMES = new RequiredBiomes(),
            DROP_ON_DEATH = new DisableDeathDrop(),
            HIDE_DURABILITY_BAR = new HideDurabilityBar(),

    // Extra Attributes (1.20.2+)
    MAX_ABSORPTION = new MaxAbsorption(),
            BLOCK_BREAK_SPEED = new BlockBreakSpeed(),
            BLOCK_INTERACTION_RANGE = new BlockInteractionRange(),
            ENTITY_INTERACTION_RANGE = new EntityInteractionRange(),
            FALL_DAMAGE_MULTIPLIER = new FallDamageMultiplier(),
            GRAVITY = new Gravity(),
            JUMP_STRENGTH = new JumpStrength(),
            SAFE_FALL_DISTANCE = new SafeFallDistance(),
            SCALE = new Scale(),
            STEP_HEIGHT = new StepHeight(),
            BURNING_TIME = new BurningTime(),
            EXPLOSION_KNOCKBACK_RESISTANCE = new ExplosionKnockbackResistance(),
            MINING_EFFICIENCY = new MiningEfficiency(),
            MOVEMENT_EFFICIENCY = new MovementEfficiency(),
            OXYGEN_BONUS = new OxygenBonus(),
            SNEAKING_SPEED = new SneakingSpeed(),
            SUBMERGED_MINING_SPEED = new SubmergedMiningSpeed(),
            SWEEPING_DAMAGE_RATIO = new SweepingDamageRatio(),
            WATER_MOVEMENT_EFFICIENCY = new WaterMovementEfficiency(),

    // Permanent Effects
    PERM_EFFECTS = new PermanentEffects(),
            GRANTED_PERMISSIONS = new GrantedPermissions(),

    // Consumable Stats
    RESTORE_HEALTH = new RestoreHealth(),
            RESTORE_FOOD = new RestoreFood(),
            RESTORE_SATURATION = new RestoreSaturation(),
            RESTORE_MANA = new RestoreMana(),
            RESTORE_STAMINA = new RestoreStamina(),
            CAN_IDENTIFY = new CanIdentify(),
            CAN_DECONSTRUCT = new CanDeconstruct(),
            CAN_DESKIN = new CanDeskin(),
            EFFECTS = new Effects(),
            SOULBINDING_CHANCE = new SoulbindingChance(),
            SOULBOUND_BREAK_CHANCE = new SoulbindingBreakChance(),
            SOULBOUND_LEVEL = new SoulboundLevel(),
    //  AUTO_SOULBIND = new BooleanStat("AUTO_SOULBIND", VersionMaterial.ENDER_EYE.toMaterial(), "Auto-Soulbind", new String[]{"Automatically soulbinds this item to", "a player when he acquires it."}, new String[]{"!consumable", "all"}),
    ITEM_COOLDOWN = new DoubleStat("ITEM_COOLDOWN", Material.COOKED_CHICKEN, "物品冷却", new String[]{"此冷却适用于消耗品", "以及物品命令"}, new String[]{"!armor", "!gem_stone", "!block", "all"}),
            COOLDOWN_REFERENCE = new StringStat("COOLDOWN_REFERENCE", Material.CHICKEN, "冷却参考", new String[]{"具有相同冷却参考的两个物品", "将共享它们的冷却时间这对于例如生命或法力药剂非常有用"}, new String[]{"!armor", "!gem_stone", "!block", "all"}),
            VANILLA_EATING_ANIMATION = new VanillaEatingAnimation(),
            GEM_COLOR = new GemColor(),
            GEM_UPGRADE_SCALING = new GemUpgradeScaling(),
            ITEM_TYPE_RESTRICTION = new ItemTypeRestriction(),
            MAX_CONSUME = new MaxConsume(),
            SUCCESS_RATE = new SuccessRate(),

    // Crafting Stats
    CRAFTING = new Crafting(),
            CRAFT_PERMISSION = new CraftingPermission(),
    //CRAFT_AMOUNT = new DoubleStat("CRAFTED_AMOUNT", Material.WOODEN_AXE, "Crafted Amount", new String[]{"The stack count for", "this item when crafted."}, new String[0]),

    // Unique Stats
    AUTOSMELT = new BooleanStat("AUTOSMELT", Material.COAL, "自动熔炼", new String[]{"如果设置为true, 你的工具将会", "自动熔炼挖掘到的矿石"}, new String[]{"tool"}),
            BOUNCING_CRACK = new BooleanStat("BOUNCING_CRACK", Material.COBBLESTONE_WALL, "范围挖掘", new String[]{"如果设置为true, 你的工具将会", "同时破坏附近的方块"}, new String[]{"tool"}),
            PICKAXE_POWER = new PickaxePower(),
            CUSTOM_SOUNDS = new CustomSounds(),
            ELEMENTS = new Elements(),
            COMMANDS = new Commands(),
    //   STAFF_SPIRIT = new StaffSpiritStat(),
    LUTE_ATTACK_SOUND = new LuteAttackSoundStat(),
            LUTE_ATTACK_EFFECT = new LuteAttackEffectStat(),
            NOTE_WEIGHT = new DoubleStat("NOTE_WEIGHT", Material.MUSIC_DISC_MALL, "音符重量", new String[]{"定义你的琴产生的投射物", "向下倾斜的程度"}, new String[]{"lute"}),
            REMOVE_ON_CRAFT = new BooleanStat("REMOVE_ON_CRAFT", Material.GLASS_BOTTLE, "合成时移除", new String[]{"如果物品在合成时应该完全", "移除, 或者变成一个空瓶子或空桶"}, new String[0], Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.MILK_BUCKET, Material.LAVA_BUCKET, Material.WATER_BUCKET),
            COMPATIBLE_TYPES = new CompatibleTypes(),
            COMPATIBLE_IDS = new CompatibleIds(),
            COMPATIBLE_MATERIALS = new CompatibleMaterials(),
            GEM_SOCKETS = new GemSockets(),
            RANDOM_UNSOCKET = new RandomUnsocket(),
    //todo CAN_UNSOCKET = new CanUnsocket(),
    REPAIR = new RepairPower(),
            REPAIR_PERCENT = new RepairPowerPercent(),
            REPAIR_TYPE = new RepairReference(),
            INEDIBLE = new BooleanStat("INEDIBLE", Material.POISONOUS_POTATO, "不可食用", new String[]{"玩家无法右键点击此消耗品", "", "它的任何效果都不会发生"}, new String[]{"consumable"}),
            DISABLE_RIGHT_CLICK_CONSUME = new DisableStat("RIGHT_CLICK_CONSUME", Material.BAKED_POTATO, "无限消耗", new String[]{"消耗品"}, "玩家将能够右键点击此消耗品", "并从其效果中受益, 但它不会被消耗"),
            KNOCKBACK = new DoubleStat("KNOCKBACK", Material.IRON_HORSE_ARMOR, "击退", new String[]{"使用这把火枪会击退", "如果为正, 则击退用户"}, new String[]{"musket", "gem_stone"}),
            RECOIL = new DoubleStat("RECOIL", Material.IRON_HORSE_ARMOR, "后坐力", new String[]{"对应于射击的不准确性"}, new String[]{"musket", "gem_stone"}),
            HANDWORN = new BooleanStat("HANDWORN", Material.STRING, "无视双持", new String[]{"这个物品无视双手持有的物品", "", "基本上是一枚你可以佩戴的戒指或手套, ", "并且仍然可以释放手来", "携带双手武器"}, new String[]{"catalyst", "off_catalyst", "main_catalyst"}),
            AMPHIBIAN = new Amphibian(),

    // Abilities & Upgrading
    ABILITIES = new Abilities(),
            UPGRADE = new UpgradeStat(),
            DOWNGRADE_ON_BREAK = new BooleanStat("BREAK_DOWNGRADE", Material.DAMAGED_ANVIL, "损坏时降级", new String[]{"如果此物品耐久度达到0，", "它将被完全修复，但也会", "降级一级。", "", "&c仅当无法进一步降级时才会损坏", "", "需要定义一个 &6升级模板", "需要定义 &6自定义耐久度"}, new String[]{"equipment"}),
            DOWNGRADE_ON_DEATH = new BooleanStat("DEATH_DOWNGRADE", Material.DAMAGED_ANVIL, "死亡时降级", new String[]{"如果装备此物品的玩家死亡，", "它可能会降级（基于 &6死亡降级", "&6几率 &7属性）", "", "需要定义一个 &6升级模板", "需要启用 &6保留物品 (keep-inventory) 规则。"}, new String[]{"equipment"}),
            DOWNGRADE_ON_DEATH_CHANCE = new DoubleStat("DEATH_DOWNGRADE_CHANCE", Material.SKELETON_SKULL, "死亡降级几率", new String[]{"物品在玩家死亡时降级的概率。", "", "超过100%将确定降级一件物品，", "并再次投掷以降级另一件（使用多余的概率）。", "&6相同的物品不会降级两次。"}, new String[]{"equipment"}, false),

    // Unique Item Stats
    DYE_COLOR = new DyeColor(),
            HIDE_DYE = new HideDye(),
            TRIM_MATERIAL = new TrimMaterialStat(),
            TRIM_PATTERN = new TrimPatternStat(),
            HIDE_ARMOR_TRIM = new HideTrim(),
            POTION_EFFECTS = new PotionEffects(),
            POTION_COLOR = new PotionColor(),
            SHIELD_PATTERN = new ShieldPatternStat(),
            HIDE_POTION_EFFECTS = new HidePotionEffects(),

    // Internal Stats
    SOULBOUND = new Soulbound(),
            CUSTOM_DURABILITY = new CustomDurability(),
            STORED_TAGS = new StoredTags(),
            ITEM_LEVEL = new ItemLevel(),
            BROWSER_DISPLAY_IDX = new BrowserDisplayIDX();

    /**
     * @deprecated Item damage is now {@link ItemDamage} and
     * custom durability is now {@link CustomDurability}
     */
    @Deprecated
    @DeprecatedStat
    public static final ItemStat DURABILITY = ITEM_DAMAGE;
}
