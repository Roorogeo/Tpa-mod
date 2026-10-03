package com.roorogeo.essentials.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.level.ServerPlayer;

/**
 * Calls {@code ChunkMap.TrackedEntity#updatePlayer}, vanilla's own "should this viewer see this
 * entity" refresh (it sends the spawn or remove packets as needed). The class is private, so it can
 * only be reached through a mixin. Used together with {@link ChunkMapAccessor} by /vanish.
 */
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public interface TrackedEntityInvoker {
	@Invoker("updatePlayer")
	void essentials$updatePlayer(ServerPlayer player);
}
