package com.roorogeo.essentials.command.teleport;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelData;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.SpawnStore;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /setspawn}: sets the Essentials spawn and the vanilla world spawn to your position.
 */
public final class SetSpawnCommand extends EssentialsCommand {
	public SetSpawnCommand() {
		super("setspawn", PermissionNodes.SETSPAWN);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			ServerPlayer player = player(ctx.getSource());
			SpawnStore.set(Location.of(player));
			ctx.getSource().getServer().setRespawnData(LevelData.RespawnData.of(player.level().dimension(), player.blockPosition(), player.getYRot(), player.getXRot()));
			send(ctx.getSource(), "spawn.set");
			return 1;
		}));
	}
}
