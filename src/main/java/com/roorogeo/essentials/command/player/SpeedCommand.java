package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /speed <0-10> [fly|walk] [player]}. 1 is the vanilla speed, 10 is
 * {@code player.max-fly-speed} / {@code player.max-walk-speed}, values below 1 are slower.
 * Without a type, the fly speed changes while flying and the walk speed otherwise.
 */
public final class SpeedCommand extends EssentialsCommand {
	private static final float DEFAULT_FLY = 0.05f;
	private static final float DEFAULT_WALK = 0.1f;

	public SpeedCommand() {
		super("speed", PermissionNodes.SPEED);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("speed", FloatArgumentType.floatArg(0, 10))
				.executes(this.run(ctx -> {
					ServerPlayer player = player(ctx.getSource());
					return speed(ctx, player, player.getAbilities().flying);
				}))
				.then(literal("fly")
						.requires(requires(PermissionNodes.SPEED_FLY))
						.executes(this.run(ctx -> speed(ctx, player(ctx.getSource()), true)))
						.then(argument("player", StringArgumentType.word())
								.requires(requires(PermissionNodes.SPEED_OTHERS))
								.suggests(PlayerLookup.ONLINE)
								.executes(this.run(ctx -> speed(ctx, onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player")), true)))))
				.then(literal("walk")
						.requires(requires(PermissionNodes.SPEED_WALK))
						.executes(this.run(ctx -> speed(ctx, player(ctx.getSource()), false)))
						.then(argument("player", StringArgumentType.word())
								.requires(requires(PermissionNodes.SPEED_OTHERS))
								.suggests(PlayerLookup.ONLINE)
								.executes(this.run(ctx -> speed(ctx, onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player")), false))))));
	}

	private static int speed(CommandContext<CommandSourceStack> ctx, ServerPlayer target, boolean fly) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();

		if (!has(source, fly ? PermissionNodes.SPEED_FLY : PermissionNodes.SPEED_WALK)) {
			throw error("general.no-permission");
		}

		float speed = FloatArgumentType.getFloat(ctx, "speed");
		float max = fly ? ConfigManager.config().player.maxFlySpeed : ConfigManager.config().player.maxWalkSpeed;
		float base = fly ? DEFAULT_FLY : DEFAULT_WALK;
		float value = speed < 1 ? base * speed : base + (max - base) * (speed - 1) / 9f;

		if (fly) {
			target.getAbilities().setFlyingSpeed(value);
			target.onUpdateAbilities();
		} else {
			AttributeInstance attribute = target.getAttribute(Attributes.MOVEMENT_SPEED);

			if (attribute != null) {
				attribute.setBaseValue(value);
			}
		}

		String type = Messages.plain(fly ? "speed.type-fly" : "speed.type-walk");
		Messages.send(target, "speed.set", "type", type, "speed", speed);

		if (source.getEntity() != target) {
			send(source, "speed.set-other", "player", DisplayNames.of(target), "type", type, "speed", speed);
		}

		return 1;
	}
}
