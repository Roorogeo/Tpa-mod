package com.roorogeo.essentials.text;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;

/**
 * Names shown in chat, private messages, lists and the tab list: the nickname (with the configured
 * prefix) when a player has one, otherwise the account name.
 */
public final class DisplayNames {
	private DisplayNames() {
	}

	public static String realName(ServerPlayer player) {
		return player.getGameProfile().name();
	}

	/** Display name of an online player, with a hover showing the real name when nicked. */
	public static MutableComponent of(ServerPlayer player) {
		PlayerData data = PlayerDataStore.get(player.getUUID());
		String realName = realName(player);

		if (data == null || data.nickname == null) {
			return Component.literal(realName);
		}

		Component hover = Component.literal(realName);
		return nickname(data.nickname).withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(hover)));
	}

	/** Display name from stored data, for offline players. */
	public static MutableComponent of(PlayerData data) {
		return data.nickname == null ? Component.literal(data.name) : nickname(data.nickname);
	}

	/** Parses a stored nickname (codes were validated when it was set) and adds the prefix. */
	public static MutableComponent nickname(String nickname) {
		return Component.empty()
				.append(TextFormatter.parse(ConfigManager.config().nick.prefix))
				.append(TextFormatter.parse(nickname));
	}

	/**
	 * Name of whoever ran a command: the player's display name, {@code general.console-name} for the
	 * server console, or the source's own name (command blocks, RCON).
	 */
	public static MutableComponent of(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();

		if (player != null) {
			return of(player);
		}

		return isConsole(source) ? Messages.get("general.console-name") : Component.literal(source.getTextName());
	}

	/** {@link #of(CommandSourceStack)} as plain text. */
	public static String plain(CommandSourceStack source) {
		return of(source).getString();
	}

	/** The server console ("Server" is vanilla's name for it). */
	public static boolean isConsole(CommandSourceStack source) {
		return source.getEntity() == null && "Server".equals(source.getTextName());
	}

	/** Plain-text display name (nickname without colors, or account name). */
	public static String plain(ServerPlayer player) {
		return of(player).getString();
	}
}
