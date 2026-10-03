package com.roorogeo.essentials.command.moderation;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /unmute <player>}.
 */
public final class UnmuteCommand extends EssentialsCommand {
	public UnmuteCommand() {
		super("unmute", PermissionNodes.UNMUTE);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.KNOWN)
				.executes(this.run(ctx -> {
					PlayerData data = knownPlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));

					if (!data.isMuted()) {
						throw error("unmute.not-muted", "player", data.name);
					}

					data.mutedUntil = 0;
					data.muteReason = "";
					data.markDirty();
					send(ctx.getSource(), "unmute.unmuted", "player", data.name);
					ServerPlayer online = online(ctx.getSource(), data);

					if (online != null) {
						Messages.send(online, "unmute.notify");
					}

					return 1;
				})));
	}
}
