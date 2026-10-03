package com.roorogeo.essentials.command.moderation;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.NamedLocations;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.util.Names;

/**
 * {@code /setjail <name>}: creates or moves a jail to your position.
 */
public final class SetJailCommand extends EssentialsCommand {
	private static final String NAME_PATTERN = "[a-z0-9_-]{1,32}";

	public SetJailCommand() {
		super("setjail", PermissionNodes.SETJAIL);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("jail", StringArgumentType.word())
				.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(NamedLocations.JAILS.all().keySet(), builder))
				.executes(this.run(ctx -> {
					String name = Names.normalize(StringArgumentType.getString(ctx, "jail"), NAME_PATTERN);

					if (name == null) {
						throw error("jail.invalid-name");
					}

					NamedLocations.JAILS.set(name, Location.of(player(ctx.getSource())));
					send(ctx.getSource(), "jail.set", "jail", name);
					return 1;
				})));
	}
}
