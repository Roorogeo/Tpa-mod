package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.AfkService;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /afk [message]} toggles your AFK status. With {@code essentials.afk.others},
 * {@code /afk <player>} toggles another player's status.
 */
public final class AfkCommand extends EssentialsCommand {
	public AfkCommand() {
		super("afk", PermissionNodes.AFK);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			ServerPlayer player = player(ctx.getSource());
			AfkService.setAfk(player, !AfkService.isAfk(player), null);
			return 1;
		})).then(argument("message", StringArgumentType.greedyString())
				.suggests((ctx, builder) -> has(ctx.getSource(), PermissionNodes.AFK_OTHERS) ? PlayerLookup.ONLINE.getSuggestions(ctx, builder) : builder.buildFuture())
				.executes(this.run(ctx -> {
					CommandSourceStack source = ctx.getSource();
					String text = StringArgumentType.getString(ctx, "message").trim();
					ServerPlayer other = !text.contains(" ") && has(source, PermissionNodes.AFK_OTHERS) ? PlayerLookup.online(source, text) : null;

					if (other != null && other != source.getEntity()) {
						AfkService.setAfk(other, !AfkService.isAfk(other), null);
						return 1;
					}

					ServerPlayer player = player(source);
					AfkService.setAfk(player, !AfkService.isAfk(player), text);
					return 1;
				})));
	}
}
