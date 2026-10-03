package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.NamedLocations;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.util.Names;

/**
 * {@code /setwarp <name>}: creates a warp at your position, or moves an existing one.
 */
public final class SetWarpCommand extends EssentialsCommand {
	public SetWarpCommand() {
		super("setwarp", PermissionNodes.SETWARP);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("warp", StringArgumentType.word())
				.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(NamedLocations.WARPS.all().keySet(), builder))
				.executes(this.run(ctx -> {
					ServerPlayer player = player(ctx.getSource());
					String name = Names.normalize(StringArgumentType.getString(ctx, "warp"), ConfigManager.config().warps.namePattern);

					if (name == null) {
						throw error("warp.invalid-name");
					}

					if (name.equals("others")) {
						throw error("warp.reserved-name", "warp", name);
					}

					boolean exists = NamedLocations.WARPS.get(name) != null;
					NamedLocations.WARPS.set(name, Location.of(player));
					send(ctx.getSource(), exists ? "warp.updated" : "warp.set", "warp", name);
					return 1;
				})));
	}
}
