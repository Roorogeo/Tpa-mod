package com.roorogeo.essentials.command.moderation;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.NamedLocations;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /jails}: lists jails.
 */
public final class JailsCommand extends EssentialsCommand {
	public JailsCommand() {
		super("jails", PermissionNodes.JAILS);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			if (NamedLocations.JAILS.all().isEmpty()) {
				send(ctx.getSource(), "jail.list-empty");
				return 0;
			}

			send(ctx.getSource(), "jail.list", "jails", String.join(", ", NamedLocations.JAILS.all().keySet()));
			return 1;
		}));
	}
}
