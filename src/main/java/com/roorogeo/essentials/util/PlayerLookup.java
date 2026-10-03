package com.roorogeo.essentials.util;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.service.VanishService;

/**
 * Resolves player names typed in commands. Vanished players are invisible to sources that can't
 * see them, so they look offline.
 */
public final class PlayerLookup {
	/** Suggests the names of online players the source can see. */
	public static final SuggestionProvider<CommandSourceStack> ONLINE = (ctx, builder) ->
			SharedSuggestionProvider.suggest(visibleNames(ctx.getSource()), builder);

	/** Suggests online names plus every known player name. */
	public static final SuggestionProvider<CommandSourceStack> KNOWN = (ctx, builder) -> {
		List<String> names = new ArrayList<>(visibleNames(ctx.getSource()));

		for (String name : PlayerDataStore.knownNames()) {
			if (!names.contains(name)) {
				names.add(name);
			}
		}

		return SharedSuggestionProvider.suggest(names, builder);
	};

	private PlayerLookup() {
	}

	public static List<String> visibleNames(CommandSourceStack source) {
		List<String> names = new ArrayList<>();

		for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
			if (VanishService.canSee(source, player)) {
				names.add(player.getGameProfile().name());
			}
		}

		return names;
	}

	/** Online player with this exact name (case-insensitive) that {@code source} can see, or null. */
	public static @Nullable ServerPlayer online(CommandSourceStack source, String name) {
		ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(name);
		return player != null && VanishService.canSee(source, player) ? player : null;
	}

	/** Online player regardless of vanish (for admin actions where hiding makes no sense). */
	public static @Nullable ServerPlayer onlineAny(MinecraftServer server, String name) {
		return server.getPlayerList().getPlayerByName(name);
	}

	/** Stored data of a player who has joined before, online or not. */
	public static @Nullable PlayerData known(MinecraftServer server, String name) {
		ServerPlayer player = server.getPlayerList().getPlayerByName(name);
		return player != null ? PlayerDataStore.get(player) : PlayerDataStore.byName(name);
	}
}
