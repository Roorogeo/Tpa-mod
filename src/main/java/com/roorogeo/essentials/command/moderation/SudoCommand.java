package com.roorogeo.essentials.command.moderation;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.service.ChatService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /sudo <player> <command>} runs a command as the player (with their permissions);
 * {@code /sudo <player> c:<message>} makes them chat.
 */
public final class SudoCommand extends EssentialsCommand {
	public SudoCommand() {
		super("sudo", PermissionNodes.SUDO);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.ONLINE)
				.then(argument("command", StringArgumentType.greedyString())
						.executes(this.run(ctx -> {
							CommandSourceStack source = ctx.getSource();
							ServerPlayer target = onlinePlayer(source, StringArgumentType.getString(ctx, "player"));

							if (Perms.check(target, PermissionNodes.SUDO_EXEMPT)) {
								throw error("sudo.exempt", "player", DisplayNames.realName(target));
							}

							String input = StringArgumentType.getString(ctx, "command");
							MinecraftServer server = source.getServer();

							if (input.startsWith("c:")) {
								String message = input.substring(2).trim();
								PlayerChatMessage chat = PlayerChatMessage.unsigned(target.getUUID(), message);
								ChatType.Bound bound = ChatType.bind(ChatType.CHAT, target);

								if (ChatService.onChat(chat, target, bound)) {
									server.getPlayerList().broadcastChatMessage(chat, target, bound);
								}

								send(source, "sudo.chat", "player", DisplayNames.of(target), "message", message);
								return 1;
							}

							String command = input.startsWith("/") ? input.substring(1) : input;
							server.getCommands().performPrefixedCommand(target.createCommandSourceStack(), command);
							send(source, "sudo.command", "player", DisplayNames.of(target), "command", command);
							return 1;
						}))));
	}
}
