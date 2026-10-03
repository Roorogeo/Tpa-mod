package com.roorogeo.essentials.service;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;

/**
 * Tab list names: {@code tablist.format} with the nickname and AFK tag, when {@code tab-list.enabled}.
 */
public final class TabListService {
	private TabListService() {
	}

	/** Name for the tab list, or null for the vanilla name (called from the ServerPlayer mixin). */
	public static @Nullable Component displayName(ServerPlayer player) {
		if (!ConfigManager.config().tabList.enabled || !Messages.isEnabled("tablist.format")) {
			return null;
		}

		Component afk = AfkService.isAfk(player) ? Messages.get("tablist.afk-tag") : Component.empty();
		return Messages.get("tablist.format", "displayname", DisplayNames.of(player), "player", DisplayNames.realName(player), "afk", afk);
	}

	/** Sends the player's current tab list name to everyone who can see them. */
	public static void refresh(ServerPlayer player) {
		ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player);

		for (ServerPlayer viewer : player.level().getServer().getPlayerList().getPlayers()) {
			if (VanishService.canSee(viewer, player)) {
				viewer.connection.send(packet);
			}
		}
	}
}
