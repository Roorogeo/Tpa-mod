package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import java.util.List;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.teleport.TpaService;

/**
 * {@code /tpacancel [player]}: cancel all your outgoing requests, or only the one to {@code player}.
 */
public final class TpaCancelCommand extends EssentialsCommand {
	public TpaCancelCommand() {
		super("tpacancel", PermissionNodes.TPACANCEL);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> cancel(ctx.getSource(), null)))
				.then(argument("player", StringArgumentType.word())
						.suggests((ctx, builder) -> {
							ServerPlayer player = ctx.getSource().getPlayer();
							List<String> names = player == null ? List.of() : TpaService.outgoing(player.getUUID()).stream().map(TpaService.Request::targetName).toList();
							return SharedSuggestionProvider.suggest(names, builder);
						})
						.executes(this.run(ctx -> cancel(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));
	}

	private static int cancel(CommandSourceStack source, @Nullable String to) throws CommandSyntaxException {
		ServerPlayer player = player(source);
		int cancelled = 0;

		for (TpaService.Request request : TpaService.outgoing(player.getUUID())) {
			if (to == null || request.targetName().equalsIgnoreCase(to)) {
				TpaService.cancel(player, request);
				cancelled++;
			}
		}

		if (cancelled == 0) {
			throw error("tpa.no-outgoing");
		}

		return cancelled;
	}
}
