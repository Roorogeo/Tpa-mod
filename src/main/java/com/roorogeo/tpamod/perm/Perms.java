package com.roorogeo.tpamod.perm;

import java.util.function.Predicate;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

import net.fabricmc.fabric.api.permission.v1.PermissionNode;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;

import com.roorogeo.tpamod.TpaMod;
import com.roorogeo.tpamod.config.ModConfig;

/**
 * Permission nodes, checked through the Fabric Permission API.
 *
 * <p>Permission managers such as LuckPerms see a node {@code tpamod:foo/bar} as
 * {@code tpamod.foo.bar}. Without a permission manager, "everyone" nodes default to
 * allowed and "op" nodes fall back to vanilla op level 2 (gamemaster).
 */
public final class Perms {
	// Everyone by default
	public static final Identifier TPA = TpaMod.id("tpa");
	public static final Identifier TPAHERE = TpaMod.id("tpahere");
	public static final Identifier TPACCEPT = TpaMod.id("tpaccept");
	public static final Identifier HOME = TpaMod.id("home");
	public static final Identifier SETHOME = TpaMod.id("sethome");
	public static final Identifier DELHOME = TpaMod.id("delhome");
	public static final Identifier WARP = TpaMod.id("warp");

	// Op by default
	public static final Identifier SETWARP = TpaMod.id("setwarp");
	public static final Identifier DELWARP = TpaMod.id("delwarp");
	public static final Identifier RELOAD = TpaMod.id("reload");
	public static final Identifier BYPASS_WARMUP = TpaMod.id("bypass/warmup");
	public static final Identifier HOMES_UNLIMITED = TpaMod.id("homes/unlimited");

	/** Integer option; with LuckPerms set it as meta: {@code /lp group vip meta set tpamod:max_homes 10}. */
	public static final PermissionNode<Integer> MAX_HOMES = PermissionNode.ofInteger(TpaMod.id("max_homes"));

	public static final PermissionLevel OP_LEVEL = PermissionLevel.GAMEMASTERS;

	private Perms() {
	}

	public static Predicate<CommandSourceStack> everyone(Identifier node) {
		return PermissionPredicates.require(node, true);
	}

	public static Predicate<CommandSourceStack> op(Identifier node) {
		return PermissionPredicates.require(node, OP_LEVEL);
	}

	public static boolean canBypassWarmup(ServerPlayer player) {
		return player.checkPermission(BYPASS_WARMUP, OP_LEVEL);
	}

	/** Per-warp node {@code tpamod.warp.<name>}, allowed by default so warps can be locked individually. */
	public static boolean canUseWarp(CommandSourceStack source, String warp) {
		return source.checkPermission(TpaMod.id("warp/" + warp), true);
	}

	/**
	 * Resolves how many homes a player may have, in this order:
	 * <ol>
	 *     <li>{@code tpamod.homes.unlimited} (op by default): no limit</li>
	 *     <li>integer option {@code tpamod:max_homes}</li>
	 *     <li>the highest granted {@code tpamod.homes.limit.<n>} from {@link ModConfig#homeLimitSteps}</li>
	 *     <li>{@link ModConfig#defaultMaxHomes}</li>
	 * </ol>
	 */
	public static int maxHomes(ServerPlayer player) {
		if (player.checkPermission(HOMES_UNLIMITED, OP_LEVEL)) {
			return Integer.MAX_VALUE;
		}

		Integer option = player.checkPermission(MAX_HOMES);

		if (option != null) {
			return Math.max(0, option);
		}

		int best = -1;

		for (int step : ModConfig.get().homeLimitSteps) {
			if (step > best && player.checkPermission(TpaMod.id("homes/limit/" + step), false)) {
				best = step;
			}
		}

		return best >= 0 ? best : ModConfig.get().defaultMaxHomes;
	}
}
