package com.roorogeo.essentials.data;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.server.MinecraftServer;

import com.roorogeo.essentials.Essentials;

/**
 * Loads every data file in the background when the server starts.
 *
 * <p>Logins are held back until {@link #ready()} completes (through Fabric's login synchronizer),
 * so commands and events always find the data in memory, and the server thread never waits for disk.
 */
public final class DataLoader {
	private static CompletableFuture<Void> ready = CompletableFuture.completedFuture(null);

	private DataLoader() {
	}

	/** Completes on the server thread once all data is installed. */
	public static CompletableFuture<Void> ready() {
		return ready;
	}

	public static boolean isReady() {
		return ready.isDone();
	}

	public static void start(MinecraftServer server) {
		CompletableFuture<Void> done = new CompletableFuture<>();
		ready = done;

		CompletableFuture<Map<UUID, PlayerData>> players = PlayerDataStore.readAsync();
		CompletableFuture<Map<String, Location>> warps = NamedLocations.WARPS.readAsync();
		CompletableFuture<Map<String, Location>> jails = NamedLocations.JAILS.readAsync();
		CompletableFuture<Optional<Location>> spawn = SpawnStore.readAsync();
		CompletableFuture<JsonObject> kits = KitStore.readAsync();
		CompletableFuture<Optional<HomewarpsImport.Found>> homewarps = HomewarpsImport.readAsync(server);

		CompletableFuture.allOf(players, warps, jails, spawn, kits, homewarps).handle((ignored, error) -> {
			server.execute(() -> {
				install(players, "player data", PlayerDataStore::install);

				// Never import on top of player data that failed to load: saving would replace the real files.
				if (players.isCompletedExceptionally()) {
					Essentials.LOGGER.error("Not importing {} because player data could not be loaded", HomewarpsImport.FILE_NAME);
				} else {
					install(homewarps, HomewarpsImport.FILE_NAME, found -> HomewarpsImport.install(server, found));
				}

				install(warps, "warps.json", NamedLocations.WARPS::install);
				install(jails, "jails.json", NamedLocations.JAILS::install);
				install(spawn, "spawn.json", SpawnStore::install);
				install(kits, "kits.json", json -> KitStore.install(json, server.registryAccess()));
				done.complete(null);
			});
			return null;
		});
	}

	private static <T> void install(CompletableFuture<T> future, String what, Consumer<T> installer) {
		try {
			installer.accept(future.join());
		} catch (Exception e) {
			Essentials.LOGGER.error("Failed to load {}; it will be empty until the problem is fixed and the server restarts", what, e);
		}
	}

	/** Forgets all loaded data (after it was saved on server stop). */
	public static void clear() {
		PlayerDataStore.clear();
		NamedLocations.WARPS.clear();
		NamedLocations.JAILS.clear();
		SpawnStore.clear();
		KitStore.clear();
		ready = CompletableFuture.completedFuture(null);
	}
}
