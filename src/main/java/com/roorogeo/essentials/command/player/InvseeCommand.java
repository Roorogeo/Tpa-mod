package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.menu.InventoryView;
import com.roorogeo.essentials.menu.ViewMenu;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /invsee <player>}: shows a player's inventory, armor and offhand. Read-only unless you have
 * {@code essentials.invsee.modify}.
 */
public final class InvseeCommand extends EssentialsCommand {
	public InvseeCommand() {
		super("invsee", PermissionNodes.INVSEE);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.ONLINE)
				.executes(this.run(ctx -> {
					ServerPlayer viewer = player(ctx.getSource());
					ServerPlayer target = onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));

					if (target == viewer) {
						throw error("invsee.self");
					}

					boolean editable = has(ctx.getSource(), PermissionNodes.INVSEE_MODIFY);
					InventoryView view = new InventoryView(target);
					viewer.openMenu(new SimpleMenuProvider((id, inventory, p) -> ViewMenu.sixRows(id, inventory, view, editable),
							Messages.get("invsee.title", "player", DisplayNames.realName(target))));
					return 1;
				})));
	}
}
