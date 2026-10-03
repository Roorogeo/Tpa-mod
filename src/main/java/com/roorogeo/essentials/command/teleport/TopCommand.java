package com.roorogeo.essentials.command.teleport;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.teleport.TeleportRequest;
import com.roorogeo.essentials.teleport.TeleportService;
import com.roorogeo.essentials.text.Messages;

/**
 * {@code /top}: teleport onto the highest block above you.
 */
public final class TopCommand extends EssentialsCommand {
	public TopCommand() {
		super("top", PermissionNodes.TOP);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			ServerPlayer player = player(ctx.getSource());
			ServerLevel level = player.level();
			int x = player.getBlockX();
			int z = player.getBlockZ();
			int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

			if (y <= level.getMinY()) {
				throw error("top.none");
			}

			Location target = Location.of(level, new Vec3(x + 0.5, y, z + 0.5), player.getYRot(), player.getXRot());
			return TeleportService.start(player, TeleportRequest.fixed("top", Messages.get("top.destination"), target, true)) ? 1 : 0;
		}));
	}
}
