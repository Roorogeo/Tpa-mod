package com.roorogeo.tpamod.data;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.jspecify.annotations.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import com.roorogeo.tpamod.TpaMod;

/**
 * Server-wide warps, saved to {@code <world>/tpamod/warps.json}.
 */
public final class WarpStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Codec<Map<String, Location>> CODEC = Codec.unboundedMap(Codec.STRING, Location.CODEC);

	private static final Map<String, Location> WARPS = new TreeMap<>();
	private static @Nullable Path path;

	private WarpStore() {
	}

	public static void load(MinecraftServer server) {
		WARPS.clear();
		path = server.getWorldPath(LevelResource.ROOT).resolve(TpaMod.MOD_ID).resolve("warps.json");

		if (!Files.exists(path)) {
			return;
		}

		try (Reader reader = Files.newBufferedReader(path)) {
			JsonElement json = JsonParser.parseReader(reader);
			CODEC.parse(JsonOps.INSTANCE, json)
					.resultOrPartial(error -> TpaMod.LOGGER.error("Failed to parse {}: {}", path, error))
					.ifPresent(WARPS::putAll);
		} catch (Exception e) {
			TpaMod.LOGGER.error("Failed to read {}", path, e);
		}

		TpaMod.LOGGER.info("Loaded {} warp(s)", WARPS.size());
	}

	public static void unload() {
		WARPS.clear();
		path = null;
	}

	public static Map<String, Location> all() {
		return Collections.unmodifiableMap(WARPS);
	}

	public static @Nullable Location get(String name) {
		return WARPS.get(name);
	}

	public static void set(String name, Location location) {
		WARPS.put(name, location);
		save();
	}

	public static boolean remove(String name) {
		if (WARPS.remove(name) == null) {
			return false;
		}

		save();
		return true;
	}

	private static void save() {
		if (path == null) {
			return;
		}

		try {
			Files.createDirectories(path.getParent());
			JsonElement json = CODEC.encodeStart(JsonOps.INSTANCE, WARPS).getOrThrow();

			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(json, writer);
			}
		} catch (IOException | IllegalStateException e) {
			TpaMod.LOGGER.error("Failed to write {}", path, e);
		}
	}
}
