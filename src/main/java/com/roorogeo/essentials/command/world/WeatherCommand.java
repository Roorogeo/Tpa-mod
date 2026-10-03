package com.roorogeo.essentials.command.world;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.Messages;

/**
 * {@code /weather <clear|rain|thunder> [duration]}. Weather is shared by all dimensions since 26.1.
 * Without a duration, {@code world.weather-duration-seconds} is used (0 = vanilla's random length).
 */
public final class WeatherCommand extends EssentialsCommand {
	/** The weather types, also used by /sun and /rain. */
	enum Type {
		CLEAR, RAIN, THUNDER
	}

	public WeatherCommand() {
		super("weather", PermissionNodes.WEATHER);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(type("clear", Type.CLEAR, PermissionNodes.WEATHER_CLEAR))
				.then(type("rain", Type.RAIN, PermissionNodes.WEATHER_RAIN))
				.then(type("thunder", Type.THUNDER, PermissionNodes.WEATHER_THUNDER));
	}

	private LiteralArgumentBuilder<CommandSourceStack> type(String name, Type type, PermissionNodes.Node node) {
		return literal(name)
				.requires(requires(node))
				.executes(this.run(ctx -> set(ctx.getSource(), type, -1)))
				.then(argument("duration", TimeArgument.time(1))
						.executes(this.run(ctx -> set(ctx.getSource(), type, IntegerArgumentType.getInteger(ctx, "duration")))));
	}

	/**
	 * Sets the weather for {@code ticks} (or the configured/random default when negative).
	 */
	static int set(CommandSourceStack source, Type type, int ticks) {
		MinecraftServer server = source.getServer();
		ServerLevel level = source.getLevel();
		int configured = ConfigManager.config().world.weatherDurationSeconds * 20;
		int duration = ticks > 0 ? ticks : configured > 0 ? configured : switch (type) {
			case CLEAR -> ServerLevel.RAIN_DELAY.sample(level.getRandom());
			case RAIN -> ServerLevel.RAIN_DURATION.sample(level.getRandom());
			case THUNDER -> ServerLevel.THUNDER_DURATION.sample(level.getRandom());
		};

		switch (type) {
			case CLEAR -> server.setWeatherParameters(duration, 0, false, false);
			case RAIN -> server.setWeatherParameters(0, duration, true, false);
			case THUNDER -> server.setWeatherParameters(0, duration, true, true);
		}

		send(source, "weather.set", "world", Location.dimensionId(level), "weather", Messages.plain("weather.name." + type.name().toLowerCase(java.util.Locale.ROOT)));
		return 1;
	}
}
