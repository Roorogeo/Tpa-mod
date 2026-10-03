package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.VanishService;
import com.roorogeo.essentials.util.Durations;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /seen <player>}: how long a player has been online, or when they were last seen.
 * Vanished players look offline to players who can't see them.
 */
public final class SeenCommand extends EssentialsCommand {
	public SeenCommand() {
		super("seen", PermissionNodes.SEEN);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.KNOWN)
				.executes(this.run(ctx -> {
					CommandSourceStack source = ctx.getSource();
					PlayerData data = knownPlayer(source, StringArgumentType.getString(ctx, "player"));
					ServerPlayer online = online(source, data);
					long now = System.currentTimeMillis();

					if (online != null && VanishService.canSee(source, online)) {
						send(source, "seen.online", "player", data.name, "time", Durations.format(now - data.sessionStart));
					} else {
						long since = online != null ? data.sessionStart : data.lastSeen;
						send(source, "seen.offline", "player", data.name, "time", Durations.format(now - since));
					}

					return 1;
				})));
	}
}
