package com.roorogeo.essentials.command.teleport;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.teleport.TeleportRequest;
import com.roorogeo.essentials.teleport.TeleportService;
import com.roorogeo.essentials.text.Messages;

/**
 * {@code /back}: return to where you were before your last teleport or death.
 */
public final class BackCommand extends EssentialsCommand {
	public BackCommand() {
		super("back", PermissionNodes.BACK);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			ServerPlayer player = player(ctx.getSource());
			Location target = PlayerDataStore.get(player).lastLocation;

			if (target == null) {
				throw error("back.none");
			}

			return TeleportService.start(player, TeleportRequest.fixed("back", Messages.get("back.destination"), target, true)) ? 1 : 0;
		}));
	}
}
