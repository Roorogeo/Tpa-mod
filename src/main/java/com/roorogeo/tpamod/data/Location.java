package com.roorogeo.tpamod.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * A saved position in a specific dimension.
 */
public record Location(String dimension, double x, double y, double z, float yaw, float pitch) {
	public static final Codec<Location> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("dimension").forGetter(Location::dimension),
			Codec.DOUBLE.fieldOf("x").forGetter(Location::x),
			Codec.DOUBLE.fieldOf("y").forGetter(Location::y),
			Codec.DOUBLE.fieldOf("z").forGetter(Location::z),
			Codec.FLOAT.fieldOf("yaw").forGetter(Location::yaw),
			Codec.FLOAT.fieldOf("pitch").forGetter(Location::pitch)
	).apply(instance, Location::new));

	public static Location of(ServerPlayer player) {
		return new Location(
				player.level().dimension().identifier().toString(),
				player.getX(), player.getY(), player.getZ(),
				player.getYRot(), player.getXRot());
	}

	public Vec3 position() {
		return new Vec3(this.x, this.y, this.z);
	}

	public @Nullable ServerLevel level(MinecraftServer server) {
		Identifier id = Identifier.tryParse(this.dimension);
		return id == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
	}

	/** Short human readable form, e.g. "120, 64, -30 (overworld)". */
	public String describe() {
		Identifier id = Identifier.tryParse(this.dimension);
		String dim = id == null ? this.dimension : id.getPath();
		return "%d, %d, %d (%s)".formatted((int) Math.floor(this.x), (int) Math.floor(this.y), (int) Math.floor(this.z), dim);
	}
}
