package com.roorogeo.essentials.combat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TraceableEntity;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.teleport.TeleportService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;

/**
 * Combat tagging.
 *
 * <p>A player who hurts, or is hurt by, another player is tagged for {@code combat.duration-seconds};
 * every new hit resets the timer. Indirect damage counts: the attacker is found by following owners
 * from the damage source (arrows, tridents, potions, area clouds and TNT are {@link TraceableEntity},
 * tamed pets are {@link OwnableEntity}, end crystal explosions carry the player who broke the crystal).
 */
public final class CombatService {
	private static final int MAX_OWNER_DEPTH = 4;
	private static final Map<UUID, Long> TAGGED_UNTIL = new HashMap<>();
	/** Players whose disconnect was started by the server (kick, ban, shutdown), so they aren't punished. */
	private static final Set<UUID> SERVER_DISCONNECTS = new HashSet<>();
	private static int tickCounter;

	private CombatService() {
	}

	public static boolean isTagged(ServerPlayer player) {
		return remainingMillis(player.getUUID()) > 0;
	}

	public static long remainingMillis(UUID player) {
		Long until = TAGGED_UNTIL.get(player);
		return until == null ? 0 : Math.max(0, until - System.currentTimeMillis());
	}

	/**
	 * Handles a damage event that is going ahead. Tags the victim and the attacking player.
	 */
	public static void onDamage(LivingEntity victim, DamageSource source) {
		EssentialsConfig.Combat config = ConfigManager.config().combat;

		if (!config.enabled || !(victim instanceof ServerPlayer victimPlayer)) {
			return;
		}

		ServerPlayer attacker = resolveAttacker(source);

		if (attacker != null) {
			if (attacker != victimPlayer) {
				tag(victimPlayer, attacker);
				tag(attacker, victimPlayer);
			}

			return;
		}

		if (config.tagOnMobDamage && source.getEntity() instanceof LivingEntity) {
			tag(victimPlayer, null);
		}
	}

	/** The player responsible for the damage, following projectile, TNT and pet owners. */
	public static @Nullable ServerPlayer resolveAttacker(DamageSource source) {
		ServerPlayer owner = owningPlayer(source.getEntity(), 0);
		return owner != null ? owner : owningPlayer(source.getDirectEntity(), 0);
	}

	private static @Nullable ServerPlayer owningPlayer(@Nullable Entity entity, int depth) {
		if (entity == null || depth > MAX_OWNER_DEPTH) {
			return null;
		}

		if (entity instanceof ServerPlayer player) {
			return player;
		}

		if (entity instanceof OwnableEntity ownable) {
			ServerPlayer owner = owningPlayer(ownable.getOwner(), depth + 1);

			if (owner != null) {
				return owner;
			}
		}

		if (entity instanceof TraceableEntity traceable) {
			return owningPlayer(traceable.getOwner(), depth + 1);
		}

		return null;
	}

	private static void tag(ServerPlayer player, @Nullable ServerPlayer opponent) {
		if (Perms.check(player, PermissionNodes.COMBAT_BYPASS)) {
			return;
		}

		EssentialsConfig.Combat config = ConfigManager.config().combat;
		boolean wasTagged = isTagged(player);
		TAGGED_UNTIL.put(player.getUUID(), System.currentTimeMillis() + config.durationSeconds * 1000L);

		if (!wasTagged) {
			enterCombat(player, opponent, config);
		}
	}

	private static void enterCombat(ServerPlayer player, @Nullable ServerPlayer opponent, EssentialsConfig.Combat config) {
		if (config.messageOnStart) {
			if (opponent != null) {
				Messages.send(player, "combat.tagged", "player", DisplayNames.of(opponent), "seconds", config.durationSeconds);
			} else {
				Messages.send(player, "combat.tagged-mob", "seconds", config.durationSeconds);
			}
		}

		if (config.cancelTeleportOnTag && TeleportService.hasWarmup(player.getUUID())) {
			TeleportService.cancel(player, "teleport.cancelled-combat");
		}

		if (config.disableFlyOnTag && player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
			player.getAbilities().mayfly = false;
			player.getAbilities().flying = false;
			player.onUpdateAbilities();
			Messages.send(player, "combat.fly-disabled");
		}

		PlayerData data = PlayerDataStore.get(player);

		if (config.disableGodOnTag && data.god) {
			data.god = false;
			data.markDirty();
			Messages.send(player, "combat.god-disabled");
		}
	}

	/** Clears the tag silently, e.g. on death. */
	public static void untag(UUID player) {
		TAGGED_UNTIL.remove(player);
	}

	/** Remembers that the server is disconnecting this player (called from the disconnect mixin). */
	public static void markServerDisconnect(UUID player) {
		SERVER_DISCONNECTS.add(player);
	}

	/**
	 * Called when a player's connection closes, before they are removed from the world. Kills
	 * combat loggers so their items drop where they logged out.
	 */
	public static void onDisconnect(ServerPlayer player, MinecraftServer server) {
		UUID id = player.getUUID();
		boolean serverInitiated = SERVER_DISCONNECTS.remove(id);
		boolean tagged = isTagged(player);
		TAGGED_UNTIL.remove(id);
		EssentialsConfig.Combat config = ConfigManager.config().combat;

		if (!tagged || !config.killOnLogout || serverInitiated || !server.isRunning() || !player.isAlive()) {
			return;
		}

		if (config.broadcastLogout) {
			Messages.broadcast(server, "combat.logout-broadcast", "player", DisplayNames.of(player));
		}

		player.kill(player.level());
	}

	public static void tick(MinecraftServer server) {
		if (TAGGED_UNTIL.isEmpty() || ++tickCounter < 10) {
			return;
		}

		tickCounter = 0;
		EssentialsConfig.Combat config = ConfigManager.config().combat;
		long now = System.currentTimeMillis();
		Iterator<Map.Entry<UUID, Long>> it = TAGGED_UNTIL.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<UUID, Long> entry = it.next();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

			if (player == null) {
				it.remove();
				continue;
			}

			long remaining = entry.getValue() - now;

			if (remaining <= 0) {
				it.remove();

				if (config.messageOnEnd) {
					Messages.send(player, "combat.untagged");
				}

				if (config.actionBar) {
					player.sendOverlayMessage(net.minecraft.network.chat.Component.empty());
				}

				continue;
			}

			if (config.actionBar) {
				Messages.actionBar(player, "combat.actionbar", "seconds", (remaining + 999) / 1000);
			}
		}
	}

	public static void clear() {
		TAGGED_UNTIL.clear();
		SERVER_DISCONNECTS.clear();
	}
}
