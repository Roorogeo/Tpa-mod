package com.roorogeo.essentials.data;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.storage.LevelResource;

import net.fabricmc.loader.api.FabricLoader;

import com.roorogeo.essentials.Essentials;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.storage.AsyncFileWriter;

/**
 * One-time import of homes from the home mod this server used before, which kept everything in
 * {@code homewarps.json}:
 * <pre>{@code
 * {
 *   "warps": { ... },
 *   "homes": {
 *     "<player uuid>": {
 *       "<home name>": {"dimension": "minecraft:overworld", "x": 1.5, "y": 64, "z": 2.5, "yaw": 0, "pitch": 0}
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>The file is looked for in the config folder (and one level of sub-folders), the server folder,
 * the world folder and the world's data folder. It is read on the I/O thread at startup together with
 * the rest of the data. Every home is added to its owner's Essentials data, even for players who
 * haven't joined since Essentials was installed, and home limits are ignored, so nobody loses a home
 * (players over their limit keep every home but can't add new ones). A home that already exists in
 * Essentials under the same name is kept as it is. Afterwards the file is renamed to
 * {@code homewarps.json.imported} so the import runs only once. Warps in the file are not imported.
 */
public final class HomewarpsImport {
	public static final String FILE_NAME = "homewarps.json";
	private static final String DONE_SUFFIX = ".imported";

	/** The file that was found and its parsed "homes" object. */
	public record Found(Path file, JsonObject homes) {
	}

	private HomewarpsImport() {
	}

	/** Finds and parses homewarps.json on the I/O thread. Empty when there is nothing to import. */
	public static CompletableFuture<Optional<Found>> readAsync(MinecraftServer server) {
		if (!ConfigManager.config().homes.importHomewarps) {
			return CompletableFuture.completedFuture(Optional.empty());
		}

		Path configDir = FabricLoader.getInstance().getConfigDir();
		List<Path> candidates = List.of(
				configDir.resolve(FILE_NAME),
				FabricLoader.getInstance().getGameDir().resolve(FILE_NAME),
				server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME),
				server.getWorldPath(LevelResource.DATA).resolve(FILE_NAME));

		return CompletableFuture.supplyAsync(() -> {
			Path file = find(candidates, configDir);

			if (file == null) {
				return Optional.empty();
			}

			try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
				JsonElement root = JsonParser.parseReader(reader);
				JsonObject homes = root.isJsonObject() && root.getAsJsonObject().get("homes") instanceof JsonObject object ? object : new JsonObject();
				return Optional.of(new Found(file, homes));
			} catch (Exception e) {
				throw new CompletionException(new IOException("Could not read " + file, e));
			}
		}, AsyncFileWriter.executor());
	}

	private static Path find(List<Path> candidates, Path configDir) {
		for (Path candidate : candidates) {
			if (Files.isRegularFile(candidate)) {
				return candidate.toAbsolutePath().normalize();
			}
		}

		// Many mods keep their files in config/<mod id>/.
		try (Stream<Path> files = Files.find(configDir, 2, (path, attributes) -> attributes.isRegularFile() && path.getFileName().toString().equals(FILE_NAME))) {
			return files.findFirst().map(path -> path.toAbsolutePath().normalize()).orElse(null);
		} catch (IOException e) {
			Essentials.LOGGER.warn("Could not search {} for {}", configDir, FILE_NAME, e);
			return null;
		}
	}

	/** Adds the homes to the player data (server thread, after player data was installed). */
	public static void install(MinecraftServer server, Optional<Found> found) {
		if (found.isEmpty()) {
			return;
		}

		Path file = found.get().file();
		int players = 0;
		int imported = 0;
		int kept = 0;
		int skipped = 0;

		for (Map.Entry<String, JsonElement> player : found.get().homes().entrySet()) {
			UUID uuid;

			try {
				uuid = UUID.fromString(player.getKey());
			} catch (IllegalArgumentException e) {
				Essentials.LOGGER.warn("{}: skipping homes of \"{}\", which is not a player UUID", FILE_NAME, player.getKey());
				continue;
			}

			if (!(player.getValue() instanceof JsonObject homes) || homes.isEmpty()) {
				continue;
			}

			PlayerData data = PlayerDataStore.getOrCreate(uuid, server.services().nameToIdCache().get(uuid).map(NameAndId::name).orElse(""));
			Set<String> added = new HashSet<>();
			players++;

			for (Map.Entry<String, JsonElement> home : homes.entrySet()) {
				Location location = location(home.getValue());

				if (location == null) {
					Essentials.LOGGER.warn("{}: skipping home \"{}\" of {}, it has no valid position", FILE_NAME, home.getKey(), uuid);
					skipped++;
					continue;
				}

				String name = homeName(home.getKey(), data, added);

				if (name == null) {
					kept++;
					continue;
				}

				if (!name.equals(home.getKey())) {
					Essentials.LOGGER.info("{}: home \"{}\" of {} is imported as \"{}\"", FILE_NAME, home.getKey(), uuid, name);
				}

				data.homes.put(name, location);
				added.add(name);
				imported++;
			}

			data.markDirty();
		}

		PlayerDataStore.saveDirty();
		Path done = file.resolveSibling(file.getFileName() + DONE_SUFFIX);

		// Queued after the player files, on the same single I/O thread, so it runs once they are written.
		AsyncFileWriter.executor().execute(() -> {
			try {
				Files.move(file, done, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				Essentials.LOGGER.error("Imported {} but could not rename it to {}; remove it by hand so it isn't imported again", file, done.getFileName(), e);
			}
		});

		Essentials.LOGGER.info("Imported {} homes of {} players from {} ({} already existed in Essentials, {} invalid). The file was renamed to {}",
				imported, players, file, kept, skipped, done.getFileName());
	}

	private static Location location(JsonElement element) {
		if (!(element instanceof JsonObject json)) {
			return null;
		}

		try {
			String dimension = json.has("dimension") ? json.get("dimension").getAsString() : json.has("world") ? json.get("world").getAsString() : "minecraft:overworld";
			return new Location(dimension, json.get("x").getAsDouble(), json.get("y").getAsDouble(), json.get("z").getAsDouble(),
					json.has("yaw") ? json.get("yaw").getAsFloat() : 0, json.has("pitch") ? json.get("pitch").getAsFloat() : 0);
		} catch (RuntimeException e) {
			return null;
		}
	}

	/**
	 * The name the home gets in Essentials: lower case, with characters that can't be typed in a
	 * command argument replaced by '_'. Null when the player already had a home with that name in
	 * Essentials before the import; that home is kept.
	 */
	private static String homeName(String original, PlayerData data, Set<String> added) {
		StringBuilder name = new StringBuilder();

		for (char c : original.toLowerCase(Locale.ROOT).toCharArray()) {
			boolean allowed = c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.' || c == '+';
			name.append(allowed ? c : '_');
		}

		if (name.isEmpty()) {
			name.append(ConfigManager.config().homes.defaultHomeName);
		}

		String result = name.toString();

		if (!data.homes.containsKey(result)) {
			return result;
		}

		if (!added.contains(result)) {
			return null;
		}

		// Two names in the file that become the same here ("Base" and "base"): number the second one.
		for (int i = 2; ; i++) {
			String numbered = result + i;

			if (!data.homes.containsKey(numbered)) {
				return numbered;
			}
		}
	}
}
