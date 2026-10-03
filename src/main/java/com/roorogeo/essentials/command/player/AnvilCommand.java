package com.roorogeo.essentials.command.player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.menu.AnywhereMenus;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.Messages;

/**
 * {@code /anvil}: opens an anvil anywhere (it never breaks).
 */
public final class AnvilCommand extends EssentialsCommand {
	public AnvilCommand() {
		super("anvil", PermissionNodes.ANVIL);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			AnywhereMenus.openAnvil(player(ctx.getSource()), Messages.get("anvil.title"));
			return 1;
		}));
	}
}
