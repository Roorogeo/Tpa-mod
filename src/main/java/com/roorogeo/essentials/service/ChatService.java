package com.roorogeo.essentials.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.lucko.fabric.api.permissions.v0.Options;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.text.TextFormatter;
import com.roorogeo.essentials.util.Durations;

/**
 * Public chat formatting, mutes, ignore lists, private messages and social spy.
 */
public final class ChatService {
	/** Reply targets. The console is stored under {@link #CONSOLE}. */
	private static final Map<UUID, UUID> REPLY = new HashMap<>();
	private static final UUID CONSOLE = new UUID(0, 0);
	private static int tickCounter;

	private ChatService() {
	}

	/**
	 * Handles a chat message. Returns true to let vanilla broadcast it unchanged, false when
	 * Essentials blocked it or already delivered it itself.
	 */
	public static boolean onChat(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound boundType) {
		PlayerData data = PlayerDataStore.get(sender);

		if (data.isMuted()) {
			Messages.send(sender, "chat.muted", "time", Durations.format(data.muteRemaining()), "reason", reasonOrDefault(data.muteReason));
			return false;
		}

		if (data.jail != null && !ConfigManager.config().jail.allowChat) {
			Messages.send(sender, "chat.jailed");
			return false;
		}

		EssentialsConfig.Chat config = ConfigManager.config().chat;
		MinecraftServer server = sender.level().getServer();

		if (!config.formatEnabled) {
			List<ServerPlayer> ignoring = new ArrayList<>();

			for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
				if (ignores(recipient, sender)) {
					ignoring.add(recipient);
				}
			}

			if (ignoring.isEmpty()) {
				return true;
			}

			// Deliver the signed message ourselves to everyone except the players ignoring the sender.
			OutgoingChatMessage outgoing = OutgoingChatMessage.create(message);

			for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
				if (!ignoring.contains(recipient)) {
					recipient.sendChatMessage(outgoing, sender.shouldFilterMessageTo(recipient), boundType);
				}
			}

			return false;
		}

		String text = message.signedContent();
		boolean local = config.localRadius >= 0;
		String wrapper = null;

		if (local) {
			if (!config.globalPrefix.isEmpty() && text.startsWith(config.globalPrefix) && text.length() > config.globalPrefix.length()) {
				text = text.substring(config.globalPrefix.length()).stripLeading();
				local = false;
				wrapper = config.globalFormat;
			} else {
				wrapper = config.localFormat;
			}
		}

		Component formatted = TextFormatter.format(format(sender, config), Messages.placeholders(
				"displayname", DisplayNames.of(sender),
				"name", DisplayNames.realName(sender),
				"player", DisplayNames.realName(sender),
				"prefix", meta(sender, "prefix"),
				"suffix", meta(sender, "suffix"),
				"world", Location.of(sender).worldName(),
				"message", TextFormatter.parseUser(text, allowedCodes(sender, PermissionNodes.CHAT_COLOR, PermissionNodes.CHAT_FORMAT, PermissionNodes.CHAT_MAGIC))));

		if (wrapper != null) {
			formatted = TextFormatter.format(wrapper, Messages.placeholders("chat", formatted));
		}

		int heard = 0;
		double radiusSq = (double) config.localRadius * config.localRadius;

		for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
			if (ignores(recipient, sender)) {
				continue;
			}

			if (local && recipient != sender && (recipient.level() != sender.level() || recipient.distanceToSqr(sender) > radiusSq)) {
				continue;
			}

			recipient.sendSystemMessage(formatted);

			if (recipient != sender) {
				heard++;
			}
		}

		if (config.logToConsole) {
			Messages.log(formatted);
		}

		if (local && heard == 0) {
			Messages.send(sender, "chat.nobody-heard", "global-prefix", config.globalPrefix);
		}

		return false;
	}

	/** The first group format the player has (chat.group-formats, in order), or chat.format. */
	private static String format(ServerPlayer player, EssentialsConfig.Chat config) {
		for (Map.Entry<String, String> group : config.groupFormats.entrySet()) {
			if (group.getValue() != null && Perms.check(player, Perms.named(PermissionNodes.CHAT_GROUP, group.getKey()))) {
				return group.getValue();
			}
		}

		return config.format;
	}

	/** Prefix or suffix meta from the permission mod (e.g. LuckPerms), parsed for & codes. */
	private static Component meta(ServerPlayer player, String key) {
		return TextFormatter.parse(Options.get(player, key, ""));
	}

	/** Which & codes a player may use, based on three permission nodes. */
	public static TextFormatter.Allowed allowedCodes(ServerPlayer player, PermissionNodes.Node colors, PermissionNodes.Node formats, PermissionNodes.Node magic) {
		return new TextFormatter.Allowed(Perms.check(player, colors), Perms.check(player, formats), Perms.check(player, magic));
	}

	/** True if {@code recipient} ignores {@code sender} (and the sender isn't exempt). */
	public static boolean ignores(ServerPlayer recipient, ServerPlayer sender) {
		if (recipient == sender) {
			return false;
		}

		PlayerData data = PlayerDataStore.get(recipient.getUUID());
		return data != null && data.ignored.contains(sender.getUUID()) && !Perms.check(sender, PermissionNodes.IGNORE_EXEMPT);
	}

	public static String reasonOrDefault(String reason) {
		return reason == null || reason.isBlank() ? Messages.plain("general.no-reason") : reason;
	}

	/**
	 * Sends a private message from {@code source} (a player or the console) to {@code target}.
	 * Messages to a player who ignores the sender look delivered to the sender but are dropped.
	 */
	public static void sendPrivate(CommandSourceStack source, ServerPlayer target, String text) {
		ServerPlayer sender = source.getPlayer();
		Component senderName = DisplayNames.of(source);
		Component targetName = DisplayNames.of(target);
		TextFormatter.Allowed allowed = sender == null
				? TextFormatter.Allowed.ALL
				: Perms.check(sender, PermissionNodes.MSG_COLOR) ? TextFormatter.Allowed.ALL : TextFormatter.Allowed.NONE;
		Component body = TextFormatter.parseUser(text, allowed);

		Messages.send(source, "msg.format-sender", "receiver", targetName, "sender", senderName, "message", body);
		boolean ignored = sender != null && ignores(target, sender);

		if (!ignored) {
			Messages.send(target, "msg.format-receiver", "receiver", targetName, "sender", senderName, "message", body);
		}

		UUID senderId = sender != null ? sender.getUUID() : CONSOLE;
		REPLY.put(senderId, target.getUUID());

		if (!ignored) {
			REPLY.put(target.getUUID(), senderId);
		}

		if (ConfigManager.config().messaging.notifyAfk && AfkService.isAfk(target)) {
			Messages.send(source, "msg.target-afk", "player", targetName);
		}

		spy(source.getServer(), "msg.format-spy", senderId, target.getUUID(), "sender", senderName, "receiver", targetName, "message", body);
	}

	/** Sends a social spy copy to every spying player who isn't part of the conversation. */
	public static void spy(MinecraftServer server, String key, UUID from, UUID to, Object... placeholders) {
		for (ServerPlayer spy : server.getPlayerList().getPlayers()) {
			if (spy.getUUID().equals(from) || spy.getUUID().equals(to)) {
				continue;
			}

			PlayerData data = PlayerDataStore.get(spy.getUUID());

			if (data != null && data.socialSpy && Perms.check(spy, PermissionNodes.SOCIALSPY)) {
				Messages.send(spy, key, placeholders);
			}
		}
	}

	/** Who {@code source} would reply to, or null. */
	public static @Nullable ServerPlayer replyTarget(CommandSourceStack source) {
		ServerPlayer sender = source.getPlayer();
		UUID target = REPLY.get(sender != null ? sender.getUUID() : CONSOLE);
		return target == null ? null : source.getServer().getPlayerList().getPlayer(target);
	}

	/** True when the stored reply target is the console. */
	public static boolean repliesToConsole(CommandSourceStack source) {
		ServerPlayer sender = source.getPlayer();
		return sender != null && CONSOLE.equals(REPLY.get(sender.getUUID()));
	}

	/** Sends a private message to the console (used by /reply when the last message came from the console). */
	public static void sendToConsole(ServerPlayer sender, String text) {
		Component body = TextFormatter.parseUser(text, Perms.check(sender, PermissionNodes.MSG_COLOR) ? TextFormatter.Allowed.ALL : TextFormatter.Allowed.NONE);
		Component consoleName = Messages.get("general.console-name");
		Messages.send(sender, "msg.format-sender", "receiver", consoleName, "sender", DisplayNames.of(sender), "message", body);
		Messages.log(Messages.get("msg.format-receiver", "receiver", consoleName, "sender", DisplayNames.of(sender), "message", body));
		REPLY.put(sender.getUUID(), CONSOLE);
		REPLY.put(CONSOLE, sender.getUUID());
	}

	public static void forget(UUID player) {
		REPLY.remove(player);
	}

	/** Notifies players whose mute ran out. */
	public static void tick(MinecraftServer server) {
		if (++tickCounter < 20) {
			return;
		}

		tickCounter = 0;
		long now = System.currentTimeMillis();

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			PlayerData data = PlayerDataStore.get(player.getUUID());

			if (data != null && data.mutedUntil > 0 && data.mutedUntil <= now) {
				data.mutedUntil = 0;
				data.muteReason = "";
				data.markDirty();
				Messages.send(player, "mute.expired");
			}
		}
	}
}
