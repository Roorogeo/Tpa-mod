package com.roorogeo.essentials.teleport;

import java.util.HashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;

/**
 * Checks that a teleport destination won't hurt the player and finds a nearby safe spot if it would.
 *
 * <p>A spot is safe when the player's hitbox fits without touching blocks, nothing at the feet,
 * head or below is dangerous (lava, fire, cactus, the blocks in {@code teleport.safety.unsafe-blocks}),
 * and there is ground to stand on (unless the player can fly). Only already-loaded chunks are read.
 */
public final class SafeLocations {
	private static Set<Block> unsafeBlocks = Set.of();
	private static int unsafeBlocksHash;

	private SafeLocations() {
	}

	/**
	 * Returns {@code location} if it is safe, a nearby safe location if one exists, or null.
	 * Players allowed to skip the check get {@code location} back unchanged.
	 */
	public static @Nullable Location find(ServerPlayer player, ServerLevel level, Location location) {
		EssentialsConfig.Safety safety = ConfigManager.config().teleport.safety;

		if (!safety.enabled || Perms.check(player, PermissionNodes.TELEPORT_SAFETY_BYPASS)
				|| safety.skipForCreativeAndSpectator && (player.isCreative() || player.isSpectator())) {
			return location;
		}

		boolean canFly = player.getAbilities().mayfly;

		if (isSafe(player, level, location.position(), canFly)) {
			return location;
		}

		BlockPos origin = BlockPos.containing(location.position());

		for (int i = 0; i <= safety.verticalRadius * 2; i++) {
			int dy = (i + 1) / 2 * (i % 2 == 0 ? -1 : 1);

			for (int radius = 0; radius <= safety.horizontalRadius; radius++) {
				for (int dx = -radius; dx <= radius; dx++) {
					for (int dz = -radius; dz <= radius; dz++) {
						if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
							continue;
						}

						BlockPos pos = origin.offset(dx, dy, dz);
						Vec3 candidate = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

						if (isSafe(player, level, candidate, canFly)) {
							return location.withPosition(candidate);
						}
					}
				}
			}
		}

		return null;
	}

	/** True if the player can stand at {@code target} without being hurt. */
	public static boolean isSafe(ServerPlayer player, ServerLevel level, Vec3 target, boolean canFly) {
		BlockPos feet = BlockPos.containing(target);
		BlockPos head = feet.above();
		BlockPos ground = BlockPos.containing(target.x, target.y - 0.2, target.z);

		if (!level.isInWorldBounds(feet) || !level.isInWorldBounds(head) || !isChunkLoaded(level, feet)) {
			return false;
		}

		AABB box = player.getBoundingBox().move(target.subtract(player.position()));

		if (!level.noCollision(player, box)) {
			return false;
		}

		BlockState feetState = level.getBlockState(feet);
		BlockState headState = level.getBlockState(head);
		BlockState groundState = level.getBlockState(ground);

		if (isHazard(feetState) || isHazard(headState) || isHazard(groundState)) {
			return false;
		}

		boolean waterUnsafe = ConfigManager.config().teleport.safety.waterIsUnsafe;
		boolean inWater = feetState.getFluidState().is(FluidTags.WATER);

		if (waterUnsafe && (inWater || headState.getFluidState().is(FluidTags.WATER))) {
			return false;
		}

		if (canFly || inWater) {
			return true;
		}

		return !level.noCollision(player, box.move(0, -0.3, 0));
	}

	private static boolean isChunkLoaded(ServerLevel level, BlockPos pos) {
		return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null;
	}

	private static boolean isHazard(BlockState state) {
		FluidState fluid = state.getFluidState();
		return fluid.is(FluidTags.LAVA) || unsafeBlocks().contains(state.getBlock());
	}

	private static Set<Block> unsafeBlocks() {
		var configured = ConfigManager.config().teleport.safety.unsafeBlocks;

		if (configured.hashCode() != unsafeBlocksHash) {
			Set<Block> blocks = new HashSet<>();

			for (String id : configured) {
				Identifier identifier = Identifier.tryParse(id);

				if (identifier != null) {
					BuiltInRegistries.BLOCK.getOptional(identifier).ifPresent(blocks::add);
				}
			}

			unsafeBlocks = blocks;
			unsafeBlocksHash = configured.hashCode();
		}

		return unsafeBlocks;
	}
}
