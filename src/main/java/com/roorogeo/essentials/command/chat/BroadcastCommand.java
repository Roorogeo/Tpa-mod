package com.roorogeo.essentials.command.chat;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.text.TextFormatter;

/**
 * {@code /broadcast <message>}: sends {@code broadcast.format} to everyone. & codes are allowed.
 */
public final class BroadcastCommand extends EssentialsCommand {
	public BroadcastCommand() {
		super("broadcast", PermissionNodes.BROADCAST);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("message", StringArgumentType.greedyString())
				.executes(this.run(ctx -> {
					Messages.broadcast(ctx.getSource().getServer(), "broadcast.format",
							"message", TextFormatter.parseUser(StringArgumentType.getString(ctx, "message"), TextFormatter.Allowed.ALL),
							"sender", ctx.getSource().getDisplayName());
					return 1;
				})));
	}
}
