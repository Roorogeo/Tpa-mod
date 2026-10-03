package com.roorogeo.essentials.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.level.ChunkMap;

/**
 * Read access to the entity tracker map, so /vanish can re-evaluate who sees a player immediately
 * instead of waiting until the player crosses a chunk section. Vanilla keeps this map private and
 * Fabric API has no tracking events.
 */
@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {
	@Accessor("entityMap")
	Int2ObjectMap<?> essentials$getEntityMap();
}
