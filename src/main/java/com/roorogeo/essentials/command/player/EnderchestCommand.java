package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.menu.ViewMenu;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /enderchest [player]}: opens your ender chest, or someone else's (read-only unless you have
 * {@code essentials.enderchest.modify}).
 */
public final class EnderchestCommand extends EssentialsCommand {
	public EnderchestCommand() {
		super("enderchest", PermissionNodes.ENDERCHEST);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			ServerPlayer player = player(ctx.getSource());
			player.openMenu(new SimpleMenuProvider((id, inventory, p) -> ViewMenu.threeRows(id, inventory, player.getEnderChestInventory(), true),
					Messages.get("enderchest.title")));
			return 1;
		})).then(argument("player", StringArgumentType.word())
				.requires(requires(PermissionNodes.ENDERCHEST_OTHERS))
				.suggests(PlayerLookup.ONLINE)
				.executes(this.run(ctx -> {
					ServerPlayer viewer = player(ctx.getSource());
					ServerPlayer target = onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
					boolean editable = target == viewer || has(ctx.getSource(), PermissionNodes.ENDERCHEST_MODIFY);
					viewer.openMenu(new SimpleMenuProvider((id, inventory, p) -> ViewMenu.threeRows(id, inventory, target.getEnderChestInventory(), editable),
							Messages.get("enderchest.title-other", "player", DisplayNames.realName(target))));
					return 1;
				})));
	}
}
