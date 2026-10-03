package com.roorogeo.essentials;

import java.io.IOException;

import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import com.roorogeo.essentials.combat.CombatService;
import com.roorogeo.essentials.command.CommandRegistry;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.DataLoader;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.event.ConnectionEvents;
import com.roorogeo.essentials.event.DamageEvents;
import com.roorogeo.essentials.event.InteractionEvents;
import com.roorogeo.essentials.service.AfkService;
import com.roorogeo.essentials.service.ChatService;
import com.roorogeo.essentials.service.FreezeService;
import com.roorogeo.essentials.service.JailService;
import com.roorogeo.essentials.service.VanishService;
import com.roorogeo.essentials.storage.AsyncFileWriter;
import com.roorogeo.essentials.teleport.ChunkLoading;
import com.roorogeo.essentials.teleport.TeleportService;
import com.roorogeo.essentials.teleport.TpaService;

/**
 * Server-side Essentials for Fabric. Vanilla clients need nothing installed: everything is done with
 * commands, chat components, vanilla menus and vanilla packets.
 */
public final class Essentials implements ModInitializer {
	public static final String MOD_ID = "essentials";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static int autosaveTicks;

	@Override
	public void onInitialize() {
		try {
			ConfigManager.apply(ConfigManager.read());
		} catch (IOException | JsonParseException e) {
			LOGGER.error("Could not read config/essentials; using built-in defaults until the files are fixed", e);
		}

		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> CommandRegistry.registerAll(dispatcher));

		ServerLifecycleEvents.SERVER_STARTING.register(DataLoader::start);
		ServerLifecycleEvents.SERVER_STOPPING.register(Essentials::onStopping);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> onStopped());

		// Hold logins until all data is loaded, so nothing ever has to be read on the server thread.
		ServerLoginConnectionEvents.QUERY_START.register((listener, server, sender, synchronizer) -> {
			if (!DataLoader.isReady()) {
				synchronizer.waitFor(DataLoader.ready());
			}
		});

		ServerPlayConnectionEvents.INIT.register((listener, server) -> ConnectionEvents.onInit(listener.getPlayer()));
		ServerPlayerEvents.JOIN.register(ConnectionEvents::onJoin);
		ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> ConnectionEvents.onDisconnect(listener.getPlayer(), server));
		ServerPlayerEvents.LEAVE.register(ConnectionEvents::onLeave);
		ServerPlayerEvents.AFTER_RESPAWN.register(ConnectionEvents::onRespawn);

		ServerLivingEntityEvents.ALLOW_DAMAGE.register(DamageEvents::allowDamage);
		ServerLivingEntityEvents.AFTER_DEATH.register(DamageEvents::afterDeath);

		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(ChatService::onChat);
		ServerMessageEvents.ALLOW_GAME_MESSAGE.register((server, message, overlay) -> overlay || ConnectionEvents.allowGameMessage(server, message));

		InteractionEvents.register();
		ServerTickEvents.END_SERVER_TICK.register(Essentials::tick);
	}

	private static void tick(MinecraftServer server) {
		if (!DataLoader.isReady()) {
			return;
		}

		TeleportService.tick(server);
		ChunkLoading.tick();
		TpaService.tick(server);
		CombatService.tick(server);
		AfkService.tick(server);
		FreezeService.tick(server);
		JailService.tick(server);
		VanishService.tick(server);
		ChatService.tick(server);

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			DamageEvents.tickGodHunger(player);
		}

		int interval = Math.max(1, ConfigManager.config().storage.autosaveSeconds) * 20;

		if (++autosaveTicks >= interval) {
			autosaveTicks = 0;
			PlayerDataStore.saveDirty();
		}
	}

	private static void onStopping(MinecraftServer server) {
		if (!DataLoader.isReady()) {
			return;
		}

		long now = System.currentTimeMillis();

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			PlayerData data = PlayerDataStore.get(player);
			data.lastSeen = now;
			data.markDirty();
		}

		PlayerDataStore.saveDirty();
	}

	private static void onStopped() {
		PlayerDataStore.saveDirty();
		AsyncFileWriter.shutdown();
		DataLoader.clear();
		TeleportService.clear();
		ChunkLoading.clear();
		TpaService.clear();
		CombatService.clear();
	}
}
