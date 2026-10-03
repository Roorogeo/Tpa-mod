package com.roorogeo.essentials.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.service.TabListService;
import com.roorogeo.essentials.service.VanishService;

/**
 * Two small hooks on {@link ServerPlayer} that Fabric API has no events for.
 *
 * <ul>
 *     <li>{@code broadcastToPlayer} is what the entity tracker asks before showing this player's
 *     entity to another player. Returning false there is the only way to hide a player's body from
 *     specific clients while keeping vanilla tracking (and all other mods) intact. Used by /vanish.</li>
 *     <li>{@code getTabListDisplayName} is the name vanilla sends for the tab list (null means "use the
 *     account name"). Overriding it shows nicknames and the AFK tag in the tab list for vanilla clients.</li>
 * </ul>
 */
@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin {
	@Inject(method = "broadcastToPlayer", at = @At("HEAD"), cancellable = true)
	private void essentials$hideVanished(ServerPlayer viewer, CallbackInfoReturnable<Boolean> cir) {
		if (VanishService.isHiddenFrom((ServerPlayer) (Object) this, viewer)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
	private void essentials$tabListName(CallbackInfoReturnable<Component> cir) {
		Component name = TabListService.displayName((ServerPlayer) (Object) this);

		if (name != null) {
			cir.setReturnValue(name);
		}
	}
}
