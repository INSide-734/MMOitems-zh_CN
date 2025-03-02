package net.Indyuce.mmoitems.listener;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.MeleeAttackMetadata;
import io.lumine.mythic.lib.entity.ProjectileMetadata;
import io.lumine.mythic.lib.entity.ProjectileType;
import io.lumine.mythic.lib.skill.SimpleSkill;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.trigger.TriggerMetadata;
import io.lumine.mythic.lib.skill.trigger.TriggerType;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.item.SpecialWeaponAttackEvent;
import net.Indyuce.mmoitems.api.interaction.*;
import net.Indyuce.mmoitems.api.interaction.projectile.ArrowParticles;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class ItemUse implements Listener {

    @EventHandler
    public void rightClickEffects(PlayerInteractEvent event) {
        if (!event.hasItem())
            // || event.getHand() != EquipmentSlot.HAND
            return;

        final NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getItem());
        final Type itemType = Type.get(item);
        if (itemType == null) return;

        /*
         * Some consumables must be fully eaten through the vanilla eating
         * animation and are handled there {@link #handleVanillaEatenConsumables(PlayerItemConsumeEvent)}
         */
        final Player player = event.getPlayer();
        final UseItem useItem = itemType.toUseItem(player, item);
        if (useItem instanceof Consumable && ((Consumable) useItem).hasVanillaEating()) return;

        // (BUG FIX) Cancel the event to prevent things like shield blocking
        if (!useItem.checkItemRequirements()) {
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        // Commands & Consumables
        final boolean rightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
        if (rightClick) {
            if (useItem.getPlayerData().getMMOPlayerData().getCooldownMap().isOnCooldown(useItem.getMMOItem())) {
                final double cd = useItem.getPlayerData().getMMOPlayerData().getCooldownMap().getCooldown(useItem.getMMOItem());
                Message.ITEM_ON_COOLDOWN.format(ChatColor.RED, "#left#", MythicLib.plugin.getMMOConfig().decimal.format(cd), "#s#", cd >= 2 ? "s" : "").send(player);
                event.setUseItemInHand(Event.Result.DENY);
                return;
            }

            if (useItem instanceof Consumable) {
                event.setUseItemInHand(Event.Result.DENY);
                Consumable.ConsumableConsumeResult result = ((Consumable) useItem).useOnPlayer(event.getHand(), false);
                if (result == Consumable.ConsumableConsumeResult.CANCEL) return;

                else if (result == Consumable.ConsumableConsumeResult.CONSUME)
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
            }

            useItem.getPlayerData().getMMOPlayerData().getCooldownMap().applyCooldown(useItem.getMMOItem(), useItem.getNBTItem().getStat("ITEM_COOLDOWN"));
            useItem.executeCommands();
        }

        // Target-free weapon effects
        if (useItem instanceof Weapon)
            ((Weapon) useItem).handleUntargetedAttack(rightClick, EquipmentSlot.fromBukkit(event.getHand()));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void meleeAttacks(PlayerAttackEvent event) {

        // Make sure it's a melee attack
        if (!(event.getAttack() instanceof MeleeAttackMetadata)) return;

        final Player player = event.getPlayer();
        final ItemStack weaponUsed = player.getInventory().getItem(((MeleeAttackMetadata) event.getAttack()).getHand().toBukkit());
        final NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(weaponUsed);
        final Type itemType = Type.get(item);
        if (itemType == null || itemType == Type.BLOCK) return;

        // Prevent melee attacks with non-melee weapons
        if (!itemType.hasMeleeAttacks()) {
            event.setCancelled(true);
            return;
        }

        // Check item requirements
        final PlayerData playerData = PlayerData.get(player);
        final Weapon weapon = new Weapon(playerData, item);
        if (!weapon.checkItemRequirements()) {
            event.setCancelled(true);
            return;
        }

        // Apply melee attack
        if (!weapon.handleTargetedAttack(event.getAttack(), event.getAttacker(), event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void specialToolAbilities(BlockBreakEvent event) {
        if (UtilityMethods.isFake(event)) return;

        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
        if (!item.hasType()) return;

        Tool tool = new Tool(PlayerData.get(player), item);
        if (!tool.checkItemRequirements()) {
            event.setCancelled(true);
            return;
        }

        if (tool.miningEffects(block)) event.setCancelled(true);
    }

    @EventHandler
    public void rightClickWeaponInteractions(PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        if (!(event.getRightClicked() instanceof LivingEntity)) return;

        final NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItem(event.getHand()));
        final Type itemType = Type.get(item);
        if (itemType == null) return;

        final LivingEntity target = (LivingEntity) event.getRightClicked();
        if (!UtilityMethods.canTarget(player, target, InteractionType.OFFENSE_ACTION)) return;

        // Check for usability
        final UseItem usableItem = itemType.toUseItem(player, item);
        if (!usableItem.checkItemRequirements()) return;

        // Apply type-specific entity interactions
        final SkillHandler<?> onEntityInteract = usableItem.getMMOItem().getType().onEntityInteract();
        if (onEntityInteract != null) {
            SpecialWeaponAttackEvent called = new SpecialWeaponAttackEvent(usableItem.getPlayerData(), (Weapon) usableItem, target);
            Bukkit.getPluginManager().callEvent(called);
            if (!called.isCancelled())
                new SimpleSkill(onEntityInteract).cast(new TriggerMetadata(usableItem.getPlayerData().getMMOPlayerData(), TriggerType.API, target));
        }
    }

    // TODO: Rewrite this with a custom 'ApplyMMOItemEvent'?
    @EventHandler
    public void gemStonesAndItemStacks(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (event.getAction() != InventoryAction.SWAP_WITH_CURSOR) return;

        final NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getCursor());
        final Type type = Type.get(item);
        if (type == null) return;

        final UseItem useItem = type.toUseItem(player, item);
        if (!useItem.checkItemRequirements()) return;

        if (useItem instanceof ItemSkin) {
            NBTItem picked = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getCurrentItem());
            if (!picked.hasType()) return;

            ItemSkin.ApplyResult result = ((ItemSkin) useItem).applyOntoItem(picked, Type.get(picked.getType()));
            if (result.getType() == ItemSkin.ResultType.NONE) return;

            event.setCancelled(true);
            item.getItem().setAmount(item.getItem().getAmount() - 1);

            if (result.getType() == ItemSkin.ResultType.FAILURE) return;

            event.setCurrentItem(result.getResult());
        }

        if (useItem instanceof GemStone) {
            NBTItem picked = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getCurrentItem());
            if (!picked.hasType()) return;

            GemStone.ApplyResult result = ((GemStone) useItem).applyOntoItem(picked, Type.get(picked.getType()));
            if (result.getType() == GemStone.ResultType.NONE) return;

            event.setCancelled(true);
            item.getItem().setAmount(item.getItem().getAmount() - 1);

            if (result.getType() == GemStone.ResultType.FAILURE) return;

            event.setCurrentItem(result.getResult());
        }

        if (useItem instanceof Consumable && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR)
            if (((Consumable) useItem).useOnItem(event, MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getCurrentItem()))) {
                event.setCancelled(true);
                event.getCursor().setAmount(event.getCursor().getAmount() - 1);
            }
    }

    /**
     * This handler registers arrows from custom MMOItems bows
     */
    @EventHandler
    public void handleCustomBows(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow) || !(event.getEntity() instanceof Player)) return;

        final NBTItem item = NBTItem.get(event.getBow());
        final Type type = Type.get(item.getType());

        if (type != null) {
            final PlayerData playerData = PlayerData.get((Player) event.getEntity());
            final Weapon weapon = new Weapon(playerData, item);
            if (!weapon.checkItemRequirements() || !weapon.checkAndApplyWeaponCosts()) {
                event.setCancelled(true);
                return;
            }

            EquipmentSlot bowSlot = EquipmentSlot.fromBukkit(MMOUtils.getHand(event, playerData.getPlayer()));
            final ProjectileMetadata proj = ProjectileMetadata.create(playerData.getMMOPlayerData(), bowSlot, ProjectileType.ARROW, event.getProjectile());
            proj.setSourceItem(item);
            proj.setCustomDamage(true);
            proj.setDamageMultiplier(MMOUtils.getForce(event));
            if (item.hasTag("MMOITEMS_ARROW_PARTICLES"))
                new ArrowParticles((AbstractArrow) event.getProjectile(), item);
            final AbstractArrow arrow = (AbstractArrow) event.getProjectile();

            // Apply arrow velocity
            final double arrowVelocity = proj.getShooter().getStat("ARROW_VELOCITY");
            if (arrowVelocity > 0) arrow.setVelocity(arrow.getVelocity().multiply(arrowVelocity));
        }
    }

    /**
     * Consumables which can be eaten using the
     * vanilla eating animation are handled here.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void handleVanillaEatenConsumables(PlayerItemConsumeEvent event) {
        final NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(event.getItem());
        final Type itemType = Type.get(item);
        if (itemType == null) return;

        Player player = event.getPlayer();
        UseItem useItem = itemType.toUseItem(player, item);
        if (!useItem.checkItemRequirements()) {
            event.setCancelled(true);
            return;
        }

        if (useItem instanceof Consumable) {

            if (useItem.getPlayerData().getMMOPlayerData().getCooldownMap().isOnCooldown(useItem.getMMOItem())) {
                final double cd = useItem.getPlayerData().getMMOPlayerData().getCooldownMap().getCooldown(useItem.getMMOItem());
                Message.ITEM_ON_COOLDOWN.format(ChatColor.RED, "#left#", MythicLib.plugin.getMMOConfig().decimal.format(cd), "#s#", cd >= 2 ? "s" : "").send(player);
                event.setCancelled(true);
                return;
            }

            org.bukkit.inventory.EquipmentSlot consumeSlot = MMOUtils.getHand(event);
            Consumable.ConsumableConsumeResult result = ((Consumable) useItem).useOnPlayer(consumeSlot, true);

            // No effects are applied and not consumed
            if (result == Consumable.ConsumableConsumeResult.CANCEL) {
                event.setCancelled(true);
                return;
            }

            // Item is not consumed but its effects are applied anyways
            if (result == Consumable.ConsumableConsumeResult.NOT_CONSUME) event.setCancelled(true);

            useItem.getPlayerData().getMMOPlayerData().getCooldownMap().applyCooldown(useItem.getMMOItem(), useItem.getNBTItem().getStat("ITEM_COOLDOWN"));
            useItem.executeCommands();
        }
    }
}
