package com.roorogeo.essentials.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

/**
 * Crafting table and anvil screens that work without the block. Vanilla closes these screens when
 * the block isn't there, so {@code stillValid} is overridden. The level access still points at the
 * player's position so crafting results are computed and sounds play; nothing is ever changed in
 * the world, because the anvil only damages blocks that are actually anvils.
 */
public final class AnywhereMenus {
	private AnywhereMenus() {
	}

	public static void openWorkbench(ServerPlayer player, Component title) {
		ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), player.blockPosition());
		player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new CraftingMenu(id, inventory, access) {
			@Override
			public boolean stillValid(Player viewer) {
				return true;
			}
		}, title));
	}

	public static void openAnvil(ServerPlayer player, Component title) {
		ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), player.blockPosition());
		player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new AnvilMenu(id, inventory, access) {
			@Override
			public boolean stillValid(Player viewer) {
				return true;
			}
		}, title));
	}
}
