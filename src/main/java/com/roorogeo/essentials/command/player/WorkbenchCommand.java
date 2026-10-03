package com.roorogeo.essentials.command.player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.menu.AnywhereMenus;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.Messages;

/**
 * {@code /workbench}: opens a crafting table anywhere.
 */
public final class WorkbenchCommand extends EssentialsCommand {
	public WorkbenchCommand() {
		super("workbench", PermissionNodes.WORKBENCH);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			AnywhereMenus.openWorkbench(player(ctx.getSource()), Messages.get("workbench.title"));
			return 1;
		}));
	}
}
