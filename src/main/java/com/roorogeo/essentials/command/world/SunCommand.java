package com.roorogeo.essentials.command.world;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.TimeArgument;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /sun [duration]}: clear weather.
 */
public final class SunCommand extends EssentialsCommand {
	public SunCommand() {
		super("sun", PermissionNodes.SUN);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> WeatherCommand.set(ctx.getSource(), WeatherCommand.Type.CLEAR, -1)))
				.then(argument("duration", TimeArgument.time(1))
						.executes(this.run(ctx -> WeatherCommand.set(ctx.getSource(), WeatherCommand.Type.CLEAR, IntegerArgumentType.getInteger(ctx, "duration")))));
	}
}
