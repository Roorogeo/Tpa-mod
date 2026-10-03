package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.teleport.TeleportRequest;
import com.roorogeo.essentials.teleport.TeleportService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /tp <player>}, {@code /tp <player> <target>}, {@code /tp <x> <y> <z>} and
 * {@code /tp <player> <x> <y> <z>}. Staff teleports: instant by default (see {@code commands.tp}).
 */
public final class TpCommand extends EssentialsCommand {
	public TpCommand() {
		super("tp", PermissionNodes.TP);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("position", Vec3Argument.vec3())
						.requires(requires(PermissionNodes.TP_POSITION))
						.executes(this.run(ctx -> toPosition(ctx, player(ctx.getSource())))))
				.then(argument("target", StringArgumentType.word())
						.suggests(PlayerLookup.ONLINE)
						.executes(this.run(ctx -> toPlayer(ctx.getSource(), player(ctx.getSource()), onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "target")))))
						.then(argument("destination", StringArgumentType.word())
								.requires(requires(PermissionNodes.TP_OTHERS))
								.suggests(PlayerLookup.ONLINE)
								.executes(this.run(ctx -> toPlayer(ctx.getSource(),
										onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "target")),
										onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "destination"))))))
						.then(argument("position", Vec3Argument.vec3())
								.requires(source -> has(source, PermissionNodes.TP_OTHERS) && has(source, PermissionNodes.TP_POSITION))
								.executes(this.run(ctx -> toPosition(ctx, onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "target")))))));
	}

	private static int toPlayer(CommandSourceStack source, ServerPlayer mover, ServerPlayer target) {
		Component targetName = DisplayNames.of(target);
		boolean self = source.getEntity() == mover;
		TeleportService.start(mover, new TeleportRequest("tp", targetName, () -> target.isRemoved() ? null : Location.of(target), false));

		if (!self) {
			send(source, "tp.other", "player", DisplayNames.of(mover), "target", targetName);
			Messages.send(mover, "tp.notify", "target", targetName, "sender", source.getDisplayName());
		}

		return 1;
	}

	private static int toPosition(CommandContext<CommandSourceStack> ctx, ServerPlayer mover) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		Vec3 position = Vec3Argument.getVec3(ctx, "position");
		Location location = Location.of(source.getLevel(), position, mover.getYRot(), mover.getXRot());
		Component description = Messages.get("tp.position", "x", location.blockX(), "y", location.blockY(), "z", location.blockZ());
		TeleportService.start(mover, TeleportRequest.fixed("tp", description, location, false));

		if (source.getEntity() != mover) {
			send(source, "tp.other", "player", DisplayNames.of(mover), "target", description);
			Messages.send(mover, "tp.notify", "target", description, "sender", source.getDisplayName());
		}

		return 1;
	}
}
