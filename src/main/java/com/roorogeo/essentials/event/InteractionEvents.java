package com.roorogeo.essentials.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.service.FreezeService;
import com.roorogeo.essentials.service.JailService;
import com.roorogeo.essentials.text.Messages;

/**
 * Stops frozen and jailed players from breaking, placing, using and attacking
 * ({@code freeze.prevent-interaction}, {@code jail.prevent-interaction}).
 */
public final class InteractionEvents {
	private InteractionEvents() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> check(player));
		UseItemCallback.EVENT.register((player, level, hand) -> check(player));
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> check(player));
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> check(player));
		AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> check(player));
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> check(player) == InteractionResult.PASS);
	}

	private static InteractionResult check(Player player) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		if (ConfigManager.config().freeze.preventInteraction && FreezeService.isFrozen(serverPlayer)) {
			Messages.actionBar(serverPlayer, "freeze.no-interact");
			return InteractionResult.FAIL;
		}

		if (ConfigManager.config().jail.preventInteraction && JailService.isJailed(serverPlayer)) {
			Messages.actionBar(serverPlayer, "jail.no-interact");
			return InteractionResult.FAIL;
		}

		return InteractionResult.PASS;
	}
}
