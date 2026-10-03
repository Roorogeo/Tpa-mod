package com.roorogeo.essentials.event;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.combat.CombatService;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.KitStore;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.data.SpawnStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.service.AfkService;
import com.roorogeo.essentials.service.ChatService;
import com.roorogeo.essentials.service.FreezeService;
import com.roorogeo.essentials.service.JailService;
import com.roorogeo.essentials.service.KitService;
import com.roorogeo.essentials.service.VanishService;
import com.roorogeo.essentials.teleport.TeleportRequest;
import com.roorogeo.essentials.teleport.TeleportService;
import com.roorogeo.essentials.teleport.TpaService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;

/**
 * Join and leave handling: player data bookkeeping, custom join/quit messages, MOTD, mail notices,
 * spawn on join, the first-join kit, and cleanup when a player leaves.
 */
public final class ConnectionEvents {
	/** Player whose vanilla join message is about to be broadcast. */
	private static @Nullable UUID joining;
	/** Player whose vanilla leave message is about to be broadcast. */
	private static @Nullable UUID leaving;

	private ConnectionEvents() {
	}

	/** The play connection was created (before vanilla's join message). */
	public static void onInit(ServerPlayer player) {
		joining = player.getUUID();
	}

	public static void onJoin(ServerPlayer player) {
		joining = null;
		MinecraftServer server = player.level().getServer();
		EssentialsConfig config = ConfigManager.config();
		boolean firstJoin = !PlayerDataStore.exists(player.getUUID());
		PlayerData data = PlayerDataStore.get(player);
		PlayerDataStore.updateName(data, DisplayNames.realName(player));
		data.sessionStart = System.currentTimeMillis();
		data.lastIp = player.getIpAddress();

		if (!config.player.persistGod) {
			data.god = false;
		}

		data.markDirty();

		VanishService.onJoin(player);
		FreezeService.onJoin(player);
		JailService.onJoin(player);

		if (config.joinQuit.customMessages && !hiddenJoinQuit(player)) {
			if (firstJoin && config.joinQuit.firstJoinBroadcast) {
				broadcastVisible(server, player, "join-quit.first-join", "player", DisplayNames.realName(player),
						"displayname", DisplayNames.of(player), "count", PlayerDataStore.all().size());
			} else {
				broadcastVisible(server, player, "join-quit.join", "player", DisplayNames.realName(player), "displayname", DisplayNames.of(player));
			}
		}

		if (config.joinQuit.motd) {
			Messages.send(player, "motd", "player", DisplayNames.realName(player), "displayname", DisplayNames.of(player),
					"online", server.getPlayerList().getPlayerCount());
		}

		int unread = data.unreadMail();

		if (config.mail.notifyOnJoin && unread > 0) {
			Messages.send(player, "mail.unread-join", "count", unread);
		}

		if (data.jail == null && (firstJoin && config.spawn.teleportOnFirstJoin || config.spawn.teleportOnJoin)) {
			TeleportService.start(player, new TeleportRequest("spawn", Messages.get("spawn.destination"), () -> SpawnStore.get(server), false));
		}

		if (firstJoin && !config.kits.firstJoinKit.isBlank()) {
			KitStore.Kit kit = KitStore.get(config.kits.firstJoinKit);

			if (kit != null) {
				KitService.give(player, kit, false);
			}
		}
	}

	/** The connection closed; the player is still in the world. */
	public static void onDisconnect(ServerPlayer player, MinecraftServer server) {
		leaving = player.getUUID();
		CombatService.onDisconnect(player, server);
		PlayerData data = PlayerDataStore.get(player);
		data.lastSeen = System.currentTimeMillis();
		data.sessionStart = 0;
		data.logoutLocation = Location.of(player);
		data.markDirty();
		PlayerDataStore.save(data);

		if (ConfigManager.config().joinQuit.customMessages && !hiddenJoinQuit(player)) {
			broadcastVisible(server, player, "join-quit.quit", "player", DisplayNames.realName(player), "displayname", DisplayNames.of(player));
		}

		TeleportService.forget(player.getUUID());
		TpaService.onLeave(player);
		AfkService.forget(player.getUUID());
		FreezeService.forget(player.getUUID());
		ChatService.forget(player.getUUID());
	}

	/** The player was removed from the player list. */
	public static void onLeave(ServerPlayer player) {
		if (player.getUUID().equals(leaving)) {
			leaving = null;
		}
	}

	private static boolean hiddenJoinQuit(ServerPlayer player) {
		return VanishService.isVanished(player) && ConfigManager.config().vanish.silentJoinQuit;
	}

	/** Sends to everyone who can see {@code subject} (everyone, unless the subject is vanished). */
	private static void broadcastVisible(MinecraftServer server, ServerPlayer subject, String key, Object... placeholders) {
		if (!Messages.isEnabled(key)) {
			return;
		}

		Component message = Messages.get(key, placeholders);

		for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
			if (viewer != subject && VanishService.canSee(viewer, subject)) {
				viewer.sendSystemMessage(message);
			}
		}

		Messages.log(message);
	}

	/**
	 * Decides whether a vanilla game message may be broadcast. Vanilla join and leave messages are
	 * dropped when Essentials sends its own, or when the player is vanished with silent join/quit.
	 */
	public static boolean allowGameMessage(MinecraftServer server, Component message) {
		if (!(message.getContents() instanceof TranslatableContents translatable)) {
			return true;
		}

		String key = translatable.getKey();
		UUID subject;

		if (key.startsWith("multiplayer.player.joined")) {
			subject = joining;
		} else if (key.equals("multiplayer.player.left")) {
			subject = leaving;
		} else {
			return true;
		}

		if (ConfigManager.config().joinQuit.customMessages) {
			return false;
		}

		ServerPlayer player = subject == null ? null : server.getPlayerList().getPlayer(subject);
		return player == null || !hiddenJoinQuit(player);
	}

	/** Respawn: send players without a bed to /spawn, and hint at /back after a death. */
	public static void onRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
		if (alive) {
			return;
		}

		MinecraftServer server = newPlayer.level().getServer();
		EssentialsConfig config = ConfigManager.config();
		PlayerData data = PlayerDataStore.get(newPlayer);

		if (data.jail == null && config.spawn.respawnAtSpawn && newPlayer.getRespawnConfig() == null) {
			TeleportService.start(newPlayer, new TeleportRequest("spawn", Messages.get("spawn.destination"), () -> SpawnStore.get(server), false));
		}

		if (config.back.recordDeaths && data.lastLocation != null && Perms.check(newPlayer, PermissionNodes.BACK_ONDEATH)) {
			Messages.send(newPlayer, "back.death-hint");
		}

		if (data.jail != null) {
			JailService.onJoin(newPlayer);
		}
	}
}
