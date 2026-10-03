package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /delhome <name>} or {@code /delhome <player>:<name>}.
 */
public final class DelHomeCommand extends EssentialsCommand {
	public DelHomeCommand() {
		super("delhome", PermissionNodes.DELHOME);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("home", StringArgumentType.greedyString())
				.suggests(HomeTarget.suggestions(PermissionNodes.DELHOME_OTHERS))
				.executes(this.run(ctx -> {
					CommandSourceStack source = ctx.getSource();
					ServerPlayer player = player(source);
					HomeTarget target = HomeTarget.parse(source, player, StringArgumentType.getString(ctx, "home"), PermissionNodes.DELHOME_OTHERS);
					String name = target.name();

					if (name == null || target.data().homes.remove(name) == null) {
						throw target.other()
								? error("home.not-found-other", "player", target.data().name, "home", String.valueOf(name))
								: error("home.not-found", "home", String.valueOf(name));
					}

					target.data().markDirty();

					if (target.other()) {
						send(source, "home.deleted-other", "player", target.data().name, "home", name);
					} else {
						send(source, "home.deleted", "home", name);
					}

					return 1;
				})));
	}
}
