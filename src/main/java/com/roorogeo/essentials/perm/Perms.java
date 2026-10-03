package com.roorogeo.essentials.perm;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import me.lucko.fabric.api.permissions.v0.Permissions;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.util.TriState;

import com.roorogeo.essentials.Essentials;
import com.roorogeo.essentials.config.ConfigManager;

/**
 * Checks permission nodes through fabric-permissions-api, which LuckPerms implements.
 *
 * <p>When the permission mod has an explicit value for a node, that value wins. Otherwise the
 * fallback default applies, looked up in this order:
 * <ol>
 *     <li>the exact node in {@code permissions.defaults} (e.g. {@code essentials.kit.vip})</li>
 *     <li>the node's pattern in {@code permissions.defaults} (e.g. {@code essentials.kit.*})</li>
 *     <li>the built-in default from {@link PermissionNodes}</li>
 * </ol>
 * A default is {@code all}, {@code op} (operator level {@code permissions.op-level}),
 * {@code op:<level>} or {@code none}.
 */
public final class Perms {
	/** Resolved default: everyone, nobody, or an operator level. */
	private record Fallback(boolean everyone, int level) {
		private static final Fallback ALL = new Fallback(true, 0);
		/** Nobody except the server console (an entity-less source with the highest level). */
		private static final Fallback NONE = new Fallback(false, -1);
	}

	private static final Map<String, Fallback> CACHE = new HashMap<>();

	private Perms() {
	}

	/** Clears cached defaults after the config was reloaded. */
	public static void invalidate() {
		CACHE.clear();
	}

	/** Brigadier requirement for {@code .requires(...)}. */
	public static Predicate<CommandSourceStack> require(PermissionNodes.Node node) {
		return source -> check(source, node.id());
	}

	public static boolean check(SharedSuggestionProvider source, PermissionNodes.Node node) {
		return check(source, node.id());
	}

	public static boolean check(ServerPlayer player, PermissionNodes.Node node) {
		return check(player, node.id());
	}

	/** Checks a node for an online player (no command source needed). */
	public static boolean check(ServerPlayer player, String node) {
		TriState state = Permissions.getPermissionValue(player, node);

		if (state != TriState.DEFAULT) {
			return state.get();
		}

		Fallback fallback = fallback(node);

		if (fallback.everyone()) {
			return true;
		}

		return fallback.level() >= 0 && Permissions.check(player, node, fallback.level());
	}

	/** Checks a concrete node id, which may be an instance of a pattern like {@code essentials.warp.spawn}. */
	public static boolean check(SharedSuggestionProvider source, String node) {
		TriState state = Permissions.getPermissionValue(source, node);

		if (state != TriState.DEFAULT) {
			return state.get();
		}

		Fallback fallback = fallback(node);

		if (fallback.everyone()) {
			return true;
		}

		if (fallback.level() < 0) {
			return source instanceof CommandSourceStack stack && stack.getEntity() == null && Permissions.check(source, node, 4);
		}

		return Permissions.check(source, node, fallback.level());
	}

	/** {@code essentials.warp.<name>} for a specific warp. */
	public static String named(PermissionNodes.Node pattern, String value) {
		return pattern.patternPrefix() + value.toLowerCase(Locale.ROOT);
	}

	private static Fallback fallback(String node) {
		Fallback cached = CACHE.get(node);

		if (cached != null) {
			return cached;
		}

		Map<String, String> defaults = ConfigManager.config().permissions.defaults;
		String configured = defaults.get(node);
		PermissionNodes.Node registered = PermissionNodes.all().get(node);

		if (configured == null || registered == null) {
			for (PermissionNodes.Node pattern : PermissionNodes.patterns()) {
				if (node.startsWith(pattern.patternPrefix()) && node.length() > pattern.patternPrefix().length()
						&& node.indexOf('.', pattern.patternPrefix().length()) < 0) {
					if (configured == null) {
						configured = defaults.get(pattern.configKey());
					}

					if (registered == null) {
						registered = pattern;
					}

					break;
				}
			}
		}

		Fallback result = configured != null ? parse(node, configured) : null;

		if (result == null) {
			result = registered == null ? Fallback.NONE : fromLevel(registered.level());
		}

		CACHE.put(node, result);
		return result;
	}

	private static Fallback fromLevel(PermissionNodes.Level level) {
		return switch (level) {
			case ALL -> Fallback.ALL;
			case OP -> new Fallback(false, ConfigManager.config().permissions.opLevel);
			case NONE -> Fallback.NONE;
		};
	}

	private static Fallback parse(String node, String value) {
		String text = value.trim().toLowerCase(Locale.ROOT);

		switch (text) {
			case "all", "true", "everyone" -> {
				return Fallback.ALL;
			}
			case "none", "false", "nobody" -> {
				return Fallback.NONE;
			}
			case "op" -> {
				return new Fallback(false, ConfigManager.config().permissions.opLevel);
			}
			default -> {
				if (text.startsWith("op:")) {
					try {
						int level = Integer.parseInt(text.substring(3));
						return new Fallback(false, Math.max(0, Math.min(4, level)));
					} catch (NumberFormatException ignored) {
						// Fall through to the warning below.
					}
				}

				Essentials.LOGGER.warn("Invalid default '{}' for permission {} in config.json, using the built-in default", value, node);
				return null;
			}
		}
	}
}
