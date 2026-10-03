package com.roorogeo.essentials.service;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.NamedLocations;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.data.SpawnStore;
import com.roorogeo.essentials.teleport.ChunkLoading;
import com.roorogeo.essentials.teleport.TeleportService;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.Durations;

/**
 * Jails: jailed players are kept inside {@code jail.radius} of their jail, can only use
 * {@code jail.allowed-commands}, and (by default) can't break, place or use things. Time runs while
 * they are online, or also while offline when {@code jail.count-offline-time} is on.
 */
public final class JailService {
	private static int tickCounter;
	private static long lastTick = System.currentTimeMillis();

	private JailService() {
	}

	public static boolean isJailed(ServerPlayer player) {
		PlayerData data = PlayerDataStore.get(player.getUUID());
		return data != null && data.jail != null;
	}

	/**
	 * Jails a player. When they are online they are moved right away; otherwise on their next join.
	 *
	 * @param durationMillis milliseconds, or {@link Durations#PERMANENT}
	 */
	public static void jail(MinecraftServer server, PlayerData data, String jailName, long durationMillis, String reason) {
		PlayerData.Jail jail = new PlayerData.Jail();
		jail.jail = jailName;
		jail.remaining = durationMillis;
		jail.reason = reason;
		ServerPlayer player = server.getPlayerList().getPlayer(data.uuid);

		if (player != null) {
			jail.previous = Location.of(player);
		} else {
			jail.previous = data.logoutLocation;
		}

		data.jail = jail;
		data.markDirty();

		if (player != null) {
			placeInJail(player, data);
			Messages.send(player, "jail.notify", "time", Durations.format(durationMillis), "reason", reason);
		}
	}

	/** Releases a player. Online players are moved to their previous location or spawn. */
	public static void release(MinecraftServer server, PlayerData data) {
		PlayerData.Jail jail = data.jail;

		if (jail == null) {
			return;
		}

		data.jail = null;
		data.markDirty();
		ServerPlayer player = server.getPlayerList().getPlayer(data.uuid);

		if (player == null) {
			return;
		}

		Location destination = "previous".equalsIgnoreCase(ConfigManager.config().jail.releaseLocation) && jail.previous != null
				? jail.previous
				: SpawnStore.get(server);
		moveTo(player, destination);
		Messages.send(player, "jail.released");
	}

	public static void onJoin(ServerPlayer player) {
		PlayerData data = PlayerDataStore.get(player);

		if (data.jail == null) {
			return;
		}

		if (ConfigManager.config().jail.countOfflineTime && !data.jail.isPermanent() && data.lastSeen > 0) {
			data.jail.remaining = Math.max(0, data.jail.remaining - (System.currentTimeMillis() - data.lastSeen));
			data.markDirty();

			if (data.jail.remaining == 0) {
				release(player.level().getServer(), data);
				return;
			}
		}

		if (!data.jail.placed && data.jail.previous == null) {
			data.jail.previous = Location.of(player);
		}

		placeInJail(player, data);
		Messages.send(player, "jail.time-left", "time", Durations.format(data.jail.remaining));
	}

	private static void placeInJail(ServerPlayer player, PlayerData data) {
		Location jail = data.jail == null ? null : NamedLocations.JAILS.get(data.jail.jail);

		if (jail == null) {
			return;
		}

		data.jail.placed = true;
		data.markDirty();
		moveTo(player, jail);
	}

	private static void moveTo(ServerPlayer player, Location location) {
		MinecraftServer server = player.level().getServer();
		ServerLevel level = location.level(server);

		if (level == null) {
			return;
		}

		ChunkLoading.whenLoaded(level, BlockPos.containing(location.position()), () -> {
			ServerPlayer current = server.getPlayerList().getPlayer(player.getUUID());

			if (current != null) {
				TeleportService.moveNow(current, level, location, false);
			}
		}, () -> {
		});
	}

	public static void tick(MinecraftServer server) {
		if (++tickCounter < 20) {
			return;
		}

		tickCounter = 0;
		long now = System.currentTimeMillis();
		long elapsed = now - lastTick;
		lastTick = now;
		EssentialsConfig.Jail config = ConfigManager.config().jail;

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			PlayerData data = PlayerDataStore.get(player.getUUID());

			if (data == null || data.jail == null) {
				continue;
			}

			if (!data.jail.isPermanent()) {
				data.jail.remaining = Math.max(0, data.jail.remaining - elapsed);
				data.markDirty();

				if (data.jail.remaining == 0) {
					release(server, data);
					continue;
				}
			}

			Location jail = NamedLocations.JAILS.get(data.jail.jail);

			if (jail != null && isOutside(player, jail, config.radius)) {
				moveTo(player, jail);
				Messages.send(player, "jail.escape");
			}
		}
	}

	private static boolean isOutside(ServerPlayer player, Location jail, double radius) {
		return !jail.isIn(player) || player.position().distanceToSqr(jail.position()) > radius * radius;
	}

	/** Releases players in a jail that was deleted, so nobody is stuck in a jail that no longer exists. */
	public static void onJailDeleted(MinecraftServer server, String name) {
		for (PlayerData data : PlayerDataStore.all()) {
			if (data.jail != null && data.jail.jail.equals(name)) {
				release(server, data);
			}
		}
	}

	public static @Nullable String jailName(ServerPlayer player) {
		PlayerData data = PlayerDataStore.get(player.getUUID());
		return data == null || data.jail == null ? null : data.jail.jail;
	}
}
