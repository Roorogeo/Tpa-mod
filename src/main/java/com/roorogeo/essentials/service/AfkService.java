package com.roorogeo.essentials.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;

/**
 * AFK status. Activity is read from vanilla's own idle tracker ({@link ServerPlayer#getLastActionTime()}),
 * which moving, looking around, chatting, running commands and interacting all reset.
 */
public final class AfkService {
	/** AFK players, with the last-action time at the moment they went AFK. */
	private static final Map<UUID, Long> AFK = new HashMap<>();
	private static final Map<UUID, String> MESSAGES = new HashMap<>();
	private static int tickCounter;

	private AfkService() {
	}

	public static boolean isAfk(ServerPlayer player) {
		return AFK.containsKey(player.getUUID());
	}

	public static @Nullable String afkMessage(ServerPlayer player) {
		return MESSAGES.get(player.getUUID());
	}

	public static void setAfk(ServerPlayer player, boolean afk, @Nullable String message) {
		if (afk == isAfk(player)) {
			return;
		}

		if (afk) {
			AFK.put(player.getUUID(), player.getLastActionTime());

			if (message != null && !message.isBlank()) {
				MESSAGES.put(player.getUUID(), message);
			}
		} else {
			AFK.remove(player.getUUID());
			MESSAGES.remove(player.getUUID());
			player.sendOverlayMessage(Component.empty());
		}

		Component suffix = afk && message != null && !message.isBlank()
				? Messages.get("afk.message-suffix", "message", message)
				: Component.empty();
		String key = afk ? "afk.now" : "afk.back";
		Object[] placeholders = {"displayname", DisplayNames.of(player), "player", DisplayNames.realName(player), "message", suffix};

		if (ConfigManager.config().afk.broadcast && !VanishService.isVanished(player)) {
			Messages.broadcast(player.level().getServer(), key, placeholders);
		} else {
			Messages.send(player, key, placeholders);
		}

		TabListService.refresh(player);
	}

	public static void forget(UUID player) {
		AFK.remove(player);
		MESSAGES.remove(player);
	}

	public static void tick(MinecraftServer server) {
		if (++tickCounter < 20) {
			return;
		}

		tickCounter = 0;
		EssentialsConfig.Afk config = ConfigManager.config().afk;
		long now = Util.getMillis();

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			long lastAction = player.getLastActionTime();
			Long afkSince = AFK.get(player.getUUID());

			if (afkSince != null && lastAction != afkSince) {
				setAfk(player, false, null);
				continue;
			}

			long idle = now - lastAction;

			if (afkSince == null && config.autoAfkSeconds > 0 && idle >= config.autoAfkSeconds * 1000L) {
				setAfk(player, true, null);
			}

			if (config.autoKickSeconds > 0 && idle >= config.autoKickSeconds * 1000L && !Perms.check(player, PermissionNodes.AFK_KICKEXEMPT)) {
				player.connection.disconnect(Messages.get("afk.kick-reason"));
				continue;
			}

			if (isAfk(player) && config.actionBar) {
				Messages.actionBar(player, "afk.actionbar");
			}
		}
	}
}
