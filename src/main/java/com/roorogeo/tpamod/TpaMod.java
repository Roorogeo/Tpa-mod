package com.roorogeo.tpamod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import com.roorogeo.tpamod.command.AdminCommands;
import com.roorogeo.tpamod.command.HomeCommands;
import com.roorogeo.tpamod.command.TpaCommands;
import com.roorogeo.tpamod.command.WarpCommands;
import com.roorogeo.tpamod.config.ModConfig;
import com.roorogeo.tpamod.data.Homes;
import com.roorogeo.tpamod.data.WarpStore;
import com.roorogeo.tpamod.teleport.TeleportManager;
import com.roorogeo.tpamod.tpa.TpaManager;

public class TpaMod implements ModInitializer {
	public static final String MOD_ID = "tpamod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModConfig.load();
		Homes.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			TpaCommands.register(dispatcher);
			HomeCommands.register(dispatcher);
			WarpCommands.register(dispatcher);
			AdminCommands.register(dispatcher);
		});

		ServerLifecycleEvents.SERVER_STARTED.register(WarpStore::load);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			WarpStore.unload();
			TeleportManager.clear();
			TpaManager.clear();
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			TeleportManager.tick(server);
			TpaManager.tick(server);
		});

		ServerPlayerEvents.LEAVE.register(player -> {
			TeleportManager.cancel(player.getUUID());
			TpaManager.onLeave(player);
		});
	}
}
