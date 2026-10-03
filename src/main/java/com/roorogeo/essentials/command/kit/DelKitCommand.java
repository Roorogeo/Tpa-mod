package com.roorogeo.essentials.command.kit;

import static net.minecraft.commands.Commands.argument;

import java.util.Locale;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.KitStore;
import com.roorogeo.essentials.perm.PermissionNodes;

/**
 * {@code /delkit <name>}.
 */
public final class DelKitCommand extends EssentialsCommand {
	public DelKitCommand() {
		super("delkit", PermissionNodes.DELKIT);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("kit", StringArgumentType.word())
				.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(KitStore.all().keySet(), builder))
				.executes(this.run(ctx -> {
					String name = StringArgumentType.getString(ctx, "kit").toLowerCase(Locale.ROOT);

					if (!KitStore.remove(name)) {
						throw error("kit.not-found", "kit", name);
					}

					send(ctx.getSource(), "delkit.deleted", "kit", name);
					return 1;
				})));
	}
}
