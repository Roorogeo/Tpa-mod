package com.roorogeo.tpamod.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Map;
import java.util.Set;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.tpamod.data.Homes;
import com.roorogeo.tpamod.data.Location;
import com.roorogeo.tpamod.perm.Perms;
import com.roorogeo.tpamod.teleport.TeleportManager;
import com.roorogeo.tpamod.util.Msg;

public final class HomeCommands {
	private static final String DEFAULT_HOME = "home";

	private static final SuggestionProvider<CommandSourceStack> OWN_HOMES = (ctx, builder) -> {
		ServerPlayer player = ctx.getSource().getPlayer();
		return SharedSuggestionProvider.suggest(player == null ? Set.of() : Homes.get(player).keySet(), builder);
	};

	private HomeCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("home")
				.requires(Perms.everyone(Perms.HOME))
				.executes(ctx -> home(ctx, null))
				.then(argument("name", StringArgumentType.word())
						.suggests(OWN_HOMES)
						.executes(ctx -> home(ctx, StringArgumentType.getString(ctx, "name")))));

		dispatcher.register(literal("homes")
				.requires(Perms.everyone(Perms.HOME))
				.executes(HomeCommands::list));

		dispatcher.register(literal("sethome")
				.requires(Perms.everyone(Perms.SETHOME))
				.executes(ctx -> setHome(ctx, DEFAULT_HOME))
				.then(argument("name", StringArgumentType.word())
						.suggests(OWN_HOMES)
						.executes(ctx -> setHome(ctx, StringArgumentType.getString(ctx, "name")))));

		dispatcher.register(literal("delhome")
				.requires(Perms.everyone(Perms.DELHOME))
				.then(argument("name", StringArgumentType.word())
						.suggests(OWN_HOMES)
						.executes(ctx -> delHome(ctx, StringArgumentType.getString(ctx, "name")))));
	}

	private static int home(CommandContext<CommandSourceStack> ctx, String rawName) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		Map<String, Location> homes = Homes.get(player);
		String name;

		if (rawName != null) {
			name = rawName.toLowerCase(java.util.Locale.ROOT);
		} else if (homes.containsKey(DEFAULT_HOME)) {
			name = DEFAULT_HOME;
		} else if (homes.size() == 1) {
			name = homes.keySet().iterator().next();
		} else if (homes.isEmpty()) {
			ctx.getSource().sendFailure(Msg.error("You don't have any homes. Use /sethome [name] to set one."));
			return 0;
		} else {
			return list(ctx);
		}

		if (!homes.containsKey(name)) {
			ctx.getSource().sendFailure(Msg.error("You don't have a home called '" + name + "'."));
			return 0;
		}

		TeleportManager.start(player, "home '" + name + "'", () -> Homes.get(player, name));
		return Command.SINGLE_SUCCESS;
	}

	private static int list(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		Map<String, Location> homes = Homes.get(player);
		int max = Perms.maxHomes(player);
		String limit = max == Integer.MAX_VALUE ? "unlimited" : String.valueOf(max);

		if (homes.isEmpty()) {
			ctx.getSource().sendSuccess(() -> Msg.info("You don't have any homes (limit " + limit + "). Use /sethome [name] to set one."), false);
			return Command.SINGLE_SUCCESS;
		}

		MutableComponent message = Msg.info("Homes (" + homes.size() + "/" + limit + "): ");
		boolean first = true;

		for (Map.Entry<String, Location> entry : homes.entrySet()) {
			if (!first) {
				message.append(Msg.info(", "));
			}

			first = false;
			message.append(Msg.link(entry.getKey(), "/home " + entry.getKey(), entry.getValue().describe() + "\nClick to teleport"));
		}

		ctx.getSource().sendSuccess(() -> message, false);
		return homes.size();
	}

	private static int setHome(CommandContext<CommandSourceStack> ctx, String rawName) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		String name = Names.normalize(rawName);

		if (name == null) {
			ctx.getSource().sendFailure(Msg.error("Home names may only use letters, numbers, '_' and '-' (max 32)."));
			return 0;
		}

		Map<String, Location> homes = Homes.get(player);
		boolean exists = homes.containsKey(name);
		int max = Perms.maxHomes(player);

		if (!exists && homes.size() >= max) {
			ctx.getSource().sendFailure(Msg.error("You have reached your home limit (" + max + "). Delete one with /delhome <name> first."));
			return 0;
		}

		Homes.set(player, name, Location.of(player));
		ctx.getSource().sendSuccess(() -> Msg.success(exists ? "Updated home " : "Set home ").append(Msg.highlight(name)).append(Msg.success(".")), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int delHome(CommandContext<CommandSourceStack> ctx, String rawName) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		String name = rawName.toLowerCase(java.util.Locale.ROOT);

		if (!Homes.remove(player, name)) {
			ctx.getSource().sendFailure(Msg.error("You don't have a home called '" + name + "'."));
			return 0;
		}

		ctx.getSource().sendSuccess(() -> Msg.success("Deleted home ").append(Msg.highlight(name)).append(Msg.success(".")), false);
		return Command.SINGLE_SUCCESS;
	}
}
