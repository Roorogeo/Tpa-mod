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
 * {@code /night [world]}: moves the clock forward to the next night.
 */
public final class NightCommand extends EssentialsCommand {
	public NightCommand() {
		super("night", PermissionNodes.NIGHT);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> night(ctx.getSource(), ctx.getSource().getLevel())))
				.then(argument("world", DimensionArgument.dimension())
						.executes(this.run(ctx -> night(ctx.getSource(), DimensionArgument.getDimension(ctx, "world")))));
	}

	private static int night(CommandSourceStack source, ServerLevel level) throws CommandSyntaxException {
		WorldClocks.moveTo(level, ClockTimeMarkers.NIGHT);
		send(source, "world-time.set", "world", Location.dimensionId(level), "time", WorldClocks.clockTime(WorldClocks.ticks(level)));
		return 1;
	}
}
