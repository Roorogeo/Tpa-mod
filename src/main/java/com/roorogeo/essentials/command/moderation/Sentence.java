package com.roorogeo.essentials.command.moderation;

import org.jspecify.annotations.Nullable;

import com.roorogeo.essentials.util.Durations;

/**
 * "[duration] [reason]" arguments of /mute and /jail: the first word is a duration if it parses as one
 * ({@code 30m}, {@code 2d}, {@code permanent}), everything else is the reason.
 *
 * @param millis duration in milliseconds, {@link Durations#PERMANENT}, or the default when none was given
 */
record Sentence(long millis, String reason) {
	static Sentence parse(@Nullable String text, long defaultMillis) {
		if (text == null || text.isBlank()) {
			return new Sentence(defaultMillis, "");
		}

		String trimmed = text.trim();
		int space = trimmed.indexOf(' ');
		String first = space < 0 ? trimmed : trimmed.substring(0, space);
		Long duration = Durations.parse(first);

		if (duration == null) {
			return new Sentence(defaultMillis, trimmed);
		}

		return new Sentence(duration, space < 0 ? "" : trimmed.substring(space + 1).trim());
	}
}
