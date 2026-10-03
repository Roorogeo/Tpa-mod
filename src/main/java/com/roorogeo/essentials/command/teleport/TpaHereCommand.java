package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.teleport.TpaService;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /tpahere <player>}: ask a player to teleport to you.
 */
public final class TpaHereCommand extends EssentialsCommand {
	public TpaHereCommand() {
		super("tpahere", PermissionNodes.TPAHERE);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.ONLINE)
				.executes(this.run(ctx -> {
					ServerPlayer sender = player(ctx.getSource());
					ServerPlayer target = onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));

					if (sender == target) {
						throw error("tpa.self");
					}

					TpaService.send(sender, target, true);
					return 1;
				})));
	}
}
