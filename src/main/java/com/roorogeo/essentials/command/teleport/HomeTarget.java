package com.roorogeo.essentials.command.teleport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * Parses home arguments shared by /home, /sethome and /delhome: {@code name} for your own home or
 * {@code player:name} for someone else's (needs the matching {@code .others} node).
 *
 * @param data the owner's data
 * @param name home name (lower case), may be null when none was typed
 * @param other true when the home belongs to another player
 */
record HomeTarget(PlayerData data, @Nullable String name, boolean other) {
	static HomeTarget parse(CommandSourceStack source, ServerPlayer self, @Nullable String raw, PermissionNodes.Node othersNode)
			throws CommandSyntaxException {
		if (raw == null) {
			return new HomeTarget(PlayerDataStore.get(self), null, false);
		}

		int colon = raw.indexOf(':');

		if (colon < 0) {
			return new HomeTarget(PlayerDataStore.get(self), raw.toLowerCase(Locale.ROOT), false);
		}

		if (!Perms.check(source, othersNode)) {
			throw HomeErrors.noPermission();
		}

		String owner = raw.substring(0, colon);
		String name = raw.substring(colon + 1).toLowerCase(Locale.ROOT);
		PlayerData data = PlayerLookup.known(source.getServer(), owner);

		if (data == null) {
			throw HomeErrors.neverJoined(owner);
		}

		boolean other = !data.uuid.equals(self.getUUID());
		return new HomeTarget(data, name.isEmpty() ? null : name, other);
	}

	/** Suggests your homes, and {@code player:home} entries when allowed and a colon was typed. */
	static SuggestionProvider<CommandSourceStack> suggestions(PermissionNodes.Node othersNode) {
		return (CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) -> suggest(ctx.getSource(), builder, othersNode);
	}

	private static CompletableFuture<Suggestions> suggest(CommandSourceStack source, SuggestionsBuilder builder, PermissionNodes.Node othersNode) {
		ServerPlayer player = source.getPlayer();
		List<String> options = new ArrayList<>();

		if (player != null) {
			options.addAll(PlayerDataStore.get(player).homes.keySet());
		}

		String input = builder.getRemaining();
		int colon = input.indexOf(':');

		if (colon > 0 && Perms.check(source, othersNode)) {
			PlayerData data = PlayerLookup.known(source.getServer(), input.substring(0, colon));

			if (data != null) {
				for (String home : data.homes.keySet()) {
					options.add(data.name + ":" + home);
				}
			}
		}

		return SharedSuggestionProvider.suggest(options, builder);
	}
}
