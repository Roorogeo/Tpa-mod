package com.roorogeo.tpamod.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.tpamod.data.Location;
import com.roorogeo.tpamod.data.WarpStore;
import com.roorogeo.tpamod.perm.Perms;
import com.roorogeo.tpamod.teleport.TeleportManager;
import com.roorogeo.tpamod.util.Msg;

public final class WarpCommands {
	private WarpCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("warp")
				.requires(Perms.everyone(Perms.WARP))
				.executes(WarpCommands::list)
				.then(argument("name", StringArgumentType.word())
						.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(usableWarps(ctx.getSource()), builder))
						.executes(WarpCommands::warp)));

		dispatcher.register(literal("warps")
				.requires(Perms.everyone(Perms.WARP))
				.executes(WarpCommands::list));

		dispatcher.register(literal("setwarp")
				.requires(Perms.op(Perms.SETWARP))
				.then(argument("name", StringArgumentType.word())
						.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(WarpStore.all().keySet(), builder))
						.executes(WarpCommands::setWarp)));

		dispatcher.register(literal("delwarp")
				.requires(Perms.op(Perms.DELWARP))
				.then(argument("name", StringArgumentType.word())
						.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(WarpStore.all().keySet(), builder))
						.executes(WarpCommands::delWarp)));
	}

	private static List<String> usableWarps(CommandSourceStack source) {
		return WarpStore.all().keySet().stream().filter(name -> Perms.canUseWarp(source, name)).toList();
	}

	private static int warp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		String name = StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT);

		if (WarpStore.get(name) == null) {
			ctx.getSource().sendFailure(Msg.error("There is no warp called '" + name + "'."));
			return 0;
		}

		if (!Perms.canUseWarp(ctx.getSource(), name)) {
			ctx.getSource().sendFailure(Msg.error("You don't have permission to use warp '" + name + "'."));
			return 0;
		}

		TeleportManager.start(player, "warp '" + name + "'", () -> WarpStore.get(name));
		return Command.SINGLE_SUCCESS;
	}

	private static int list(CommandContext<CommandSourceStack> ctx) {
		List<String> warps = usableWarps(ctx.getSource());

		if (warps.isEmpty()) {
			ctx.getSource().sendSuccess(() -> Msg.info("There are no warps."), false);
			return 0;
		}

		Map<String, Location> all = WarpStore.all();
		MutableComponent message = Msg.info("Warps (" + warps.size() + "): ");

		for (int i = 0; i < warps.size(); i++) {
			String name = warps.get(i);

			if (i > 0) {
				message.append(Msg.info(", "));
			}

			message.append(Msg.link(name, "/warp " + name, all.get(name).describe() + "\nClick to teleport"));
		}

		ctx.getSource().sendSuccess(() -> message, false);
		return warps.size();
	}

	private static int setWarp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		String name = Names.normalize(StringArgumentType.getString(ctx, "name"));

		if (name == null) {
			ctx.getSource().sendFailure(Msg.error("Warp names may only use letters, numbers, '_' and '-' (max 32)."));
			return 0;
		}

		boolean exists = WarpStore.get(name) != null;
		WarpStore.set(name, Location.of(player));
		ctx.getSource().sendSuccess(() -> Msg.success(exists ? "Updated warp " : "Created warp ").append(Msg.highlight(name)).append(Msg.success(".")), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int delWarp(CommandContext<CommandSourceStack> ctx) {
		String name = StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT);

		if (!WarpStore.remove(name)) {
			ctx.getSource().sendFailure(Msg.error("There is no warp called '" + name + "'."));
			return 0;
		}

		ctx.getSource().sendSuccess(() -> Msg.success("Deleted warp ").append(Msg.highlight(name)).append(Msg.success(".")), true);
		return Command.SINGLE_SUCCESS;
	}
}
