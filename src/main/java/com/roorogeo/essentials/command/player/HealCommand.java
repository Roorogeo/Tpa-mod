package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /heal [player]}: full health and hunger, removes fire and harmful effects (configurable).
 */
public final class HealCommand extends EssentialsCommand {
	public HealCommand() {
		super("heal", PermissionNodes.HEAL);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> heal(ctx.getSource(), player(ctx.getSource()))))
				.then(argument("player", StringArgumentType.word())
						.requires(requires(PermissionNodes.HEAL_OTHERS))
						.suggests(PlayerLookup.ONLINE)
						.executes(this.run(ctx -> heal(ctx.getSource(), onlinePlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"))))));
	}

	private static int heal(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
		if (!target.isAlive()) {
			throw error("heal.dead");
		}

		EssentialsConfig.Player config = ConfigManager.config().player;
		target.setHealth(target.getMaxHealth());
		target.getFoodData().setFoodLevel(20);
		target.getFoodData().setSaturation(20.0f);

		if (config.healExtinguishes) {
			target.clearFire();
		}

		if (config.healRemovesNegativeEffects) {
			List<Holder<MobEffect>> harmful = new ArrayList<>();

			for (MobEffectInstance effect : target.getActiveEffects()) {
				if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
					harmful.add(effect.getEffect());
				}
			}

			harmful.forEach(target::removeEffect);
		}

		Messages.send(target, "heal.healed");

		if (source.getEntity() != target) {
			send(source, "heal.healed-other", "player", DisplayNames.of(target));
		}

		return 1;
	}
}
