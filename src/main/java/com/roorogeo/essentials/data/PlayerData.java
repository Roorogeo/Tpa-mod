package com.roorogeo.essentials.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * Everything Essentials remembers about one player. Saved as
 * {@code config/essentials/userdata/<uuid>.json}.
 *
 * <p>Instances are only touched on the server thread. Call {@link #markDirty()} after changing a
 * field so the store writes it out on the next autosave, logout or stop.
 */
public final class PlayerData {
	public UUID uuid;
	/** Last known username. */
	public String name = "";
	/** Nickname with & codes, or null. Stored without the configured prefix. */
	public @Nullable String nickname;
	public long firstJoin;
	/** Time of the last logout (or last save while online). */
	public long lastSeen;
	/** Start of the current session, 0 while offline. */
	public long sessionStart;
	public String lastIp = "";

	public Map<String, Location> homes = new TreeMap<>();
	/** Location for /back. */
	public @Nullable Location lastLocation;
	public @Nullable Location logoutLocation;

	public double balance;

	public List<Mail> mail = new ArrayList<>();
	public Set<UUID> ignored = new LinkedHashSet<>();
	/** Kit name to the time it was last claimed (epoch millis). */
	public Map<String, Long> kitUses = new HashMap<>();

	/** 0 = not muted, -1 = muted forever, otherwise epoch millis when the mute ends. */
	public long mutedUntil;
	public String muteReason = "";

	public @Nullable Jail jail;

	public boolean god;
	public boolean socialSpy;
	public boolean vanished;
	public boolean frozen;

	private transient boolean dirty;

	public PlayerData() {
	}

	public PlayerData(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}

	public void markDirty() {
		this.dirty = true;
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public void clearDirty() {
		this.dirty = false;
	}

	public boolean isMuted() {
		return this.mutedUntil == -1 || this.mutedUntil > System.currentTimeMillis();
	}

	/** Milliseconds of mute left, or -1 for a permanent mute. */
	public long muteRemaining() {
		return this.mutedUntil == -1 ? -1 : Math.max(0, this.mutedUntil - System.currentTimeMillis());
	}

	public int unreadMail() {
		int count = 0;

		for (Mail entry : this.mail) {
			if (!entry.read) {
				count++;
			}
		}

		return count;
	}

	/** Repairs fields that are null in hand-edited or old files. */
	public void normalize() {
		if (this.name == null) {
			this.name = "";
		}

		if (this.lastIp == null) {
			this.lastIp = "";
		}

		if (this.muteReason == null) {
			this.muteReason = "";
		}

		this.homes = this.homes == null ? new TreeMap<>() : new TreeMap<>(this.homes);
		this.mail = this.mail == null ? new ArrayList<>() : new ArrayList<>(this.mail);
		this.ignored = this.ignored == null ? new LinkedHashSet<>() : new LinkedHashSet<>(this.ignored);
		this.kitUses = this.kitUses == null ? new HashMap<>() : new HashMap<>(this.kitUses);
	}

	/** A piece of mail. */
	public static final class Mail {
		public String sender = "";
		public @Nullable UUID senderId;
		public long time;
		public String message = "";
		public boolean read;

		public Mail() {
		}

		public Mail(String sender, @Nullable UUID senderId, long time, String message) {
			this.sender = sender;
			this.senderId = senderId;
			this.time = time;
			this.message = message;
		}
	}

	/** Jail sentence. */
	public static final class Jail {
		public String jail = "";
		/** Milliseconds left, or -1 for no end. */
		public long remaining = -1;
		public String reason = "";
		/** Where the player was when jailed, used when released. */
		public @Nullable Location previous;
		/** Whether the player has been moved into the jail yet (false when jailed while offline). */
		public boolean placed;

		public boolean isPermanent() {
			return this.remaining == -1;
		}
	}
}
