package net.Indyuce.mmoitems.api.interaction.weapon;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.flags.CustomFlag;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.SkillResult;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.UntargetedWeaponUseEvent;
import net.Indyuce.mmoitems.api.interaction.UseItem;
import net.Indyuce.mmoitems.api.interaction.WeaponAttackResult;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.LegacyWeapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.stat.ActionLeftClick;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class Weapon extends UseItem {
    @Deprecated
    public Weapon(Player player, NBTItem item) {
        this(PlayerData.get(player), item);
    }

    public Weapon(PlayerData playerData, NBTItem item) {
        super(playerData, item);
    }

    @Override
    public boolean checkItemRequirements() {

        // Light checks first
        if (playerData.isEncumbered()) {
            Message.HANDS_TOO_CHARGED.format(ChatColor.RED).send(getPlayer());
            return false;
        }

        // Check for class, level... then flags
        return playerData.getRPG().canUse(getNBTItem(), true) && MythicLib.plugin.getFlags().isFlagAllowed(getPlayer(), getUseFlag());
    }

    public CustomFlag getUseFlag() {
        return CustomFlag.MI_WEAPONS;
    }

    /**
     * Only applies mana and stamina weapon costs
     *
     * @return If the attack was cast successfully
     */
    public boolean checkAndApplyWeaponCosts() {
        if (!checkWeaponCosts(false)) return false;
        applyWeaponCosts(null);
        return true;
    }

    /**
     * @return If instantaneous weapon costs are met.
     * @see #applyWeaponCosts(Double)
     */
    public boolean checkWeaponCosts(boolean cooldowns) {
        if (cooldowns && getPlayerData().getMMOPlayerData().getCooldownMap().isOnCooldown(mmoitem.getType()))
            return false;

        double manaCost = getNBTItem().getStat("MANA_COST");
        if (manaCost > 0 && playerData.getRPG().getMana() < manaCost) {
            Message.NOT_ENOUGH_MANA.format(ChatColor.RED).send(getPlayer());
            return false;
        }

        double staminaCost = getNBTItem().getStat("STAMINA_COST");
        if (staminaCost > 0 && playerData.getRPG().getStamina() < staminaCost) {
            Message.NOT_ENOUGH_STAMINA.format(ChatColor.RED).send(getPlayer());
            return false;
        }

        return true;
    }

    /**
     * Hard coded instantaneous weapon costs are as follows:
     * - mana and stamina (resources)
     * - cooldown (given by the item type attack cooldown key)
     * <p>
     * Other weapon costs are inherent to the item type and are
     * fully configurable inside of the types config file.
     *
     * @see #checkWeaponCosts(boolean)
     */
    public void applyWeaponCosts(@Nullable Double attackDelay) {

        final double manaCost = getNBTItem().getStat("MANA_COST");
        if (manaCost > 0) playerData.getRPG().giveMana(-manaCost);

        final double staminaCost = getNBTItem().getStat("STAMINA_COST");
        if (staminaCost > 0) playerData.getRPG().giveStamina(-staminaCost);

        if (attackDelay != null)
            getPlayerData().getMMOPlayerData().getCooldownMap().applyCooldown(mmoitem.getType(), attackDelay);
    }

    /**
     * Only applies mana and stamina costs. Cooldown is not required for
     * targeted attacks since the vanilla attack bar already does that.
     *
     * @param attackMeta The attack being performed
     * @param attacker   The player attacker
     * @param target     The attack target
     * @return If the attack is successful, or if it was canceled otherwise
     */
    public boolean handleTargetedAttack(AttackMetadata attackMeta, @NotNull PlayerMetadata attacker, LivingEntity target) {

        // Handle weapon mana and stamina costs ONLY
        if (!checkAndApplyWeaponCosts()) return false;

        // Handle on-hit attack effects
        final SkillHandler onHitSkill = mmoitem.getType().onAttack();
        if (onHitSkill != null && !getNBTItem().getBoolean("MMOITEMS_DISABLE_ATTACK_PASSIVE"))
            new SimpleSkill(onHitSkill).cast(new TriggerMetadata(attacker, TriggerType.API, target, attackMeta));

        return true;
    }

    @Nullable
    private SkillHandler findClickSkill(boolean rightClick) {
        String skillId = getNBTItem().getString((rightClick ? ItemStats.RIGHT_CLICK_SCRIPT : ItemStats.LEFT_CLICK_SCRIPT).getNBTPath());

        // (Deprecated) Support for staff spirits on left-clicks
        if (!rightClick && (skillId == null || skillId.isEmpty()))
            skillId = getNBTItem().getString(ActionLeftClick.LEGACY_PATH);

        if (skillId == null || skillId.isEmpty()) {

            // Find item type action
            final Type itemType = mmoitem.getType();
            return rightClick ? itemType.onRightClick() : itemType.onLeftClick();
        }

        // Override item type with custom action
        return MythicLib.plugin.getSkills().getHandler(skillId);
    }

    /**
     * Called when the player interacts with the item. This method is used to
     * apply durability and cast the weapon attack
     *
     * @param rightClick Is the click a right click? If false, it's a left click.
     * @param actionHand Slot being interacted with
     * @return Null if there are no attack detected
     * @implNote Since MI 6.7.3 this method handles custom durability, cooldown
     *         checks and player stat snapshots.
     */
    @Nullable
    public WeaponAttackResult handleUntargetedAttack(boolean rightClick, @NotNull EquipmentSlot actionHand) {
        if (this instanceof LegacyWeapon) return legacyHandleUntargetedAttack(rightClick, actionHand);

        // Check for item type attack effect
        final SkillHandler handler = findClickSkill(rightClick);
        if (handler == null) return WeaponAttackResult.NO_ATTACK;

        // Check for attack effect conditions
        final SkillMetadata meta = new TriggerMetadata(getPlayerData().getMMOPlayerData(), TriggerType.API).toSkillMetadata(new SimpleSkill(handler));
        final SkillResult result = handler.getResult(meta);
        if (!result.isSuccessful()) return WeaponAttackResult.NO_ATTACK;

        // Check for durability
        DurabilityItem durItem = DurabilityItem.from(getPlayer(), getNBTItem(), actionHand.toBukkit());
        if (durItem != null && durItem.isBroken()) return WeaponAttackResult.DURABILITY;

        // Apply weapon instantaneous costs
        PlayerMetadata stats = getPlayerData().getStats().newTemporary(actionHand);
        if (!checkWeaponCosts(true)) return WeaponAttackResult.WEAPON_COSTS;

        // Check for Bukkit event
        UntargetedWeaponUseEvent called = new UntargetedWeaponUseEvent(playerData, this);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled()) return WeaponAttackResult.BUKKIT_EVENT;

        // Attack is ready to be performed.
        // Apply weapon costs
        final double attackDelay = 1 / requireNonZero(stats.getStat("ATTACK_SPEED"), MMOItems.plugin.getConfig().getDouble("default.attack-speed"));
        applyWeaponCosts(attackDelay);

        // Apply weapon attack effect
        handler.whenCast(result, meta);

        // Apply durability loss
        if (durItem != null) durItem.decreaseDurability(1).updateInInventory();
        return WeaponAttackResult.SUCCESS;
    }

    @Deprecated
    private WeaponAttackResult legacyHandleUntargetedAttack(boolean rightClick, @NotNull EquipmentSlot actionHand) {

        // Check for attack effect conditions
        if (!((LegacyWeapon) this).canAttack(rightClick, actionHand)) return WeaponAttackResult.NO_ATTACK;

        // Check for durability
        DurabilityItem durItem = DurabilityItem.from(getPlayer(), getNBTItem(), actionHand.toBukkit());
        if (durItem != null && durItem.isBroken()) return WeaponAttackResult.DURABILITY;

        // Apply weapon instantaneous costs
        PlayerMetadata stats = getPlayerData().getStats().newTemporary(actionHand);
        if (!checkWeaponCosts(true)) return WeaponAttackResult.WEAPON_COSTS;

        // Check for Bukkit event
        UntargetedWeaponUseEvent called = new UntargetedWeaponUseEvent(playerData, this);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled()) return WeaponAttackResult.BUKKIT_EVENT;

        // Attack is ready to be performed.
        // Apply weapon costs
        final double attackDelay = 1 / requireNonZero(stats.getStat("ATTACK_SPEED"), MMOItems.plugin.getConfig().getDouble("default.attack-speed"));
        applyWeaponCosts(attackDelay);

        // Apply weapon attack effect
        final PlayerMetadata caster = playerData.getMMOPlayerData().getStatMap().cache(actionHand);
        ((LegacyWeapon) this).applyAttackEffect(caster, actionHand);

        // Apply durability loss
        if (durItem != null) durItem.decreaseDurability(1).updateInInventory();
        return WeaponAttackResult.SUCCESS;
    }

    @Deprecated
    protected Location getGround(Location loc) {
        for (int j = 0; j < 20; j++) {
            if (loc.getBlock().getType().isSolid()) return loc;
            loc.add(0, -1, 0);
        }
        return loc;
    }

    /**
     * @return First argument, or second if zero or lower
     */
    protected double requireNonZero(double number, double elseNumber) {
        return number <= 0 ? elseNumber : number;
    }
}
