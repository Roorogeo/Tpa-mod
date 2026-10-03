package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /clearinventory [player]}: empties the inventory (armor and offhand too when
 * {@code player.clear-inventory-includes-armor}).
 */
public final class ClearInventoryCommand extends EssentialsCommand {
	public ClearInventoryCommand() {
		super("clearinventory", PermissionNodes.CLEARINVENTORY);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> clear(ctx.getSource(), player(ctx.getSource()))))
				.then(argument("player", StringArgumentType.word())
						.requires(requires(PermissionNodes.CLEARINVENTORY_OTHERS))
						.suggests(PlayerLookup.ONLINE)
						.executes(this.run(ctx -> clear(ctx.getSource(), onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"))))));
	}

	private static int clear(CommandSourceStack source, ServerPlayer target) {
		Inventory inventory = target.getInventory();

		if (ConfigManager.config().player.clearInventoryIncludesArmor) {
			inventory.clearContent();
		} else {
			for (int slot = 0; slot < inventory.getNonEquipmentItems().size(); slot++) {
				inventory.setItem(slot, ItemStack.EMPTY);
			}
		}

		Messages.send(target, "clearinventory.cleared");

		if (source.getEntity() != target) {
			send(source, "clearinventory.cleared-other", "player", DisplayNames.of(target));
		}

		return 1;
	}
}
