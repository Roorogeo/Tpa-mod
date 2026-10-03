package com.roorogeo.essentials.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.roorogeo.essentials.Essentials;

/**
 * Writes files on a single background thread so the server thread never waits for disk I/O.
 *
 * <p>Callers serialize their data on the server thread (cheap, and it gives a consistent snapshot)
 * and hand the resulting text to {@link #write(Path, String)}. Writes to the same path are coalesced:
 * if a file is queued twice before the worker gets to it, only the newest content is written.
 * Files are written to a temporary file first and then moved into place, so a crash mid-write
 * never leaves a truncated file behind.
 */
public final class AsyncFileWriter {
	private static final Map<Path, String> PENDING = new ConcurrentHashMap<>();
	private static ExecutorService executor = newExecutor();

	private AsyncFileWriter() {
	}

	private static ExecutorService newExecutor() {
		return Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "Essentials-IO");
			thread.setDaemon(true);
			return thread;
		});
	}

	/** The executor used for background reads too, so reads and writes of a file never race. */
	public static synchronized ExecutorService executor() {
		if (executor.isShutdown()) {
			executor = newExecutor();
		}

		return executor;
	}

	/** Queues {@code content} to be written to {@code path}. Returns immediately. */
	public static void write(Path path, String content) {
		if (PENDING.put(path, content) == null) {
			executor().execute(() -> flushOne(path));
		}
	}

	/** Queues {@code path} for deletion, cancelling a pending write to it. */
	public static void delete(Path path) {
		PENDING.remove(path);
		executor().execute(() -> {
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				Essentials.LOGGER.error("Failed to delete {}", path, e);
			}
		});
	}

	private static void flushOne(Path path) {
		String content = PENDING.remove(path);

		if (content == null) {
			return;
		}

		try {
			Files.createDirectories(path.getParent());
			Path temp = path.resolveSibling(path.getFileName() + ".tmp");
			Files.writeString(temp, content, StandardCharsets.UTF_8);

			try {
				Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			Essentials.LOGGER.error("Failed to write {}", path, e);
		}
	}

	/**
	 * Writes everything still queued and stops the worker. Called when the server stops; this is
	 * the only place where the server thread waits for the disk, which is what "flush on stop" means.
	 */
	public static synchronized void shutdown() {
		ExecutorService current = executor;
		current.shutdown();

		try {
			if (!current.awaitTermination(30, TimeUnit.SECONDS)) {
				Essentials.LOGGER.warn("Background writer did not finish in 30 seconds, writing the rest directly");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Anything that was queued but not picked up (timeout or interruption) is written here.
		for (Path path : PENDING.keySet()) {
			flushOne(path);
		}
	}
}
