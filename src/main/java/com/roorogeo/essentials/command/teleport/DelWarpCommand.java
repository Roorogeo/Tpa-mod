package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import java.util.Locale;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.NamedLocations;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /delwarp <name>}.
 */
public final class DelWarpCommand extends EssentialsCommand {
	public DelWarpCommand() {
		super("delwarp", PermissionNodes.DELWARP);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("warp", StringArgumentType.word())
				.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(NamedLocations.WARPS.all().keySet(), builder))
				.executes(this.run(ctx -> {
					String name = StringArgumentType.getString(ctx, "warp").toLowerCase(Locale.ROOT);

					if (!NamedLocations.WARPS.remove(name)) {
						throw error("warp.not-found", "warp", name);
					}

					send(ctx.getSource(), "warp.deleted", "warp", name);
					return 1;
				})));
	}
}
