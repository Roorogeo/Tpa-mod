package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /fly [player]}: toggles flight.
 */
public final class FlyCommand extends EssentialsCommand {
	public FlyCommand() {
		super("fly", PermissionNodes.FLY);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> toggle(ctx.getSource(), player(ctx.getSource()))))
				.then(argument("player", StringArgumentType.word())
						.requires(requires(PermissionNodes.FLY_OTHERS))
						.suggests(PlayerLookup.ONLINE)
						.executes(this.run(ctx -> toggle(ctx.getSource(), onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"))))));
	}

	private static int toggle(CommandSourceStack source, ServerPlayer target) {
		boolean enable = !target.getAbilities().mayfly;
		target.getAbilities().mayfly = enable;

		if (!enable) {
			target.getAbilities().flying = false;
		}

		target.onUpdateAbilities();
		Messages.send(target, enable ? "fly.enabled" : "fly.disabled");

		if (source.getEntity() != target) {
			send(source, enable ? "fly.enabled-other" : "fly.disabled-other", "player", DisplayNames.of(target));
		}

		return 1;
	}
}
