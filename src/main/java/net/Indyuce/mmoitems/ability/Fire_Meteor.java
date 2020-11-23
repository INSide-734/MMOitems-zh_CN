package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.VectorAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.version.VersionSound;

public class Fire_Meteor extends Ability {
	public Fire_Meteor() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("knockback", 1);
		addModifier("radius", 4);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new VectorAbilityResult(ability, stats.getPlayer(), target);
		}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 3, 1);
		new BukkitRunnable() {
			double ti = 0;
			final Location loc = stats.getPlayer().getLocation().clone().add(0, 10, 0);
			final Vector vec = ((VectorAbilityResult) ability).getTarget().multiply(1.3).setY(-1).normalize();

			public void run() {
				ti++;
				if (ti > 40)
					cancel();

				loc.add(vec);
				loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
				loc.getWorld().spawnParticle(Particle.FLAME, loc, 4, .2, .2, .2, 0);
				if (loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid() || loc.getBlock().getType().isSolid()) {
					loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3, .6f);
					loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 10, 2, 2, 2, 0);
					loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 32, 0, 0, 0, .3);
					loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .3);

					double damage = ability.getModifier("damage");
					double knockback = ability.getModifier("knockback");
					double radius = ability.getModifier("radius");
					for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
						if (MMOUtils.canDamage(stats.getPlayer(), entity) && entity.getLocation().distanceSquared(loc) < radius * radius) {
							new AttackResult(damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE).damage(stats.getPlayer(), (LivingEntity) entity);
							entity.setVelocity(entity.getLocation().toVector().subtract(loc.toVector()).multiply(.1 * knockback).setY(.4 * knockback));
						}
					cancel();
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
