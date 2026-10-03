package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import java.util.Locale;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.NamedLocations;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.teleport.TeleportRequest;
import com.roorogeo.essentials.teleport.TeleportService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /warp [name] [player]}: teleport to a warp (each warp also needs {@code essentials.warp.<name>}).
 * Without a name it lists the warps.
 */
public final class WarpCommand extends EssentialsCommand {
	public WarpCommand() {
		super("warp", PermissionNodes.WARP);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> WarpsCommand.list(ctx.getSource())))
				.then(argument("warp", StringArgumentType.word())
						.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(WarpsCommand.usable(ctx.getSource()), builder))
						.executes(this.run(ctx -> this.warp(ctx, player(ctx.getSource()), true)))
						.then(argument("player", StringArgumentType.word())
								.requires(requires(PermissionNodes.WARP_OTHERS))
								.suggests(PlayerLookup.ONLINE)
								.executes(this.run(ctx -> {
									ServerPlayer target = onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
									int result = this.warp(ctx, target, false);
									send(ctx.getSource(), "warp.sent-other", "player", DisplayNames.of(target), "warp", StringArgumentType.getString(ctx, "warp"));
									return result;
								}))));
	}

	private int warp(CommandContext<CommandSourceStack> ctx, ServerPlayer mover, boolean self) throws CommandSyntaxException {
		String name = StringArgumentType.getString(ctx, "warp").toLowerCase(Locale.ROOT);

		if (NamedLocations.WARPS.get(name) == null) {
			throw error("warp.not-found", "warp", name);
		}

		if (ConfigManager.config().warps.perWarpPermissions && !Perms.check(ctx.getSource(), Perms.named(PermissionNodes.WARP_NAMED, name))) {
			throw error("warp.no-permission", "warp", name);
		}

		Component destination = Messages.get("warp.destination", "warp", name);
		return TeleportService.start(mover, new TeleportRequest("warp", destination, () -> NamedLocations.WARPS.get(name), self)) ? 1 : 0;
	}
}
