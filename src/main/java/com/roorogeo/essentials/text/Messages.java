package com.roorogeo.essentials.text;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.Essentials;
import com.roorogeo.essentials.config.ConfigManager;

/**
 * Builds and sends messages from {@code messages.json}.
 *
 * <p>Placeholders are given as alternating name/value pairs:
 * {@code Messages.send(player, "home.set", "home", name, "count", 2, "max", 3)}.
 * Values may be strings, numbers or components. A message whose text is empty is never sent.
 */
public final class Messages {
	private Messages() {
	}

	/** Placeholder map from name/value pairs, with {@code {tag}} always available. */
	public static Map<String, Object> placeholders(Object... pairs) {
		if (pairs.length % 2 != 0) {
			throw new IllegalArgumentException("Placeholders must be name/value pairs");
		}

		Map<String, Object> map = new HashMap<>();

		for (int i = 0; i < pairs.length; i += 2) {
			map.put(String.valueOf(pairs[i]), pairs[i + 1]);
		}

		map.putIfAbsent("tag", TextFormatter.parse(ConfigManager.message("tag")));
		return map;
	}

	public static boolean isEnabled(String key) {
		return !ConfigManager.message(key).isEmpty();
	}

	/** The message as a component (empty component if the message is disabled). */
	public static MutableComponent get(String key, Object... pairs) {
		return TextFormatter.format(ConfigManager.message(key), placeholders(pairs));
	}

	/** The message as plain text, e.g. for kick screens and log lines. */
	public static String plain(String key, Object... pairs) {
		return get(key, pairs).getString();
	}

	public static void send(CommandSourceStack source, String key, Object... pairs) {
		if (isEnabled(key)) {
			source.sendSystemMessage(get(key, pairs));
		}
	}

	public static void send(ServerPlayer player, String key, Object... pairs) {
		if (isEnabled(key)) {
			player.sendSystemMessage(get(key, pairs));
		}
	}

	public static void actionBar(ServerPlayer player, String key, Object... pairs) {
		if (isEnabled(key)) {
			player.sendOverlayMessage(get(key, pairs));
		}
	}

	/** Sends a message to every online player and the console. */
	public static void broadcast(MinecraftServer server, String key, Object... pairs) {
		if (isEnabled(key)) {
			Component message = get(key, pairs);
			server.getPlayerList().broadcastSystemMessage(message, false);
		}
	}

	/** Logs a message's plain text to the console (used for chat lines Essentials sends itself). */
	public static void log(Component message) {
		Essentials.LOGGER.info(message.getString());
	}

	/**
	 * A clickable piece of text that runs {@code command}, with hover text.
	 *
	 * @param textKey message key of the visible text
	 * @param hoverKey message key of the hover text
	 */
	public static MutableComponent button(String textKey, String hoverKey, String command, Object... pairs) {
		MutableComponent hover = get(hoverKey, pairs);
		return get(textKey, pairs).withStyle(style -> style
				.withClickEvent(new ClickEvent.RunCommand(command))
				.withHoverEvent(new HoverEvent.ShowText(hover)));
	}
}
