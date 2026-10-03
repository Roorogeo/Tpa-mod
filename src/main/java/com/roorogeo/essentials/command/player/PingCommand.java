package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /ping [player]}: connection latency in milliseconds.
 */
public final class PingCommand extends EssentialsCommand {
	public PingCommand() {
		super("ping", PermissionNodes.PING);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			ServerPlayer player = player(ctx.getSource());
			send(ctx.getSource(), "ping.self", "ping", player.connection.latency());
			return 1;
		})).then(argument("player", StringArgumentType.word())
				.requires(requires(PermissionNodes.PING_OTHERS))
				.suggests(PlayerLookup.ONLINE)
				.executes(this.run(ctx -> {
					ServerPlayer target = onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
					send(ctx.getSource(), "ping.other", "player", DisplayNames.of(target), "ping", target.connection.latency());
					return 1;
				})));
	}
}
