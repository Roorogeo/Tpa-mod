package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.teleport.TpaService;

/**
 * {@code /tpaccept [player]}: accept the newest request, or the one from {@code player}.
 */
public final class TpAcceptCommand extends EssentialsCommand {
	/** Suggests the names of players with a pending request to the source. */
	static final SuggestionProvider<CommandSourceStack> INCOMING = (ctx, builder) -> {
		ServerPlayer player = ctx.getSource().getPlayer();
		List<String> names = player == null ? List.of() : TpaService.incoming(player.getUUID()).stream().map(TpaService.Request::senderName).toList();
		return SharedSuggestionProvider.suggest(names, builder);
	};

	public TpAcceptCommand() {
		super("tpaccept", PermissionNodes.TPACCEPT);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> accept(ctx.getSource(), null)))
				.then(argument("player", StringArgumentType.word())
						.suggests(INCOMING)
						.executes(this.run(ctx -> accept(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));
	}

	private static int accept(CommandSourceStack source, @Nullable String from) throws CommandSyntaxException {
		ServerPlayer player = player(source);
		TpaService.accept(player, find(player, from));
		return 1;
	}

	/** The pending request to {@code player}, from {@code from} if given. */
	static TpaService.Request find(ServerPlayer player, @Nullable String from) throws CommandSyntaxException {
		UUID sender = null;

		if (from != null) {
			for (TpaService.Request request : TpaService.incoming(player.getUUID())) {
				if (request.senderName().equalsIgnoreCase(from)) {
					sender = request.sender();
					break;
				}
			}

			if (sender == null) {
				throw error("tpa.no-pending-from", "player", from);
			}
		}

		TpaService.Request request = TpaService.find(player.getUUID(), sender);

		if (request == null) {
			throw from == null ? error("tpa.no-pending") : error("tpa.no-pending-from", "player", from);
		}

		return request;
	}
}
