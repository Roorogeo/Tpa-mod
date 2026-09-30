package com.roorogeo.tpamod.teleport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import com.roorogeo.tpamod.config.ModConfig;
import com.roorogeo.tpamod.data.Location;
import com.roorogeo.tpamod.perm.Perms;
import com.roorogeo.tpamod.util.Msg;

/**
 * Runs the teleport warmup: the player has to stand still for {@link ModConfig#warmupSeconds}
 * seconds, otherwise the teleport is cancelled.
 */
public final class TeleportManager {
	private static final Map<UUID, Warmup> WARMUPS = new HashMap<>();

	private TeleportManager() {
	}

	/**
	 * Starts a teleport for {@code player}.
	 *
	 * @param destination human readable destination, e.g. "home 'base'"
	 * @param target resolved when the warmup finishes, so moving targets (players) are followed;
	 *               returning {@code null} aborts the teleport
	 */
	public static void start(ServerPlayer player, String destination, Supplier<@Nullable Location> target) {
		int seconds = ModConfig.get().warmupSeconds;

		if (seconds <= 0 || Perms.canBypassWarmup(player)) {
			WARMUPS.remove(player.getUUID());
			complete(player, destination, target);
			return;
		}

		Warmup previous = WARMUPS.put(player.getUUID(), new Warmup(destination, target, player.position(), seconds * 20));

		if (previous != null) {
			player.sendSystemMessage(Msg.error("Your teleport to " + previous.destination + " was replaced."));
		}

		player.sendSystemMessage(Msg.info("Teleporting to ")
				.append(Msg.highlight(destination))
				.append(Msg.info(" in " + seconds + " seconds. Don't move!")));
	}

	public static boolean isWarmingUp(UUID player) {
		return WARMUPS.containsKey(player);
	}

	/** Cancels silently, e.g. when the player logs out. */
	public static void cancel(UUID player) {
		WARMUPS.remove(player);
	}

	public static void clear() {
		WARMUPS.clear();
	}

	public static void tick(MinecraftServer server) {
		if (WARMUPS.isEmpty()) {
			return;
		}

		double tolerance = ModConfig.get().moveTolerance;
		double toleranceSq = tolerance * tolerance;
		Iterator<Map.Entry<UUID, Warmup>> it = WARMUPS.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<UUID, Warmup> entry = it.next();
			Warmup warmup = entry.getValue();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

			if (player == null) {
				it.remove();
				continue;
			}

			if (!player.isAlive()) {
				it.remove();
				player.sendSystemMessage(Msg.error("Teleport cancelled, you died."));
				continue;
			}

			if (player.position().distanceToSqr(warmup.start) > toleranceSq) {
				it.remove();
				player.sendSystemMessage(Msg.error("Teleport cancelled, you moved."));
				player.sendOverlayMessage(Msg.error("Teleport cancelled"));
				continue;
			}

			if (warmup.ticksLeft % 20 == 0) {
				player.sendOverlayMessage(Msg.info("Teleporting in ").append(Msg.highlight(String.valueOf(warmup.ticksLeft / 20))).append(Msg.info("... don't move")));
			}

			if (--warmup.ticksLeft <= 0) {
				it.remove();
				complete(player, warmup.destination, warmup.target);
			}
		}
	}

	private static void complete(ServerPlayer player, String destination, Supplier<@Nullable Location> target) {
		Location location = target.get();

		if (location == null) {
			player.sendSystemMessage(Msg.error("Teleport cancelled, " + destination + " is no longer available."));
			return;
		}

		if (!teleport(player, location)) {
			player.sendSystemMessage(Msg.error("Teleport failed, the dimension of " + destination + " doesn't exist."));
			return;
		}

		player.sendSystemMessage(Msg.success("Teleported to ").append(Msg.highlight(destination)).append(Msg.success(".")));
	}

	/** Teleports right away. Returns false if the target dimension doesn't exist. */
	public static boolean teleport(ServerPlayer player, Location location) {
		ServerLevel level = location.level(player.level().getServer());

		if (level == null) {
			return false;
		}

		if (player.isPassenger()) {
			player.stopRiding();
		}

		player.teleport(new TeleportTransition(level, location.position(), Vec3.ZERO, location.yaw(), location.pitch(), TeleportTransition.DO_NOTHING));
		player.resetFallDistance();
		return true;
	}

	private static final class Warmup {
		private final String destination;
		private final Supplier<@Nullable Location> target;
		private final Vec3 start;
		private int ticksLeft;

		private Warmup(String destination, Supplier<@Nullable Location> target, Vec3 start, int ticks) {
			this.destination = destination;
			this.target = target;
			this.start = start;
			this.ticksLeft = ticks;
		}
	}
}
