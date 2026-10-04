package com.roorogeo.essentials.config;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import net.fabricmc.loader.api.FabricLoader;

import com.roorogeo.essentials.Essentials;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.storage.AsyncFileWriter;

/**
 * Loads {@code config.json} and {@code messages.json} from {@code config/essentials/}.
 *
 * <p>Both files are read once during mod initialization (before the server thread exists) and on
 * {@code /essentials reload}, where they are read on the background I/O thread. Writes always go
 * through {@link AsyncFileWriter}.
 */
public final class ConfigManager {
	public static final Path DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve(Essentials.MOD_ID);
	private static final Path CONFIG_PATH = DIRECTORY.resolve("config.json");
	private static final Path MESSAGES_PATH = DIRECTORY.resolve("messages.json");

	/** Gson for config files: kebab-case keys, pretty printed. */
	public static final Gson GSON = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.serializeNulls()
			.create();

	private static final Type MESSAGES_TYPE = new TypeToken<LinkedHashMap<String, String>>() {
	}.getType();

	private static EssentialsConfig config = new EssentialsConfig();
	private static Map<String, String> messages = DefaultMessages.create();

	private ConfigManager() {
	}

	public static EssentialsConfig config() {
		return config;
	}

	/** Raw text of a message, or the key itself if it doesn't exist (so typos are visible). */
	public static String message(String key) {
		String value = messages.get(key);
		return value == null ? key : value;
	}

	/** Parsed contents of both files, produced off the server thread by {@link #read()}. */
	public record Loaded(EssentialsConfig config, Map<String, String> messages) {
	}

	/**
	 * Reads and parses both files, filling in missing keys. Does file I/O, so call it at startup
	 * or from a background thread, then hand the result to {@link #apply(Loaded)}.
	 *
	 * @throws IOException or {@link JsonParseException} if a file can't be read
	 */
	public static Loaded read() throws IOException {
		Files.createDirectories(DIRECTORY);
		return new Loaded(readConfig(), readMessages());
	}

	/** Activates loaded settings and queues the completed files to be written back. */
	public static void apply(Loaded loaded) {
		config = loaded.config();
		messages = loaded.messages();
		AsyncFileWriter.write(CONFIG_PATH, GSON.toJson(config));
		AsyncFileWriter.write(MESSAGES_PATH, GSON.toJson(messages));
	}

	/**
	 * Re-reads both files on the background I/O thread and applies them on the server thread.
	 * The returned future completes (on the server thread) once the new settings are active.
	 */
	public static CompletableFuture<Void> reloadAsync(Executor serverExecutor) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return read();
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		}, AsyncFileWriter.executor()).thenAcceptAsync(ConfigManager::apply, serverExecutor);
	}

	/**
	 * Replaces null fields with their defaults, also inside every section (e.g. {@code "teleport": {"sound": null}}),
	 * so a stray null in config.json can never cause an error later.
	 */
	private static void repairNulls(Object loaded, Object defaults) throws IllegalAccessException {
		for (Field field : loaded.getClass().getFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			Object value = field.get(loaded);

			if (value == null) {
				field.set(loaded, field.get(defaults));
			} else if (field.getType().getDeclaringClass() == EssentialsConfig.class) {
				repairNulls(value, field.get(defaults));
			}
		}
	}

	private static EssentialsConfig readConfig() throws IOException {
		EssentialsConfig loaded = null;

		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
				loaded = GSON.fromJson(reader, EssentialsConfig.class);
			}
		}

		if (loaded == null) {
			loaded = new EssentialsConfig();
		}

		fillDefaults(loaded);
		return loaded;
	}

	/** Gson leaves fields null when the JSON has an explicit null, and replaces whole maps; repair both. */
	private static void fillDefaults(EssentialsConfig loaded) {
		try {
			repairNulls(loaded, new EssentialsConfig());
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}

		Map<String, EssentialsConfig.CommandSettings> commands = new LinkedHashMap<>();

		for (Map.Entry<String, EssentialsConfig.CommandSettings> entry : EssentialsConfig.defaultCommands().entrySet()) {
			EssentialsConfig.CommandSettings existing = loaded.commands.get(entry.getKey());
			commands.put(entry.getKey(), existing != null ? existing : entry.getValue());
		}

		// Keep entries we don't know (e.g. from a newer version) so they aren't lost.
		loaded.commands.forEach(commands::putIfAbsent);
		commands.values().forEach(settings -> {
			if (settings.aliases == null) {
				settings.aliases = new java.util.ArrayList<>();
			}
		});
		loaded.commands = commands;

		Map<String, String> permissionDefaults = new LinkedHashMap<>();

		for (PermissionNodes.Node node : PermissionNodes.all().values()) {
			String key = node.configKey();
			String existing = loaded.permissions.defaults == null ? null : loaded.permissions.defaults.get(key);
			permissionDefaults.put(key, existing != null ? existing : node.level().name().toLowerCase(java.util.Locale.ROOT));
		}

		if (loaded.permissions.defaults != null) {
			loaded.permissions.defaults.forEach(permissionDefaults::putIfAbsent);
		}

		loaded.permissions.defaults = permissionDefaults;
	}

	private static Map<String, String> readMessages() throws IOException {
		Map<String, String> result = DefaultMessages.create();

		if (Files.exists(MESSAGES_PATH)) {
			try (Reader reader = Files.newBufferedReader(MESSAGES_PATH, StandardCharsets.UTF_8)) {
				Map<String, String> loaded = GSON.fromJson(reader, MESSAGES_TYPE);

				if (loaded != null) {
					loaded.forEach((key, value) -> result.put(key, value == null ? "" : value));
				}
			}
		}

		return result;
	}
}
