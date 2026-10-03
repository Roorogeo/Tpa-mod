package com.roorogeo.essentials.command.moderation;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.VanishService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /vanish [player]}: toggles being hidden from players without {@code essentials.vanish.see}.
 */
public final class VanishCommand extends EssentialsCommand {
	public VanishCommand() {
		super("vanish", PermissionNodes.VANISH);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> toggle(ctx.getSource(), player(ctx.getSource()))))
				.then(argument("player", StringArgumentType.word())
						.requires(requires(PermissionNodes.VANISH_OTHERS))
						.suggests(PlayerLookup.ONLINE)
						.executes(this.run(ctx -> toggle(ctx.getSource(), onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"))))));
	}

	private static int toggle(CommandSourceStack source, ServerPlayer target) {
		boolean vanish = !VanishService.isVanished(target);
		VanishService.setVanished(target, vanish);
		Messages.send(target, vanish ? "vanish.enabled" : "vanish.disabled");

		if (source.getEntity() != target) {
			send(source, vanish ? "vanish.enabled-other" : "vanish.disabled-other", "player", DisplayNames.of(target));
		}

		return 1;
	}
}
