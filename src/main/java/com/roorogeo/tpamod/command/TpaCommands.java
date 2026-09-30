package com.roorogeo.tpamod.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.List;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.tpamod.perm.Perms;
import com.roorogeo.tpamod.tpa.TpaManager;
import com.roorogeo.tpamod.tpa.TpaRequest;
import com.roorogeo.tpamod.util.Msg;

public final class TpaCommands {
	private TpaCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("tpa")
				.requires(Perms.everyone(Perms.TPA))
				.then(argument("player", EntityArgument.player())
						.executes(ctx -> request(ctx, TpaRequest.Type.TO))));

		dispatcher.register(literal("tpahere")
				.requires(Perms.everyone(Perms.TPAHERE))
				.then(argument("player", EntityArgument.player())
						.executes(ctx -> request(ctx, TpaRequest.Type.HERE))));

		dispatcher.register(literal("tpaccept")
				.requires(Perms.everyone(Perms.TPACCEPT))
				.executes(ctx -> respond(ctx, false, true))
				.then(argument("player", EntityArgument.player())
						.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(incomingNames(ctx.getSource()), builder))
						.executes(ctx -> respond(ctx, true, true))));

		dispatcher.register(literal("tpdeny")
				.requires(Perms.everyone(Perms.TPACCEPT))
				.executes(ctx -> respond(ctx, false, false))
				.then(argument("player", EntityArgument.player())
						.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(incomingNames(ctx.getSource()), builder))
						.executes(ctx -> respond(ctx, true, false))));

		dispatcher.register(literal("tpcancel")
				.requires(Perms.everyone(Perms.TPA))
				.executes(TpaCommands::cancel));
	}

	private static int request(CommandContext<CommandSourceStack> ctx, TpaRequest.Type type) throws CommandSyntaxException {
		ServerPlayer sender = ctx.getSource().getPlayerOrException();
		ServerPlayer target = EntityArgument.getPlayer(ctx, "player");

		if (sender == target) {
			ctx.getSource().sendFailure(Msg.error("You can't send a teleport request to yourself."));
			return 0;
		}

		TpaManager.send(sender, target, type);
		return Command.SINGLE_SUCCESS;
	}

	private static int respond(CommandContext<CommandSourceStack> ctx, boolean named, boolean accept) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		ServerPlayer from = named ? EntityArgument.getPlayer(ctx, "player") : null;
		TpaRequest request = TpaManager.find(player.getUUID(), from == null ? null : from.getUUID());

		if (request == null) {
			ctx.getSource().sendFailure(Msg.error(from == null
					? "You have no pending teleport requests."
					: "You have no pending teleport request from " + from.getGameProfile().name() + "."));
			return 0;
		}

		if (accept) {
			TpaManager.accept(player, request);
		} else {
			TpaManager.deny(player, request);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int cancel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		TpaRequest request = TpaManager.outgoing(player.getUUID());

		if (request == null) {
			ctx.getSource().sendFailure(Msg.error("You have no outgoing teleport request."));
			return 0;
		}

		TpaManager.cancel(player, request);
		return Command.SINGLE_SUCCESS;
	}

	private static List<String> incomingNames(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		return player == null ? List.of() : TpaManager.incoming(player.getUUID()).stream().map(TpaRequest::senderName).toList();
	}
}
