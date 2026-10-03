package com.roorogeo.essentials.command.world;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.WorldClock;

import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.text.Messages;

/**
 * Time helpers. Since 26.1 time belongs to world clocks: each dimension type has a default clock
 * (the overworld and end have one, the nether doesn't), and named time markers such as day or night.
 */
final class WorldClocks {
	private WorldClocks() {
	}

	static Holder<WorldClock> clock(ServerLevel level) throws CommandSyntaxException {
		return level.dimensionType().defaultClock().orElseThrow(() ->
				new SimpleCommandExceptionType(Messages.get("world-time.no-clock", "world", Location.dimensionId(level))).create());
	}

	/** Moves the world's clock forward to the next occurrence of {@code marker}. */
	static void moveTo(ServerLevel level, ResourceKey<ClockTimeMarker> marker) throws CommandSyntaxException {
		level.getServer().clockManager().moveToTimeMarker(clock(level), marker);
	}

	static long ticks(ServerLevel level) throws CommandSyntaxException {
		return level.getServer().clockManager().getTotalTicks(clock(level));
	}

	/** Time of day as HH:MM, where tick 0 is 06:00. */
	static String clockTime(long ticks) {
		long dayTicks = Math.floorMod(ticks, 24000L);
		long hours = (dayTicks / 1000 + 6) % 24;
		long minutes = dayTicks % 1000 * 60 / 1000;
		return String.format("%02d:%02d", hours, minutes);
	}
}
