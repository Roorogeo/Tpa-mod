package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.VanishService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;

/**
 * {@code /near [radius]}: lists players near you, closest first.
 */
public final class NearCommand extends EssentialsCommand {
	public NearCommand() {
		super("near", PermissionNodes.NEAR);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> near(ctx.getSource(), ConfigManager.config().near.defaultRadius)))
				.then(argument("radius", IntegerArgumentType.integer(1))
						.requires(requires(PermissionNodes.NEAR_RADIUS))
						.executes(this.run(ctx -> near(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius")))));
	}

	private static int near(CommandSourceStack source, int radius) throws CommandSyntaxException {
		int max = ConfigManager.config().near.maxRadius;

		if (radius > max) {
			throw error("near.radius-too-large", "max", max);
		}

		ServerPlayer self = player(source);
		double radiusSq = (double) radius * radius;
		List<ServerPlayer> nearby = new ArrayList<>();

		for (ServerPlayer other : self.level().players()) {
			if (other != self && VanishService.canSee(self, other) && other.distanceToSqr(self) <= radiusSq) {
				nearby.add(other);
			}
		}

		if (nearby.isEmpty()) {
			send(source, "near.none", "radius", radius);
			return 0;
		}

		nearby.sort(Comparator.comparingDouble(other -> other.distanceToSqr(self)));
		MutableComponent message = Messages.get("near.header", "radius", radius);

		for (int i = 0; i < nearby.size(); i++) {
			if (i > 0) {
				message.append(Messages.get("near.separator"));
			}

			ServerPlayer other = nearby.get(i);
			message.append(Messages.get("near.entry", "displayname", DisplayNames.of(other), "player", DisplayNames.realName(other),
					"distance", Math.round(Math.sqrt(other.distanceToSqr(self)))));
		}

		source.sendSystemMessage(message);
		return nearby.size();
	}
}
