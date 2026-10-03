package com.roorogeo.essentials.data;

import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.google.gson.reflect.TypeToken;
import org.jspecify.annotations.Nullable;

import com.roorogeo.essentials.Essentials;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.storage.AsyncFileWriter;

/**
 * A JSON file of named locations, used for warps ({@code warps.json}) and jails ({@code jails.json}).
 */
public final class NamedLocations {
	private static final Type TYPE = new TypeToken<TreeMap<String, Location>>() {
	}.getType();

	public static final NamedLocations WARPS = new NamedLocations("warps.json");
	public static final NamedLocations JAILS = new NamedLocations("jails.json");

	private final Path path;
	private final Map<String, Location> entries = new TreeMap<>();

	private NamedLocations(String fileName) {
		this.path = ConfigManager.DIRECTORY.resolve(fileName);
	}

	/** Reads the file on the I/O thread; the returned future yields the parsed map. */
	public CompletableFuture<Map<String, Location>> readAsync() {
		return CompletableFuture.supplyAsync(() -> {
			if (!Files.exists(this.path)) {
				return Map.of();
			}

			try (Reader reader = Files.newBufferedReader(this.path, StandardCharsets.UTF_8)) {
				Map<String, Location> loaded = ConfigManager.GSON.fromJson(reader, TYPE);
				return loaded == null ? Map.<String, Location>of() : loaded;
			} catch (Exception e) {
				throw new CompletionException(e);
			}
		}, AsyncFileWriter.executor());
	}

	/** Installs loaded entries (server thread). */
	public void install(Map<String, Location> loaded) {
		this.entries.clear();
		this.entries.putAll(loaded);
		Essentials.LOGGER.info("Loaded {} entries from {}", this.entries.size(), this.path.getFileName());
	}

	public Map<String, Location> all() {
		return Collections.unmodifiableMap(this.entries);
	}

	public @Nullable Location get(String name) {
		return this.entries.get(name);
	}

	public void set(String name, Location location) {
		this.entries.put(name, location);
		this.save();
	}

	public boolean remove(String name) {
		if (this.entries.remove(name) == null) {
			return false;
		}

		this.save();
		return true;
	}

	public void clear() {
		this.entries.clear();
	}

	private void save() {
		AsyncFileWriter.write(this.path, ConfigManager.GSON.toJson(this.entries, TYPE));
	}
}
