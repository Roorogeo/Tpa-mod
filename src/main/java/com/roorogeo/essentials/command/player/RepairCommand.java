package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /repair [hand|all]}: repairs the held item, or every item (needs {@code essentials.repair.all}).
 * Items listed in {@code player.repair-blacklist} are never repaired.
 */
public final class RepairCommand extends EssentialsCommand {
	public RepairCommand() {
		super("repair", PermissionNodes.REPAIR);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> hand(ctx.getSource())))
				.then(literal("hand").executes(this.run(ctx -> hand(ctx.getSource()))))
				.then(literal("all")
						.requires(requires(PermissionNodes.REPAIR_ALL))
						.executes(this.run(ctx -> {
							ServerPlayer player = player(ctx.getSource());
							Inventory inventory = player.getInventory();
							int count = 0;

							for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
								if (repair(inventory.getItem(slot))) {
									count++;
								}
							}

							if (count == 0) {
								throw error("repair.none");
							}

							send(ctx.getSource(), "repair.all", "count", count);
							return count;
						})));
	}

	private static int hand(CommandSourceStack source) throws CommandSyntaxException {
		ServerPlayer player = player(source);
		ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

		if (stack.isEmpty() || !stack.isDamageableItem() || isBlacklisted(stack)) {
			throw error("repair.not-repairable");
		}

		if (!repair(stack)) {
			throw error("repair.none");
		}

		send(source, "repair.hand", "item", stack.getHoverName());
		return 1;
	}

	private static boolean repair(ItemStack stack) {
		if (stack.isEmpty() || !stack.isDamageableItem() || !stack.isDamaged() || isBlacklisted(stack)) {
			return false;
		}

		stack.setDamageValue(0);
		return true;
	}

	private static boolean isBlacklisted(ItemStack stack) {
		return ConfigManager.config().player.repairBlacklist.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
	}
}
