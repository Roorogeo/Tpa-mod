package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.teleport.TeleportRequest;
import com.roorogeo.essentials.teleport.TeleportService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /tpall [player]}: pull every online player to you, or to {@code player}.
 */
public final class TpAllCommand extends EssentialsCommand {
	public TpAllCommand() {
		super("tpall", PermissionNodes.TPALL);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> teleportAll(ctx.getSource(), player(ctx.getSource()))))
				.then(argument("player", StringArgumentType.word())
						.suggests(PlayerLookup.ONLINE)
						.executes(this.run(ctx -> teleportAll(ctx.getSource(), onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"))))));
	}

	private static int teleportAll(CommandSourceStack source, ServerPlayer destination) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		Component name = DisplayNames.of(destination);
		int count = 0;

		for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
			if (player == destination) {
				continue;
			}

			TeleportService.start(player, new TeleportRequest("tpall", name, () -> destination.isRemoved() ? null : Location.of(destination), false));
			Messages.send(player, "tp.notify", "target", name, "sender", source.getDisplayName());
			count++;
		}

		if (count == 0) {
			throw error("tpall.none");
		}

		send(source, "tpall.success", "count", count, "target", name);
		return count;
	}
}
