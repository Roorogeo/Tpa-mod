package com.roorogeo.essentials.teleport;

import java.util.UUID;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.Location;

/**
 * Finds a random safe surface location for /rtp. Each attempt loads its chunk asynchronously, so a
 * search never stalls the server; up to {@code rtp.max-attempts} spots are tried.
 */
public final class RandomTeleport {
	private RandomTeleport() {
	}

	/** The dimension /rtp uses for this player (their own if allowed, otherwise {@code rtp.target-dimension}). */
	public static @Nullable ServerLevel targetLevel(ServerPlayer player) {
		EssentialsConfig.Rtp config = ConfigManager.config().rtp;
		String current = Location.dimensionId(player.level());

		if (config.allowedDimensions.contains(current)) {
			return player.level();
		}

		return Location.level(player.level().getServer(), config.targetDimension);
	}

	/**
	 * Searches for a location and passes it to {@code onFound}, or null to {@code onFound} when
	 * every attempt failed. Runs on the server thread, spread over several ticks.
	 */
	public static void search(ServerPlayer player, ServerLevel level, Consumer<@Nullable Location> onFound) {
		attempt(player.getUUID(), level, ConfigManager.config().rtp.maxAttempts, onFound);
	}

	private static void attempt(UUID playerId, ServerLevel level, int attemptsLeft, Consumer<@Nullable Location> onFound) {
		MinecraftServer server = level.getServer();
		ServerPlayer player = server.getPlayerList().getPlayer(playerId);

		if (player == null) {
			return;
		}

		if (attemptsLeft <= 0) {
			onFound.accept(null);
			return;
		}

		BlockPos column = randomColumn(level, level.getRandom());

		if (column == null) {
			attempt(playerId, level, attemptsLeft - 1, onFound);
			return;
		}

		ChunkLoading.whenLoaded(level, column, () -> {
			ServerPlayer current = server.getPlayerList().getPlayer(playerId);

			if (current == null) {
				return;
			}

			Location found = check(current, level, column);

			if (found != null) {
				onFound.accept(found);
			} else {
				attempt(playerId, level, attemptsLeft - 1, onFound);
			}
		}, () -> attempt(playerId, level, attemptsLeft - 1, onFound));
	}

	private static @Nullable BlockPos randomColumn(ServerLevel level, RandomSource random) {
		EssentialsConfig.Rtp config = ConfigManager.config().rtp;
		double centerX;
		double centerZ;

		if ("spawn".equalsIgnoreCase(config.center.trim())) {
			BlockPos spawn = level.getRespawnData().pos();
			centerX = spawn.getX();
			centerZ = spawn.getZ();
		} else {
			String[] parts = config.center.split(",");

			try {
				centerX = Double.parseDouble(parts[0].trim());
				centerZ = Double.parseDouble(parts[1].trim());
			} catch (RuntimeException e) {
				centerX = 0;
				centerZ = 0;
			}
		}

		int min = Math.max(0, config.minRadius);
		int max = Math.max(min + 1, config.maxRadius);
		double angle = random.nextDouble() * Math.PI * 2;
		double distance = min + random.nextDouble() * (max - min);
		int x = (int) Math.floor(centerX + Math.cos(angle) * distance);
		int z = (int) Math.floor(centerZ + Math.sin(angle) * distance);

		if (config.respectWorldBorder) {
			WorldBorder border = level.getWorldBorder();

			if (!border.isWithinBounds(x + 0.5, z + 0.5)) {
				return null;
			}
		}

		return new BlockPos(x, level.getSeaLevel(), z);
	}

	private static @Nullable Location check(ServerPlayer player, ServerLevel level, BlockPos column) {
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
		BlockPos feet = new BlockPos(column.getX(), y, column.getZ());

		if (y <= level.getMinY() || isBlacklisted(level.getBiome(feet))) {
			return null;
		}

		Vec3 target = new Vec3(column.getX() + 0.5, y, column.getZ() + 0.5);

		if (!SafeLocations.isSafe(player, level, target, false)) {
			return null;
		}

		return Location.of(level, target, player.getYRot(), player.getXRot());
	}

	private static boolean isBlacklisted(Holder<Biome> biome) {
		return biome.unwrapKey()
				.map(key -> ConfigManager.config().rtp.blacklistedBiomes.contains(key.identifier().toString()))
				.orElse(false);
	}
}
