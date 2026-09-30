package com.roorogeo.tpamod.data;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.mojang.serialization.Codec;
import org.jspecify.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import com.roorogeo.tpamod.TpaMod;

/**
 * Player homes, stored as a persistent Fabric data attachment on the player so they are
 * saved with the player data and survive death.
 */
public final class Homes {
	private static final AttachmentType<Map<String, Location>> HOMES = AttachmentRegistry.create(
			TpaMod.id("homes"),
			builder -> builder
					.persistent(Codec.unboundedMap(Codec.STRING, Location.CODEC))
					.copyOnDeath()
	);

	private Homes() {
	}

	/** Forces class loading so the attachment is registered during mod init. */
	public static void init() {
	}

	/** All homes of a player, sorted by name. The returned map is read-only. */
	public static Map<String, Location> get(ServerPlayer player) {
		Map<String, Location> homes = player.getAttached(HOMES);
		return homes == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(homes));
	}

	public static @Nullable Location get(ServerPlayer player, String name) {
		return get(player).get(name);
	}

	public static void set(ServerPlayer player, String name, Location location) {
		Map<String, Location> homes = new TreeMap<>(get(player));
		homes.put(name, location);
		player.setAttached(HOMES, homes);
	}

	public static boolean remove(ServerPlayer player, String name) {
		Map<String, Location> homes = new TreeMap<>(get(player));

		if (homes.remove(name) == null) {
			return false;
		}

		player.setAttached(HOMES, homes.isEmpty() ? null : homes);
		return true;
	}
}
