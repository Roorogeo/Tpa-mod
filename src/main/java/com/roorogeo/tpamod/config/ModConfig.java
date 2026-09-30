package com.roorogeo.tpamod.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import com.roorogeo.tpamod.TpaMod;

/**
 * Plain JSON config stored at config/tpamod.json.
 */
public final class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("tpamod.json");

	private static ModConfig instance = new ModConfig();

	/** Seconds a player has to stand still before teleporting. 0 disables the warmup. */
	public int warmupSeconds = 10;
	/** How far (in blocks) a player may drift during the warmup before it is cancelled. */
	public double moveTolerance = 0.2;
	/** Seconds before an unanswered /tpa request expires. */
	public int requestTimeoutSeconds = 60;
	/** Homes a player gets when no permission or option says otherwise. */
	public int defaultMaxHomes = 3;
	/** Values checked for tpamod.homes.limit.<n> permission nodes. The highest granted one wins. */
	public List<Integer> homeLimitSteps = List.of(1, 2, 3, 5, 10, 15, 20, 25, 50, 100);

	public static ModConfig get() {
		return instance;
	}

	public static void load() {
		ModConfig config = null;

		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				config = GSON.fromJson(reader, ModConfig.class);
			} catch (Exception e) {
				TpaMod.LOGGER.error("Failed to read {}, using defaults", PATH, e);
			}
		}

		if (config == null) {
			config = new ModConfig();
		}

		if (config.homeLimitSteps == null) {
			config.homeLimitSteps = List.of();
		}

		instance = config;
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());

			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			TpaMod.LOGGER.error("Failed to write {}", PATH, e);
		}
	}
}
