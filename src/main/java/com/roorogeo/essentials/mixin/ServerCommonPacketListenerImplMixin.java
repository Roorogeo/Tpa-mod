package com.roorogeo.essentials.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import com.roorogeo.essentials.combat.CombatService;
import com.roorogeo.essentials.config.ConfigManager;

/**
 * Tells combat logging apart from kicks.
 *
 * <p>Every disconnect the server starts (/kick, bans, /tempban, server shutdown, anti-cheat kicks
 * from other mods) goes through {@code disconnect(DisconnectionDetails)}, while a player who closes
 * the game never reaches it. Fabric's disconnect event doesn't say which of the two happened, so
 * this records server-initiated disconnects before the event fires. Keep-alive timeouts also come
 * through here; they still count as combat logging unless {@code combat.punish-timeouts} is false.
 */
@Mixin(ServerCommonPacketListenerImpl.class)
abstract class ServerCommonPacketListenerImplMixin {
	@Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("HEAD"))
	private void essentials$recordServerDisconnect(DisconnectionDetails details, CallbackInfo ci) {
		if (!((Object) this instanceof ServerGamePacketListenerImpl game)) {
			return;
		}

		boolean timeout = details.reason().getContents() instanceof TranslatableContents translatable
				&& translatable.getKey().equals("disconnect.timeout");

		if (!timeout || !ConfigManager.config().combat.punishTimeouts) {
			CombatService.markServerDisconnect(game.getPlayer().getUUID());
		}
	}
}
