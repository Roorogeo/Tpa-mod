package com.roorogeo.essentials.command.moderation;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandSourceStack;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.service.FreezeService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /freeze <player>}: toggles freezing a player in place.
 */
public final class FreezeCommand extends EssentialsCommand {
	public FreezeCommand() {
		super("freeze", PermissionNodes.FREEZE);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.ONLINE)
				.executes(this.run(ctx -> {
					ServerPlayer target = onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
					boolean freeze = !FreezeService.isFrozen(target);

					if (freeze && Perms.check(target, PermissionNodes.FREEZE_EXEMPT)) {
						throw error("freeze.exempt", "player", DisplayNames.realName(target));
					}

					FreezeService.setFrozen(target, freeze);
					Messages.send(target, freeze ? "freeze.notify-frozen" : "freeze.notify-unfrozen");
					send(ctx.getSource(), freeze ? "freeze.frozen" : "freeze.unfrozen", "player", DisplayNames.of(target));
					return 1;
				})));
	}
}
