package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /feed [player]}: fills hunger and saturation.
 */
public final class FeedCommand extends EssentialsCommand {
	public FeedCommand() {
		super("feed", PermissionNodes.FEED);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> feed(ctx.getSource(), player(ctx.getSource()))))
				.then(argument("player", StringArgumentType.word())
						.requires(requires(PermissionNodes.FEED_OTHERS))
						.suggests(PlayerLookup.ONLINE)
						.executes(this.run(ctx -> feed(ctx.getSource(), onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"))))));
	}

	private static int feed(CommandSourceStack source, ServerPlayer target) {
		target.getFoodData().setFoodLevel(20);
		target.getFoodData().setSaturation(20.0f);
		Messages.send(target, "feed.fed");

		if (source.getEntity() != target) {
			send(source, "feed.fed-other", "player", DisplayNames.of(target));
		}

		return 1;
	}
}
