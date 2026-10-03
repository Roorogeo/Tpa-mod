package com.roorogeo.essentials.command.world;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.Locale;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ClockTimeMarkers;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /time [world]}, {@code /time set <day|noon|night|midnight|ticks> [world]},
 * {@code /time add <time> [world]}. Replaces vanilla /time (use vanilla /time through a disabled
 * Essentials /time if you need clock rates or timelines).
 */
public final class TimeCommand extends EssentialsCommand {
	public TimeCommand() {
		super("time", PermissionNodes.TIME);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> query(ctx.getSource(), ctx.getSource().getLevel())))
				.then(argument("world", DimensionArgument.dimension())
						.executes(this.run(ctx -> query(ctx.getSource(), DimensionArgument.getDimension(ctx, "world")))))
				.then(literal("set")
						.requires(requires(PermissionNodes.TIME_SET))
						.then(argument("time", StringArgumentType.word())
								.suggests((ctx, builder) -> {
									for (String name : new String[] {"day", "noon", "night", "midnight"}) {
										builder.suggest(name);
									}

									return builder.buildFuture();
								})
								.executes(this.run(ctx -> set(ctx.getSource(), ctx.getSource().getLevel(), StringArgumentType.getString(ctx, "time"))))
								.then(argument("world", DimensionArgument.dimension())
										.executes(this.run(ctx -> set(ctx.getSource(), DimensionArgument.getDimension(ctx, "world"), StringArgumentType.getString(ctx, "time")))))))
				.then(literal("add")
						.requires(requires(PermissionNodes.TIME_ADD))
						.then(argument("time", TimeArgument.time())
								.executes(this.run(ctx -> add(ctx.getSource(), ctx.getSource().getLevel(), IntegerArgumentType.getInteger(ctx, "time"))))
								.then(argument("world", DimensionArgument.dimension())
										.executes(this.run(ctx -> add(ctx.getSource(), DimensionArgument.getDimension(ctx, "world"), IntegerArgumentType.getInteger(ctx, "time")))))));
	}

	private static int query(CommandSourceStack source, ServerLevel level) throws CommandSyntaxException {
		long ticks = WorldClocks.ticks(level);
		send(source, "world-time.query", "world", Location.dimensionId(level), "time", WorldClocks.clockTime(ticks),
				"ticks", Math.floorMod(ticks, 24000L), "day", ticks / 24000L);
		return 1;
	}

	private static int set(CommandSourceStack source, ServerLevel level, String value) throws CommandSyntaxException {
		switch (value.toLowerCase(Locale.ROOT)) {
			case "day" -> WorldClocks.moveTo(level, ClockTimeMarkers.DAY);
			case "noon" -> WorldClocks.moveTo(level, ClockTimeMarkers.NOON);
			case "night" -> WorldClocks.moveTo(level, ClockTimeMarkers.NIGHT);
			case "midnight" -> WorldClocks.moveTo(level, ClockTimeMarkers.MIDNIGHT);
			default -> {
				long ticks;

				try {
					ticks = Long.parseLong(value);
				} catch (NumberFormatException e) {
					throw error("general.invalid-duration", "input", value);
				}

				level.getServer().clockManager().setTotalTicks(WorldClocks.clock(level), ticks);
			}
		}

		send(source, "world-time.set", "world", Location.dimensionId(level), "time", WorldClocks.clockTime(WorldClocks.ticks(level)));
		return 1;
	}

	private static int add(CommandSourceStack source, ServerLevel level, int ticks) throws CommandSyntaxException {
		level.getServer().clockManager().addTicks(WorldClocks.clock(level), ticks);
		send(source, "world-time.added", "world", Location.dimensionId(level), "ticks", ticks);
		return 1;
	}
}
