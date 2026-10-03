package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.SpawnStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.teleport.TeleportRequest;
import com.roorogeo.essentials.teleport.TeleportService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /spawn [player]}: teleport to the server spawn (set with /setspawn, otherwise the world spawn).
 */
public final class SpawnCommand extends EssentialsCommand {
	public SpawnCommand() {
		super("spawn", PermissionNodes.SPAWN);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			ServerPlayer player = player(ctx.getSource());
			return TeleportService.start(player, request(ctx.getSource().getServer(), true)) ? 1 : 0;
		})).then(argument("player", StringArgumentType.word())
				.requires(requires(PermissionNodes.SPAWN_OTHERS))
				.suggests(PlayerLookup.ONLINE)
				.executes(this.run(ctx -> {
					ServerPlayer target = onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
					TeleportService.start(target, request(ctx.getSource().getServer(), false));
					send(ctx.getSource(), "spawn.sent-other", "player", DisplayNames.of(target));
					Messages.send(target, "spawn.sent-by", "sender", ctx.getSource().getDisplayName());
					return 1;
				})));
	}

	private static TeleportRequest request(MinecraftServer server, boolean playerInitiated) {
		return new TeleportRequest("spawn", Messages.get("spawn.destination"), () -> SpawnStore.get(server), playerInitiated);
	}
}
