package com.roorogeo.essentials.command.chat;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.ChatService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.text.TextFormatter;

/**
 * {@code /me <action>}: emote ({@code me.format}). Players ignoring you don't see it.
 */
public final class MeCommand extends EssentialsCommand {
	public MeCommand() {
		super("me", PermissionNodes.ME);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("action", StringArgumentType.greedyString())
				.executes(this.run(ctx -> {
					CommandSourceStack source = ctx.getSource();
					ServerPlayer sender = source.getPlayer();
					String text = StringArgumentType.getString(ctx, "action");
					TextFormatter.Allowed allowed = sender == null
							? TextFormatter.Allowed.ALL
							: ChatService.allowedCodes(sender, PermissionNodes.CHAT_COLOR, PermissionNodes.CHAT_FORMAT, PermissionNodes.CHAT_MAGIC);
					Component name = sender == null ? source.getDisplayName() : DisplayNames.of(sender);
					Component message = Messages.get("me.format", "displayname", name, "player", source.getTextName(), "message", TextFormatter.parseUser(text, allowed));

					for (ServerPlayer recipient : source.getServer().getPlayerList().getPlayers()) {
						if (sender == null || !ChatService.ignores(recipient, sender)) {
							recipient.sendSystemMessage(message);
						}
					}

					Messages.log(message);
					return 1;
				})));
	}
}
