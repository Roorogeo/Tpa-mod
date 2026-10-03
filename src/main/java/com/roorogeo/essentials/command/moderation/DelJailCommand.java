package com.roorogeo.essentials.command.moderation;

import static net.minecraft.commands.Commands.argument;

import java.util.Locale;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.NamedLocations;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.JailService;

/**
 * {@code /deljail <name>}: deletes a jail and releases everyone held in it.
 */
public final class DelJailCommand extends EssentialsCommand {
	public DelJailCommand() {
		super("deljail", PermissionNodes.DELJAIL);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("jail", StringArgumentType.word())
				.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(NamedLocations.JAILS.all().keySet(), builder))
				.executes(this.run(ctx -> {
					String name = StringArgumentType.getString(ctx, "jail").toLowerCase(Locale.ROOT);

					if (!NamedLocations.JAILS.remove(name)) {
						throw error("jail.not-found", "jail", name);
					}

					JailService.onJailDeleted(ctx.getSource().getServer(), name);
					send(ctx.getSource(), "jail.deleted", "jail", name);
					return 1;
				})));
	}
}
