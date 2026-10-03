package com.roorogeo.essentials.command.moderation;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.text.TextFormatter;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /kick <player> [reason]}. Replaces vanilla /kick. Kicks never count as combat logging.
 */
public final class KickCommand extends EssentialsCommand {
	public KickCommand() {
		super("kick", PermissionNodes.KICK);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.ONLINE)
				.executes(this.run(ctx -> kick(ctx.getSource(), StringArgumentType.getString(ctx, "player"), null)))
				.then(argument("reason", StringArgumentType.greedyString())
						.executes(this.run(ctx -> kick(ctx.getSource(), StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "reason"))))));
	}

	private static int kick(CommandSourceStack source, String name, String reason) throws CommandSyntaxException {
		ServerPlayer target = onlinePlayer(source, name);

		if (Perms.check(target, PermissionNodes.KICK_EXEMPT)) {
			throw error("kick.exempt", "player", name);
		}

		Component message = reason == null ? Messages.get("kick.default-reason") : TextFormatter.parseUser(reason, TextFormatter.Allowed.ALL);
		send(source, "kick.kicked", "player", DisplayNames.of(target), "reason", message);
		target.connection.disconnect(message);
		return 1;
	}
}
