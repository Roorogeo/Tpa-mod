package com.roorogeo.essentials.data;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.text.TextFormatter;

/**
 * A position in a specific dimension, stored in JSON files as
 * {@code {"world": "minecraft:overworld", "x": 0.5, "y": 64, "z": 0.5, "yaw": 0, "pitch": 0}}.
 */
public record Location(String world, double x, double y, double z, float yaw, float pitch) {
	public static Location of(Entity entity) {
		return new Location(dimensionId(entity.level()), entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
	}

	public static Location of(ServerLevel level, Vec3 position, float yaw, float pitch) {
		return new Location(dimensionId(level), position.x, position.y, position.z, yaw, pitch);
	}

	public static String dimensionId(Level level) {
		return level.dimension().identifier().toString();
	}

	public static @Nullable ServerLevel level(MinecraftServer server, String world) {
		Identifier id = Identifier.tryParse(world);
		return id == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
	}

	public @Nullable ServerLevel level(MinecraftServer server) {
		return level(server, this.world);
	}

	public Vec3 position() {
		return new Vec3(this.x, this.y, this.z);
	}

	public Location withPosition(Vec3 position) {
		return new Location(this.world, position.x, position.y, position.z, this.yaw, this.pitch);
	}

	public int blockX() {
		return (int) Math.floor(this.x);
	}

	public int blockY() {
		return (int) Math.floor(this.y);
	}

	public int blockZ() {
		return (int) Math.floor(this.z);
	}

	/** Short dimension name, e.g. "overworld" for "minecraft:overworld". */
	public String worldName() {
		Identifier id = Identifier.tryParse(this.world);
		return id == null ? this.world : id.getPath();
	}

	/** Human readable text using the {@code teleport.location-format} message. */
	public String describe() {
		return TextFormatter.format(ConfigManager.message("teleport.location-format"), Map.of(
				"x", this.blockX(), "y", this.blockY(), "z", this.blockZ(), "world", this.worldName())).getString();
	}

	public boolean isIn(ServerPlayer player) {
		return this.world.equals(dimensionId(player.level()));
	}
}
