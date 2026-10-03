package com.roorogeo.essentials.command.chat;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.ChatService;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /msg <player> <message>}: private message. Replaces vanilla /msg, /tell and /w by default.
 */
public final class MsgCommand extends EssentialsCommand {
	public MsgCommand() {
		super("msg", PermissionNodes.MSG);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.ONLINE)
				.then(argument("message", StringArgumentType.greedyString())
						.executes(this.run(ctx -> {
							CommandSourceStack source = ctx.getSource();
							String name = StringArgumentType.getString(ctx, "player");
							ServerPlayer target = ConfigManager.config().messaging.allowMessagingVanished
									? PlayerLookup.onlineAny(source.getServer(), name)
									: PlayerLookup.online(source, name);

							if (target == null) {
								throw error("general.player-not-found", "player", name);
							}

							if (source.getEntity() == target) {
								throw error("msg.self");
							}

							ChatService.sendPrivate(source, target, StringArgumentType.getString(ctx, "message"));
							return 1;
						}))));
	}
}
