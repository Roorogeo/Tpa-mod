package com.roorogeo.essentials.util;

import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jspecify.annotations.Nullable;

import com.roorogeo.essentials.Essentials;

/**
 * Validates home, warp, jail and kit names against the patterns in config.json.
 */
public final class Names {
	private static final Pattern FALLBACK = Pattern.compile("[a-z0-9_-]{1,32}");

	private Names() {
	}

	/** Lower-cases {@code name} and returns it if it matches {@code pattern}, otherwise null. */
	public static @Nullable String normalize(String name, String pattern) {
		String lower = name.toLowerCase(Locale.ROOT);
		return compile(pattern).matcher(lower).matches() ? lower : null;
	}

	private static Pattern compile(String pattern) {
		try {
			return Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {
			Essentials.LOGGER.warn("Invalid name pattern '{}' in config.json, using {}", pattern, FALLBACK.pattern());
			return FALLBACK;
		}
	}
}
