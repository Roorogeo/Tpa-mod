package com.roorogeo.essentials.command.player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /suicide}.
 */
public final class SuicideCommand extends EssentialsCommand {
	public SuicideCommand() {
		super("suicide", PermissionNodes.SUICIDE);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			ServerPlayer player = player(ctx.getSource());
			send(ctx.getSource(), "suicide.done");
			player.kill(player.level());
			return 1;
		}));
	}
}
