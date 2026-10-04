package com.roorogeo.essentials.data;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jspecify.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.Essentials;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.storage.AsyncFileWriter;

/**
 * Per-player data, one JSON file per UUID in {@code config/essentials/userdata/}.
 *
 * <p>All files are read on the background I/O thread when the server starts, and players can't
 * finish logging in until that is done (see {@link DataLoader}), so data is always in memory when
 * it is needed and the server thread never reads from disk. Changes are written back asynchronously.
 */
public final class PlayerDataStore {
	public static final Path DIRECTORY = ConfigManager.DIRECTORY.resolve("userdata");

	private static final Gson GSON = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
			.disableHtmlEscaping()
			.create();
	private static final Gson PRETTY_GSON = GSON.newBuilder().setPrettyPrinting().create();

	private static final Map<UUID, PlayerData> DATA = new HashMap<>();
	private static final Map<String, UUID> BY_NAME = new HashMap<>();

	private PlayerDataStore() {
	}

	/** Reads every player file on the I/O thread. */
	public static CompletableFuture<Map<UUID, PlayerData>> readAsync() {
		return CompletableFuture.supplyAsync(PlayerDataStore::readAll, AsyncFileWriter.executor());
	}

	/** Installs loaded data (server thread). */
	public static void install(Map<UUID, PlayerData> loaded) {
		for (PlayerData data : loaded.values()) {
			DATA.putIfAbsent(data.uuid, data);
			index(data);
		}

		Essentials.LOGGER.info("Loaded data of {} players", loaded.size());
	}

	private static Map<UUID, PlayerData> readAll() {
		Map<UUID, PlayerData> result = new HashMap<>();

		if (!Files.isDirectory(DIRECTORY)) {
			return result;
		}

		try (DirectoryStream<Path> files = Files.newDirectoryStream(DIRECTORY, "*.json")) {
			for (Path file : files) {
				try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
					PlayerData data = GSON.fromJson(reader, PlayerData.class);

					if (data == null) {
						continue;
					}

					if (data.uuid == null) {
						String fileName = file.getFileName().toString();
						data.uuid = UUID.fromString(fileName.substring(0, fileName.length() - ".json".length()));
					}

					data.normalize();
					result.put(data.uuid, data);
				} catch (Exception e) {
					Essentials.LOGGER.error("Skipping unreadable player file {}", file, e);
				}
			}
		} catch (IOException e) {
			Essentials.LOGGER.error("Failed to list {}", DIRECTORY, e);
		}

		return result;
	}

	private static void index(PlayerData data) {
		if (!data.name.isEmpty()) {
			BY_NAME.put(data.name.toLowerCase(Locale.ROOT), data.uuid);
		}
	}

	public static @Nullable PlayerData get(UUID uuid) {
		return DATA.get(uuid);
	}

	/** Data of an online player, created on first use. */
	public static PlayerData get(ServerPlayer player) {
		return getOrCreate(player.getUUID(), player.getGameProfile().name());
	}

	/**
	 * Data of any player, created when they have none yet (also used to import homes of players who
	 * haven't joined since Essentials was installed). {@code name} may be empty if it isn't known.
	 */
	public static PlayerData getOrCreate(UUID uuid, String name) {
		PlayerData data = DATA.get(uuid);

		if (data == null) {
			data = new PlayerData(uuid, name);
			data.firstJoin = System.currentTimeMillis();
			data.balance = ConfigManager.config().economy.startingBalance;
			data.markDirty();
			DATA.put(data.uuid, data);
			index(data);
		}

		return data;
	}

	/** True if this player has data, i.e. has joined before. */
	public static boolean exists(UUID uuid) {
		return DATA.containsKey(uuid);
	}

	/** Finds a known player by their last username (case-insensitive). */
	public static @Nullable PlayerData byName(String name) {
		UUID uuid = BY_NAME.get(name.toLowerCase(Locale.ROOT));
		return uuid == null ? null : DATA.get(uuid);
	}

	/** Updates the stored name when a player joins with a new one. */
	public static void updateName(PlayerData data, String name) {
		if (!data.name.equals(name)) {
			BY_NAME.remove(data.name.toLowerCase(Locale.ROOT), data.uuid);
			data.name = name;
			index(data);
			data.markDirty();
		}
	}

	public static Collection<PlayerData> all() {
		return Collections.unmodifiableCollection(DATA.values());
	}

	public static Collection<String> knownNames() {
		return DATA.values().stream().map(data -> data.name).filter(name -> !name.isEmpty()).toList();
	}

	/** Queues a write of one player's data. Serialization happens here, the disk write happens later. */
	public static void save(PlayerData data) {
		Gson gson = ConfigManager.config().storage.prettyPrint ? PRETTY_GSON : GSON;
		AsyncFileWriter.write(DIRECTORY.resolve(data.uuid + ".json"), gson.toJson(data));
		data.clearDirty();
	}

	/** Queues writes for every changed player. */
	public static void saveDirty() {
		for (PlayerData data : DATA.values()) {
			if (data.isDirty()) {
				save(data);
			}
		}
	}

	/** Forgets everything (on server stop, after saving). */
	public static void clear() {
		DATA.clear();
		BY_NAME.clear();
	}
}
