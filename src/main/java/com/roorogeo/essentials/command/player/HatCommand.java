package com.roorogeo.essentials.command.player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /hat}: wear the item in your hand. With an empty hand it takes your hat off.
 */
public final class HatCommand extends EssentialsCommand {
	public HatCommand() {
		super("hat", PermissionNodes.HAT);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			ServerPlayer player = player(ctx.getSource());
			ItemStack hand = player.getItemInHand(InteractionHand.MAIN_HAND);
			ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);

			if (!head.isEmpty() && EnchantmentHelper.has(head, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) && !player.isCreative()) {
				throw error("hat.binding");
			}

			if (hand.isEmpty()) {
				if (head.isEmpty()) {
					throw error("hat.empty");
				}

				player.setItemInHand(InteractionHand.MAIN_HAND, head);
				player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
				send(ctx.getSource(), "hat.removed");
				return 1;
			}

			player.setItemSlot(EquipmentSlot.HEAD, hand);
			player.setItemInHand(InteractionHand.MAIN_HAND, head);
			send(ctx.getSource(), "hat.success");
			return 1;
		}));
	}
}
