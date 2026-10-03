package com.roorogeo.essentials.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.roorogeo.essentials.config.ConfigManager;

/**
 * Parses and prints durations such as {@code 1d2h30m}.
 */
public final class Durations {
	/** Returned by {@link #parse(String)} for "permanent" / "perm" / "forever". */
	public static final long PERMANENT = -1L;

	private static final Pattern PART = Pattern.compile("(\\d+)(w|d|h|m|s)");

	private Durations() {
	}

	/**
	 * Parses a duration into milliseconds. Units: {@code w d h m s}; parts can be combined
	 * ({@code 1h30m}). A bare number is read as seconds.
	 *
	 * @return milliseconds, {@link #PERMANENT}, or {@code null} if the text isn't a duration
	 */
	public static Long parse(String text) {
		String input = text.trim().toLowerCase(Locale.ROOT);

		if (input.equals("permanent") || input.equals("perm") || input.equals("forever")) {
			return PERMANENT;
		}

		if (input.matches("\\d+")) {
			return Long.parseLong(input) * 1000L;
		}

		Matcher matcher = PART.matcher(input);
		long total = 0;
		int consumed = 0;

		while (matcher.find()) {
			if (matcher.start() != consumed) {
				return null;
			}

			long amount = Long.parseLong(matcher.group(1));
			total += switch (matcher.group(2)) {
				case "w" -> amount * 7L * 24 * 3600 * 1000;
				case "d" -> amount * 24L * 3600 * 1000;
				case "h" -> amount * 3600L * 1000;
				case "m" -> amount * 60L * 1000;
				default -> amount * 1000L;
			};
			consumed = matcher.end();
		}

		return consumed == input.length() && consumed > 0 ? total : null;
	}

	/** Formats milliseconds with the {@code time.*} messages, e.g. "1d 2h 5m". */
	public static String format(long millis) {
		if (millis == PERMANENT) {
			return ConfigManager.message("time.permanent");
		}

		long seconds = Math.max(0, (millis + 999) / 1000);

		if (seconds == 0) {
			return ConfigManager.message("time.now");
		}

		long days = seconds / 86400;
		long hours = seconds % 86400 / 3600;
		long minutes = seconds % 3600 / 60;
		long secs = seconds % 60;
		String separator = ConfigManager.message("time.separator");
		StringBuilder out = new StringBuilder();
		append(out, separator, "time.days", days);
		append(out, separator, "time.hours", hours);
		append(out, separator, "time.minutes", minutes);
		append(out, separator, "time.seconds", secs);
		return out.toString();
	}

	private static void append(StringBuilder out, String separator, String key, long value) {
		if (value <= 0) {
			return;
		}

		if (!out.isEmpty()) {
			out.append(separator);
		}

		out.append(ConfigManager.message(key).replace("{n}", Long.toString(value)));
	}

	/** Seconds formatted like {@link #format(long)}. */
	public static String formatSeconds(long seconds) {
		return format(seconds * 1000L);
	}
}
