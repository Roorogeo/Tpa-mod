package com.roorogeo.essentials.text;

import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Turns {@code &}-coded text into chat components.
 *
 * <p>Supported codes: {@code &0-&9 &a-&f} colors, {@code &#RRGGBB} hex colors, {@code &l} bold,
 * {@code &m} strikethrough, {@code &n} underline, {@code &o} italic, {@code &k} obfuscated and
 * {@code &r} reset. A color code resets the styles before it, like vanilla section codes.
 * {@code &&} writes a literal ampersand.
 *
 * <p>Placeholders ({@code {name}}) are replaced with values that are never parsed for codes, so
 * player-controlled text such as names and chat can't inject formatting. A placeholder value can
 * also be a {@link Component}, which is inserted as-is and inherits the surrounding style.
 */
public final class TextFormatter {
	/** Which kinds of codes are allowed when parsing player-written text. */
	public record Allowed(boolean colors, boolean formats, boolean magic) {
		public static final Allowed ALL = new Allowed(true, true, true);
		public static final Allowed NONE = new Allowed(false, false, false);

		public boolean any() {
			return this.colors || this.formats || this.magic;
		}
	}

	private static final String COLOR_CODES = "0123456789abcdef";

	private TextFormatter() {
	}

	/** Parses a template with every code allowed and no placeholders. */
	public static MutableComponent parse(String text) {
		return format(text, Map.of());
	}

	/**
	 * Parses a template (trusted text from messages.json) and fills its placeholders.
	 * Unknown placeholders are left as written.
	 */
	public static MutableComponent format(String template, Map<String, ?> placeholders) {
		MutableComponent root = Component.empty();
		Style style = Style.EMPTY;
		StringBuilder buffer = new StringBuilder();
		int length = template.length();
		int i = 0;

		while (i < length) {
			char c = template.charAt(i);

			if (c == '\\' && i + 1 < length && template.charAt(i + 1) == 'n') {
				buffer.append('\n');
				i += 2;
				continue;
			}

			if (c == '{') {
				int end = template.indexOf('}', i + 1);

				if (end > i + 1) {
					String key = template.substring(i + 1, end);

					if (placeholders.containsKey(key)) {
						flush(root, buffer, style);
						Object value = placeholders.get(key);

						if (value instanceof Component component) {
							root.append(Component.empty().withStyle(style).append(component));
						} else {
							root.append(Component.literal(String.valueOf(value)).withStyle(style));
						}

						i = end + 1;
						continue;
					}
				}
			}

			if (c == '&' && i + 1 < length) {
				Style next = applyCode(template, i, style, Allowed.ALL);

				if (next != null) {
					flush(root, buffer, style);
					style = next;
					i += codeLength(template, i);
					continue;
				}

				if (template.charAt(i + 1) == '&') {
					buffer.append('&');
					i += 2;
					continue;
				}
			}

			buffer.append(c);
			i++;
		}

		flush(root, buffer, style);
		return root;
	}

	/**
	 * Parses player-written text, honoring only the code types in {@code allowed}. Disallowed codes
	 * stay in the text as typed.
	 */
	public static MutableComponent parseUser(String text, Allowed allowed) {
		if (!allowed.any()) {
			return Component.literal(text);
		}

		MutableComponent root = Component.empty();
		Style style = Style.EMPTY;
		StringBuilder buffer = new StringBuilder();
		int i = 0;

		while (i < text.length()) {
			char c = text.charAt(i);

			if (c == '&' && i + 1 < text.length()) {
				Style next = applyCode(text, i, style, allowed);

				if (next != null) {
					flush(root, buffer, style);
					style = next;
					i += codeLength(text, i);
					continue;
				}
			}

			buffer.append(c);
			i++;
		}

		flush(root, buffer, style);
		return root;
	}

	/** Removes every {@code &} code, e.g. to measure a nickname's visible length. */
	public static String strip(String text) {
		StringBuilder out = new StringBuilder();
		int i = 0;

		while (i < text.length()) {
			char c = text.charAt(i);

			if (c == '&' && i + 1 < text.length() && applyCode(text, i, Style.EMPTY, Allowed.ALL) != null) {
				i += codeLength(text, i);
				continue;
			}

			out.append(c);
			i++;
		}

		return out.toString();
	}

	/** True if {@code text} contains a code of a kind not in {@code allowed}. */
	public static boolean containsDisallowed(String text, Allowed allowed) {
		for (int i = 0; i < text.length() - 1; i++) {
			if (text.charAt(i) == '&' && applyCode(text, i, Style.EMPTY, Allowed.ALL) != null
					&& applyCode(text, i, Style.EMPTY, allowed) == null) {
				return true;
			}
		}

		return false;
	}

	private static void flush(MutableComponent root, StringBuilder buffer, Style style) {
		if (!buffer.isEmpty()) {
			root.append(Component.literal(buffer.toString()).withStyle(style));
			buffer.setLength(0);
		}
	}

	private static int codeLength(String text, int index) {
		return text.charAt(index + 1) == '#' ? 8 : 2;
	}

	/** Returns the style after the code at {@code index}, or null if there is no (allowed) code there. */
	private static Style applyCode(String text, int index, Style style, Allowed allowed) {
		char code = Character.toLowerCase(text.charAt(index + 1));

		if (code == '#') {
			if (!allowed.colors() || index + 8 > text.length()) {
				return null;
			}

			String hex = text.substring(index + 2, index + 8);

			try {
				return Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt(hex, 16)));
			} catch (NumberFormatException e) {
				return null;
			}
		}

		ChatFormatting formatting = ChatFormatting.getByCode(code);

		if (formatting == null) {
			return null;
		}

		if (formatting == ChatFormatting.RESET) {
			return allowed.colors() || allowed.formats() ? Style.EMPTY : null;
		}

		if (COLOR_CODES.indexOf(code) >= 0) {
			return allowed.colors() ? Style.EMPTY.withColor(formatting) : null;
		}

		if (formatting == ChatFormatting.OBFUSCATED) {
			return allowed.magic() ? style.applyFormat(formatting) : null;
		}

		return allowed.formats() ? style.applyFormat(formatting) : null;
	}
}
