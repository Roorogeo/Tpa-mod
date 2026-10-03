package com.roorogeo.essentials.command.player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.combat.CombatService;
import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /combat}: shows how long you stay combat tagged.
 */
public final class CombatCommand extends EssentialsCommand {
	public CombatCommand() {
		super("combat", PermissionNodes.COMBAT_CHECK);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			ServerPlayer player = player(ctx.getSource());
			long remaining = CombatService.remainingMillis(player.getUUID());

			if (remaining > 0) {
				send(ctx.getSource(), "combat.status-tagged", "seconds", (remaining + 999) / 1000);
			} else {
				send(ctx.getSource(), "combat.status-free");
			}

			return 1;
		}));
	}
}
