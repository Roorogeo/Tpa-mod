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
 * {@code /tphere <player>}: pull a player to you.
 */
public final class TpHereCommand extends EssentialsCommand {
	public TpHereCommand() {
		super("tphere", PermissionNodes.TPHERE);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.ONLINE)
				.executes(this.run(ctx -> {
					ServerPlayer self = player(ctx.getSource());
					ServerPlayer target = onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));

					if (target == self) {
						throw error("tpa.self");
					}

					Component selfName = DisplayNames.of(self);
					TeleportService.start(target, new TeleportRequest("tphere", selfName, () -> self.isRemoved() ? null : Location.of(self), false));
					send(ctx.getSource(), "tphere.success", "player", DisplayNames.of(target));
					Messages.send(target, "tp.notify", "target", selfName, "sender", selfName);
					return 1;
				})));
	}
}
