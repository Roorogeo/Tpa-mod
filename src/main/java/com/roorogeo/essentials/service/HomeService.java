package com.roorogeo.essentials.service;

import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.text.Messages;

/**
 * Home limits.
 *
 * <ol>
 *     <li>{@code essentials.sethome.unlimited}: no per-group limit, only {@code homes.absolute-max-homes}
 *     (truly unlimited when that is -1).</li>
 *     <li>Otherwise the highest granted {@code essentials.sethome.multiple.<n>}, or
 *     {@code homes.default-max-homes} when none is granted.</li>
 *     <li>The result is clamped to {@code homes.absolute-max-homes} unless that is -1.</li>
 * </ol>
 * Nodes are probed from {@code homes.multiple-scan-limit} (or the absolute cap, if higher) downwards.
 */
public final class HomeService {
	public static final int UNLIMITED = Integer.MAX_VALUE;

	private HomeService() {
	}

	public static int maxHomes(ServerPlayer player) {
		EssentialsConfig.Homes config = ConfigManager.config().homes;
		int absolute = config.absoluteMaxHomes;

		if (Perms.check(player, PermissionNodes.SETHOME_UNLIMITED)) {
			return absolute < 0 ? UNLIMITED : absolute;
		}

		int scanTo = Math.max(config.multipleScanLimit, absolute);
		int limit = config.defaultMaxHomes;

		for (int n = scanTo; n >= 1; n--) {
			if (Perms.check(player, Perms.named(PermissionNodes.SETHOME_MULTIPLE, Integer.toString(n)))) {
				limit = n;
				break;
			}
		}

		return absolute < 0 ? limit : Math.min(limit, absolute);
	}

	/** "3" or the homes.unlimited message. */
	public static String describe(int max) {
		return max == UNLIMITED ? Messages.plain("homes.unlimited") : Integer.toString(max);
	}
}
