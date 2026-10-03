package com.roorogeo.essentials.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;

/**
 * /freeze: frozen players are pulled back to where they were frozen every tick. Looking around is
 * still allowed. Frozen state is saved, so relogging doesn't escape it.
 */
public final class FreezeService {
	private static final double MAX_DRIFT_SQ = 0.01;
	private static final Map<UUID, Vec3> ANCHORS = new HashMap<>();

	private FreezeService() {
	}

	public static boolean isFrozen(ServerPlayer player) {
		PlayerData data = PlayerDataStore.get(player.getUUID());
		return data != null && data.frozen;
	}

	public static void setFrozen(ServerPlayer player, boolean frozen) {
		PlayerData data = PlayerDataStore.get(player);
		data.frozen = frozen;
		data.markDirty();

		if (frozen) {
			ANCHORS.put(player.getUUID(), player.position());
			player.getAbilities().flying = false;
			player.onUpdateAbilities();
		} else {
			ANCHORS.remove(player.getUUID());
		}
	}

	public static void onJoin(ServerPlayer player) {
		if (isFrozen(player)) {
			ANCHORS.put(player.getUUID(), player.position());
		}
	}

	public static void forget(UUID player) {
		ANCHORS.remove(player);
	}

	public static void tick(MinecraftServer server) {
		if (ANCHORS.isEmpty()) {
			return;
		}

		for (Map.Entry<UUID, Vec3> entry : ANCHORS.entrySet()) {
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

			if (player == null || !player.isAlive()) {
				continue;
			}

			Vec3 anchor = entry.getValue();

			if (player.position().distanceToSqr(anchor) > MAX_DRIFT_SQ) {
				if (player.isPassenger()) {
					player.stopRiding();
				}

				player.connection.teleport(anchor.x, anchor.y, anchor.z, player.getYRot(), player.getXRot());
				player.setDeltaMovement(Vec3.ZERO);
			}
		}
	}
}
