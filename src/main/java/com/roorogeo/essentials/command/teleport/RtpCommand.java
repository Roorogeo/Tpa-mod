package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.teleport.RandomTeleport;
import com.roorogeo.essentials.teleport.TeleportRequest;
import com.roorogeo.essentials.teleport.TeleportService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /rtp [player]}: teleport to a random safe location (see the {@code rtp} config section).
 */
public final class RtpCommand extends EssentialsCommand {
	public RtpCommand() {
		super("rtp", PermissionNodes.RTP);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> rtp(player(ctx.getSource()), true)))
				.then(argument("player", StringArgumentType.word())
						.requires(requires(PermissionNodes.RTP_OTHERS))
						.suggests(PlayerLookup.ONLINE)
						.executes(this.run(ctx -> {
							ServerPlayer target = onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
							rtp(target, false);
							send(ctx.getSource(), "rtp.sent-other", "player", DisplayNames.of(target));
							return 1;
						})));
	}

	private static int rtp(ServerPlayer player, boolean self) throws CommandSyntaxException {
		ServerLevel level = RandomTeleport.targetLevel(player);

		if (level == null) {
			throw error("general.unknown-world", "world", ConfigManager.config().rtp.targetDimension);
		}

		if (self && !TeleportService.precheck(player, "rtp")) {
			return 0;
		}

		Messages.send(player, "rtp.searching");
		RandomTeleport.search(player, level, location -> {
			ServerPlayer current = level.getServer().getPlayerList().getPlayer(player.getUUID());

			if (current == null) {
				return;
			}

			if (location == null) {
				Messages.send(current, "rtp.failed");
				return;
			}

			TeleportService.start(current, TeleportRequest.fixed("rtp",
					Messages.get("rtp.destination", "x", location.blockX(), "y", location.blockY(), "z", location.blockZ()), location, self));
		});
		return 1;
	}
}
