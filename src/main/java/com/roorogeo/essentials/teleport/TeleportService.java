package com.roorogeo.essentials.teleport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import com.roorogeo.essentials.combat.CombatService;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.Durations;

/**
 * Runs every Essentials teleport: combat check, cooldown, warmup (cancelled by moving or damage),
 * async chunk loading, the safe-destination search, /back recording and the arrival sound.
 *
 * <p>Warmup and cooldown come from {@code commands.<command>.teleport-delay-seconds} /
 * {@code teleport-cooldown-seconds}, falling back to {@code teleport.delay-seconds} /
 * {@code teleport.cooldown-seconds}. A command with its own cooldown tracks it separately from the
 * shared teleport cooldown.
 */
public final class TeleportService {
	private static final String SHARED_COOLDOWN = "teleport";
	private static final Map<UUID, Warmup> WARMUPS = new HashMap<>();
	private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

	private TeleportService() {
	}

	private static final class Warmup {
		private final TeleportRequest request;
		private final Vec3 start;
		private int ticksLeft;

		private Warmup(TeleportRequest request, Vec3 start, int ticks) {
			this.request = request;
			this.start = start;
			this.ticksLeft = ticks;
		}
	}

	private record Timing(int delaySeconds, int cooldownSeconds, String cooldownKey) {
	}

	private static Timing timing(String command) {
		EssentialsConfig config = ConfigManager.config();
		EssentialsConfig.CommandSettings settings = config.commands.get(command);
		int delay = settings != null && settings.teleportDelaySeconds >= 0 ? settings.teleportDelaySeconds : config.teleport.delaySeconds;
		boolean ownCooldown = settings != null && settings.teleportCooldownSeconds >= 0;
		int cooldown = ownCooldown ? settings.teleportCooldownSeconds : config.teleport.cooldownSeconds;
		return new Timing(delay, cooldown, ownCooldown ? command : SHARED_COOLDOWN);
	}

	/**
	 * Checks the combat tag and cooldown for a player-initiated teleport without starting it, telling
	 * the player why when it isn't allowed. Used by /rtp before it spends time searching.
	 */
	public static boolean precheck(ServerPlayer player, String command) {
		Timing timing = timing(command);

		if (CombatService.isTagged(player) && !Perms.check(player, PermissionNodes.COMBAT_COMMAND_BYPASS)) {
			Messages.send(player, "teleport.in-combat", "time", Durations.format(CombatService.remainingMillis(player.getUUID())));
			return false;
		}

		if (timing.cooldownSeconds() > 0 && !Perms.check(player, PermissionNodes.TELEPORT_COOLDOWN_BYPASS)) {
			long remaining = cooldownRemaining(player.getUUID(), timing.cooldownKey());

			if (remaining > 0) {
				Messages.send(player, "teleport.cooldown", "time", Durations.format(remaining));
				return false;
			}
		}

		return true;
	}

	/**
	 * Starts a teleport. Returns false if it was refused right away (combat, cooldown).
	 */
	public static boolean start(ServerPlayer player, TeleportRequest request) {
		Timing timing = timing(request.command());

		if (request.playerInitiated()) {
			if (!precheck(player, request.command())) {
				return false;
			}

			if (timing.cooldownSeconds() > 0 && !ConfigManager.config().teleport.cooldownOnlyOnSuccess
					&& !Perms.check(player, PermissionNodes.TELEPORT_COOLDOWN_BYPASS)) {
				setCooldown(player.getUUID(), timing);
			}
		}

		int delay = request.playerInitiated() ? timing.delaySeconds() : 0;

		if (delay <= 0 || Perms.check(player, PermissionNodes.TELEPORT_DELAY_BYPASS)) {
			Warmup previous = WARMUPS.remove(player.getUUID());

			if (previous != null) {
				Messages.send(player, "teleport.cancelled-replaced", "destination", previous.request.destination());
			}

			execute(player, request, timing);
			return true;
		}

		Warmup previous = WARMUPS.put(player.getUUID(), new Warmup(request, player.position(), delay * 20));

		if (previous != null) {
			Messages.send(player, "teleport.cancelled-replaced", "destination", previous.request.destination());
		}

		Messages.send(player, "teleport.warmup", "destination", request.destination(), "seconds", delay);
		return true;
	}

	public static boolean hasWarmup(UUID player) {
		return WARMUPS.containsKey(player);
	}

	/** Cancels a pending warmup and tells the player why (no message if {@code messageKey} is null). */
	public static void cancel(ServerPlayer player, @Nullable String messageKey) {
		if (WARMUPS.remove(player.getUUID()) != null && messageKey != null) {
			Messages.send(player, messageKey);
		}
	}

	public static void forget(UUID player) {
		WARMUPS.remove(player);
	}

	/** Called when a player takes damage. */
	public static void onDamage(ServerPlayer player) {
		if (ConfigManager.config().teleport.cancelOnDamage && WARMUPS.containsKey(player.getUUID())) {
			cancel(player, "teleport.cancelled-damage");
		}
	}

	public static void tick(MinecraftServer server) {
		if (WARMUPS.isEmpty()) {
			return;
		}

		EssentialsConfig.Teleport config = ConfigManager.config().teleport;
		double toleranceSq = config.moveTolerance * config.moveTolerance;
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
				Messages.send(player, "teleport.cancelled-death");
				continue;
			}

			if (config.cancelOnMove && player.position().distanceToSqr(warmup.start) > toleranceSq) {
				it.remove();
				Messages.send(player, "teleport.cancelled-move");
				continue;
			}

			if (config.warmupActionBar && warmup.ticksLeft % 20 == 0) {
				Messages.actionBar(player, "teleport.warmup-actionbar", "seconds", warmup.ticksLeft / 20);
			}

			if (--warmup.ticksLeft <= 0) {
				it.remove();
				execute(player, warmup.request, timing(warmup.request.command()));
			}
		}
	}

	private static void execute(ServerPlayer player, TeleportRequest request, Timing timing) {
		Location location = request.target().get();

		if (location == null) {
			Messages.send(player, "teleport.destination-gone", "destination", request.destination());
			return;
		}

		MinecraftServer server = player.level().getServer();
		ServerLevel level = location.level(server);

		if (level == null) {
			Messages.send(player, "teleport.world-missing", "destination", request.destination());
			return;
		}

		UUID id = player.getUUID();
		ChunkLoading.whenLoaded(level, BlockPos.containing(location.position()), () -> {
			ServerPlayer current = server.getPlayerList().getPlayer(id);

			if (current == null || !current.isAlive()) {
				return;
			}

			Location safe = SafeLocations.find(current, level, location);

			if (safe == null) {
				Messages.send(current, "teleport.unsafe", "destination", request.destination());
				return;
			}

			moveNow(current, level, safe, true);

			if (request.playerInitiated() && timing.cooldownSeconds() > 0 && ConfigManager.config().teleport.cooldownOnlyOnSuccess
					&& !Perms.check(current, PermissionNodes.TELEPORT_COOLDOWN_BYPASS)) {
				setCooldown(id, timing);
			}

			Messages.send(current, "teleport.success", "destination", request.destination());
		}, () -> {
			ServerPlayer current = server.getPlayerList().getPlayer(id);

			if (current != null) {
				Messages.send(current, "teleport.chunk-timeout", "destination", request.destination());
			}
		});
	}

	/**
	 * Moves the player right now, across dimensions if needed. The destination chunk should already
	 * be loaded. Records the old position for /back when {@code recordBack} is set.
	 */
	public static void moveNow(ServerPlayer player, ServerLevel level, Location location, boolean recordBack) {
		if (recordBack && ConfigManager.config().back.recordTeleports) {
			recordBack(player);
		}

		if (player.isPassenger()) {
			player.stopRiding();
		}

		player.ejectPassengers();
		player.teleport(new TeleportTransition(level, location.position(), Vec3.ZERO, location.yaw(), location.pitch(), TeleportTransition.DO_NOTHING));
		player.resetFallDistance();
		playSound(player);
	}

	public static void recordBack(ServerPlayer player) {
		PlayerData data = PlayerDataStore.get(player);
		data.lastLocation = Location.of(player);
		data.markDirty();
	}

	private static void playSound(ServerPlayer player) {
		EssentialsConfig.Teleport config = ConfigManager.config().teleport;

		if (config.sound.isEmpty()) {
			return;
		}

		Identifier id = Identifier.tryParse(config.sound);
		SoundEvent sound = id == null ? null : BuiltInRegistries.SOUND_EVENT.getValue(id);

		if (sound != null) {
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, config.soundVolume, config.soundPitch);
		}
	}

	private static long cooldownRemaining(UUID player, String key) {
		Map<String, Long> map = COOLDOWNS.get(player);
		Long until = map == null ? null : map.get(key);
		return until == null ? 0 : until - System.currentTimeMillis();
	}

	private static void setCooldown(UUID player, Timing timing) {
		COOLDOWNS.computeIfAbsent(player, k -> new HashMap<>()).put(timing.cooldownKey(), System.currentTimeMillis() + timing.cooldownSeconds() * 1000L);
	}

	public static void clear() {
		WARMUPS.clear();
		COOLDOWNS.clear();
	}
}
