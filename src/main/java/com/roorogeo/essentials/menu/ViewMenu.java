package com.roorogeo.essentials.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * A chest screen showing someone else's container (/invsee, /enderchest). When {@code editable}
 * is false every click is refused and the client is resynced, so the view is read-only.
 */
public final class ViewMenu extends ChestMenu {
	private final boolean editable;

	private ViewMenu(MenuType<?> type, int id, Inventory viewer, Container container, int rows, boolean editable) {
		super(type, id, viewer, container, rows);
		this.editable = editable;
	}

	public static ViewMenu threeRows(int id, Inventory viewer, Container container, boolean editable) {
		return new ViewMenu(MenuType.GENERIC_9x3, id, viewer, container, 3, editable);
	}

	public static ViewMenu sixRows(int id, Inventory viewer, Container container, boolean editable) {
		return new ViewMenu(MenuType.GENERIC_9x6, id, viewer, container, 6, editable);
	}

	@Override
	public void clicked(int slot, int button, ContainerInput input, Player player) {
		if (this.editable) {
			super.clicked(slot, button, input, player);
		} else {
			this.sendAllDataToRemote();
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return this.editable ? super.quickMoveStack(player, slot) : ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.getContainer().stillValid(player);
	}
}
