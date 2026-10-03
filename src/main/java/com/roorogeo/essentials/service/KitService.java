package com.roorogeo.essentials.service;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.KitStore;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.text.Messages;

/**
 * Kit cooldowns and delivery.
 */
public final class KitService {
	private KitService() {
	}

	public static boolean mayUse(ServerPlayer player, KitStore.Kit kit) {
		return Perms.check(player, Perms.named(PermissionNodes.KIT_NAMED, kit.name()));
	}

	/**
	 * Milliseconds until {@code player} may claim the kit again: 0 when available, -1 when it was a
	 * one-time kit that was already claimed.
	 */
	public static long remaining(ServerPlayer player, KitStore.Kit kit) {
		if (Perms.check(player, PermissionNodes.KIT_COOLDOWN_BYPASS)) {
			return 0;
		}

		Long lastUse = PlayerDataStore.get(player).kitUses.get(kit.name());

		if (lastUse == null) {
			return 0;
		}

		if (kit.oneTime()) {
			return -1;
		}

		return Math.max(0, lastUse + kit.cooldownSeconds() * 1000L - System.currentTimeMillis());
	}

	/**
	 * Gives the kit's items. Respects {@code kits.overflow}: "deny" refuses when the inventory is too
	 * full, "drop" drops what doesn't fit. Returns false if the kit was refused.
	 *
	 * @param recordUse whether to start the cooldown (false for kits given by staff)
	 */
	public static boolean give(ServerPlayer player, KitStore.Kit kit, boolean recordUse) {
		boolean deny = "deny".equalsIgnoreCase(ConfigManager.config().kits.overflow);

		if (deny && freeSlots(player) < kit.items().size()) {
			Messages.send(player, "kit.inventory-full", "kit", kit.name());
			return false;
		}

		List<ItemStack> leftovers = new ArrayList<>();

		for (ItemStack item : kit.items()) {
			ItemStack copy = item.copy();
			player.getInventory().add(copy);

			if (!copy.isEmpty()) {
				leftovers.add(copy);
			}
		}

		for (ItemStack leftover : leftovers) {
			player.drop(leftover, false);
		}

		if (!leftovers.isEmpty()) {
			Messages.send(player, "kit.dropped");
		}

		if (recordUse) {
			PlayerData data = PlayerDataStore.get(player);
			data.kitUses.put(kit.name(), System.currentTimeMillis());
			data.markDirty();
		}

		return true;
	}

	private static int freeSlots(ServerPlayer player) {
		int free = 0;

		for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
			if (stack.isEmpty()) {
				free++;
			}
		}

		return free;
	}
}
