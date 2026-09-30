package com.roorogeo.tpamod.command;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;

import com.roorogeo.tpamod.config.ModConfig;
import com.roorogeo.tpamod.data.WarpStore;
import com.roorogeo.tpamod.perm.Perms;
import com.roorogeo.tpamod.util.Msg;

public final class AdminCommands {
	private AdminCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("tpamod")
				.requires(Perms.op(Perms.RELOAD))
				.then(literal("reload")
						.executes(ctx -> {
							ModConfig.load();
							WarpStore.load(ctx.getSource().getServer());
							ctx.getSource().sendSuccess(() -> Msg.success("Reloaded TPA Mod config and warps."), true);
							return Command.SINGLE_SUCCESS;
						})));
	}
}
