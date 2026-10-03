package com.roorogeo.essentials.command.chat;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.ChatService;

/**
 * {@code /reply <message>}: answer the last private message you sent or received.
 */
public final class ReplyCommand extends EssentialsCommand {
	public ReplyCommand() {
		super("reply", PermissionNodes.REPLY);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("message", StringArgumentType.greedyString())
				.executes(this.run(ctx -> {
					CommandSourceStack source = ctx.getSource();
					String message = StringArgumentType.getString(ctx, "message");
					ServerPlayer self = source.getPlayer();

					if (self != null && ChatService.repliesToConsole(source)) {
						ChatService.sendToConsole(self, message);
						return 1;
					}

					ServerPlayer target = ChatService.replyTarget(source);

					if (target == null) {
						throw error("msg.no-reply");
					}

					ChatService.sendPrivate(source, target, message);
					return 1;
				})));
	}
}
