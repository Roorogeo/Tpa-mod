package com.roorogeo.tpamod.tpa;

import java.util.UUID;

/**
 * A pending teleport request.
 *
 * @param type {@link Type#TO}: sender teleports to target (/tpa);
 *             {@link Type#HERE}: target teleports to sender (/tpahere)
 */
public record TpaRequest(UUID sender, String senderName, UUID target, String targetName, Type type, long expiresAt) {
	public enum Type {
		TO,
		HERE
	}

	public boolean isExpired() {
		return System.currentTimeMillis() >= this.expiresAt;
	}
}
