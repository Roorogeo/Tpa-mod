package com.roorogeo.essentials.command.world;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ClockTimeMarkers;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /day [world]}: moves the clock forward to the next morning.
 */
public final class DayCommand extends EssentialsCommand {
	public DayCommand() {
		super("day", PermissionNodes.DAY);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> day(ctx.getSource(), ctx.getSource().getLevel())))
				.then(argument("world", DimensionArgument.dimension())
						.executes(this.run(ctx -> day(ctx.getSource(), DimensionArgument.getDimension(ctx, "world")))));
	}

	private static int day(CommandSourceStack source, ServerLevel level) throws CommandSyntaxException {
		WorldClocks.moveTo(level, ClockTimeMarkers.DAY);
		send(source, "world-time.set", "world", Location.dimensionId(level), "time", WorldClocks.clockTime(WorldClocks.ticks(level)));
		return 1;
	}
}
