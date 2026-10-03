package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.teleport.TpaService;

/**
 * {@code /tpdeny [player]}: deny the newest request, or the one from {@code player}.
 */
public final class TpDenyCommand extends EssentialsCommand {
	public TpDenyCommand() {
		super("tpdeny", PermissionNodes.TPDENY);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> deny(ctx.getSource(), null)))
				.then(argument("player", StringArgumentType.word())
						.suggests(TpAcceptCommand.INCOMING)
						.executes(this.run(ctx -> deny(ctx.getSource(), StringArgumentType.getString(ctx, "player")))));
	}

	private static int deny(CommandSourceStack source, @Nullable String from) throws CommandSyntaxException {
		ServerPlayer player = player(source);
		TpaService.deny(player, TpAcceptCommand.find(player, from));
		return 1;
	}
}
