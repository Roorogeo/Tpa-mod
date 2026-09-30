package com.roorogeo.tpamod.command;

import java.util.Locale;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/**
 * Home and warp names: lowercase letters, digits, '_' and '-', up to 32 characters.
 */
final class Names {
	private static final Pattern VALID = Pattern.compile("[a-z0-9_-]{1,32}");

	private Names() {
	}

	/** Returns the normalized name, or null if it's not allowed. */
	static @Nullable String normalize(String name) {
		String lower = name.toLowerCase(Locale.ROOT);
		return VALID.matcher(lower).matches() ? lower : null;
	}
}
