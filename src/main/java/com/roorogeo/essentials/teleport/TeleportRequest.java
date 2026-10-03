package com.roorogeo.essentials.teleport;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.chat.Component;

import com.roorogeo.essentials.data.Location;

/**
 * A teleport to perform.
 *
 * @param command primary name of the command that caused it; its {@code commands.<name>} settings
 *                decide the warmup and cooldown
 * @param destination shown in messages, e.g. "home base"
 * @param target resolved when the warmup ends, so moving targets (players) are followed;
 *               returning null cancels the teleport
 * @param playerInitiated true when the moving player asked for it themselves; only then the
 *                        combat tag, warmup and cooldown apply
 */
public record TeleportRequest(String command, Component destination, Supplier<@Nullable Location> target, boolean playerInitiated) {
	public static TeleportRequest fixed(String command, Component destination, Location location, boolean playerInitiated) {
		return new TeleportRequest(command, destination, () -> location, playerInitiated);
	}
}
