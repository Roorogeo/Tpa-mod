package com.roorogeo.essentials.data;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.jspecify.annotations.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelData;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.storage.AsyncFileWriter;

/**
 * The location set with {@code /setspawn}, stored in {@code spawn.json}. When none is set, the
 * vanilla world spawn is used.
 */
public final class SpawnStore {
	private static final Path PATH = ConfigManager.DIRECTORY.resolve("spawn.json");
	private static @Nullable Location spawn;

	private SpawnStore() {
	}

	public static CompletableFuture<Optional<Location>> readAsync() {
		return CompletableFuture.supplyAsync(() -> {
			if (!Files.exists(PATH)) {
				return Optional.<Location>empty();
			}

			try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				return Optional.ofNullable(ConfigManager.GSON.fromJson(reader, Location.class));
			} catch (Exception e) {
				throw new CompletionException(e);
			}
		}, AsyncFileWriter.executor());
	}

	public static void install(Optional<Location> loaded) {
		spawn = loaded.orElse(null);
	}

	public static void set(Location location) {
		spawn = location;
		AsyncFileWriter.write(PATH, ConfigManager.GSON.toJson(location));
	}

	public static void clear() {
		spawn = null;
	}

	/** The Essentials spawn, or the vanilla world spawn if none is set. */
	public static Location get(MinecraftServer server) {
		if (spawn != null && spawn.level(server) != null) {
			return spawn;
		}

		LevelData.RespawnData data = server.getRespawnData();
		ServerLevel level = server.getLevel(data.dimension());

		if (level == null) {
			level = server.overworld();
		}

		return new Location(Location.dimensionId(level), data.pos().getX() + 0.5, data.pos().getY(), data.pos().getZ() + 0.5, data.yaw(), data.pitch());
	}
}
