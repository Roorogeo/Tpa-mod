package com.roorogeo.essentials.teleport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import com.roorogeo.essentials.config.ConfigManager;

/**
 * Loads destination chunks without blocking the server thread.
 *
 * <p>A temporary portal ticket makes the chunk system load (or generate) the chunks around the
 * destination on its worker threads; the callback runs on the server thread once they are ready.
 * If loading takes longer than {@code teleport.chunk-load-timeout-seconds}, the timeout callback runs instead.
 */
public final class ChunkLoading {
	private static final List<Pending> PENDING = new ArrayList<>();

	private ChunkLoading() {
	}

	private static final class Pending {
		private final ServerLevel level;
		private final ChunkPos chunk;
		private final Runnable onLoaded;
		private final Runnable onTimeout;
		private final long deadline;
		private boolean done;

		private Pending(ServerLevel level, ChunkPos chunk, Runnable onLoaded, Runnable onTimeout, long deadline) {
			this.level = level;
			this.chunk = chunk;
			this.onLoaded = onLoaded;
			this.onTimeout = onTimeout;
			this.deadline = deadline;
		}
	}

	/** Runs {@code onLoaded} once the chunks around {@code pos} (radius 1) are loaded. */
	public static void whenLoaded(ServerLevel level, BlockPos pos, Runnable onLoaded, Runnable onTimeout) {
		ChunkPos chunk = ChunkPos.containing(pos);

		if (isLoaded(level, chunk)) {
			onLoaded.run();
			return;
		}

		MinecraftServer server = level.getServer();
		long deadline = System.currentTimeMillis() + ConfigManager.config().teleport.chunkLoadTimeoutSeconds * 1000L;
		Pending pending = new Pending(level, chunk, onLoaded, onTimeout, deadline);
		PENDING.add(pending);
		level.getChunkSource().addTicketAndLoadWithRadius(TicketType.PORTAL, chunk, 1)
				.whenComplete((result, error) -> server.execute(() -> finish(pending, error == null)));
	}

	private static boolean isLoaded(ServerLevel level, ChunkPos chunk) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (level.getChunkSource().getChunkNow(chunk.x() + dx, chunk.z() + dz) == null) {
					return false;
				}
			}
		}

		return true;
	}

	private static void finish(Pending pending, boolean success) {
		if (pending.done) {
			return;
		}

		pending.done = true;
		PENDING.remove(pending);

		if (success && isLoaded(pending.level, pending.chunk)) {
			pending.onLoaded.run();
		} else {
			pending.onTimeout.run();
		}
	}

	/** Expires requests that took too long. */
	public static void tick() {
		if (PENDING.isEmpty()) {
			return;
		}

		long now = System.currentTimeMillis();
		Iterator<Pending> it = PENDING.iterator();
		List<Pending> expired = new ArrayList<>();

		while (it.hasNext()) {
			Pending pending = it.next();

			if (now >= pending.deadline) {
				it.remove();
				expired.add(pending);
			}
		}

		for (Pending pending : expired) {
			if (!pending.done) {
				pending.done = true;
				pending.onTimeout.run();
			}
		}
	}

	public static void clear() {
		PENDING.clear();
	}
}
