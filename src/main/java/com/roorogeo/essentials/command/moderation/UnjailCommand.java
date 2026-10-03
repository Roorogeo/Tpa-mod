package com.roorogeo.essentials.command.moderation;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.JailService;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /unjail <player>}.
 */
public final class UnjailCommand extends EssentialsCommand {
	public UnjailCommand() {
		super("unjail", PermissionNodes.UNJAIL);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.KNOWN)
				.executes(this.run(ctx -> {
					PlayerData data = knownPlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));

					if (data.jail == null) {
						throw error("jail.not-jailed", "player", data.name);
					}

					JailService.release(ctx.getSource().getServer(), data);
					send(ctx.getSource(), "jail.unjailed", "player", data.name);
					return 1;
				})));
	}
}
