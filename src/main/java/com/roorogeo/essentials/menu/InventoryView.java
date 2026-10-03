package com.roorogeo.essentials.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Shows a player's whole inventory (36 slots, 4 armor slots and the offhand: 41 slots in vanilla's
 * slot order) as a 54-slot double chest for /invsee. The extra 13 slots stay empty and accept nothing.
 */
public final class InventoryView implements Container {
	private static final int SIZE = 54;

	private final ServerPlayer target;
	private final Inventory inventory;

	public InventoryView(ServerPlayer target) {
		this.target = target;
		this.inventory = target.getInventory();
	}

	private boolean mapped(int slot) {
		return slot >= 0 && slot < this.inventory.getContainerSize();
	}

	@Override
	public int getContainerSize() {
		return SIZE;
	}

	@Override
	public boolean isEmpty() {
		return this.inventory.isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.mapped(slot) ? this.inventory.getItem(slot) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int count) {
		return this.mapped(slot) ? this.inventory.removeItem(slot, count) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return this.mapped(slot) ? this.inventory.removeItemNoUpdate(slot) : ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (this.mapped(slot)) {
			this.inventory.setItem(slot, stack);
		}
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return this.mapped(slot);
	}

	@Override
	public void setChanged() {
		this.inventory.setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return !this.target.isRemoved() && !this.target.hasDisconnected();
	}

	@Override
	public void clearContent() {
		this.inventory.clearContent();
	}
}
