package com.roorogeo.essentials.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import com.roorogeo.essentials.combat.CombatService;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.service.AfkService;
import com.roorogeo.essentials.service.FreezeService;
import com.roorogeo.essentials.service.VanishService;
import com.roorogeo.essentials.teleport.TeleportService;

/**
 * Damage and death: god mode and other protections, combat tagging, warmup cancelling, /back on death.
 */
public final class DamageEvents {
	private DamageEvents() {
	}

	/** Returns false to cancel the damage. */
	public static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
		if (entity instanceof ServerPlayer player && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && isProtected(player)) {
			return false;
		}

		CombatService.onDamage(entity, source);

		if (entity instanceof ServerPlayer player) {
			TeleportService.onDamage(player);
		}

		return true;
	}

	private static boolean isProtected(ServerPlayer player) {
		EssentialsConfig config = ConfigManager.config();
		PlayerData data = PlayerDataStore.get(player.getUUID());

		if (data == null) {
			return false;
		}

		return data.god
				|| config.vanish.invulnerable && data.vanished
				|| config.freeze.invulnerable && data.frozen
				|| config.afk.invulnerable && AfkService.isAfk(player);
	}

	public static void afterDeath(LivingEntity entity, DamageSource source) {
		if (!(entity instanceof ServerPlayer player)) {
			return;
		}

		CombatService.untag(player.getUUID());
		TeleportService.cancel(player, null);

		if (ConfigManager.config().back.recordDeaths && Perms.check(player, PermissionNodes.BACK_ONDEATH)) {
			PlayerData data = PlayerDataStore.get(player);
			data.lastLocation = Location.of(player);
			data.markDirty();
		}

		if (VanishService.isVanished(player)) {
			VanishService.refresh(player);
		}
	}

	/** Keeps hunger full for players in god mode. */
	public static void tickGodHunger(ServerPlayer player) {
		PlayerData data = PlayerDataStore.get(player.getUUID());

		if (data != null && data.god && ConfigManager.config().player.godPreventsHunger && player.getFoodData().getFoodLevel() < 20) {
			player.getFoodData().setFoodLevel(20);
		}

		if (FreezeService.isFrozen(player)) {
			player.resetFallDistance();
		}
	}
}
