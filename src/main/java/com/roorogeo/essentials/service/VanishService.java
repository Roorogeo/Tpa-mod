package com.roorogeo.essentials.service;

import java.util.List;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.mixin.ChunkMapAccessor;
import com.roorogeo.essentials.mixin.TrackedEntityInvoker;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;

/**
 * /vanish: hides players from everyone without {@code essentials.vanish.see}, in the world (through
 * the entity tracker), the tab list, /list, /near, tab completion and private messages.
 */
public final class VanishService {
	private static int tickCounter;

	private VanishService() {
	}

	public static boolean isVanished(ServerPlayer player) {
		PlayerData data = PlayerDataStore.get(player.getUUID());
		return data != null && data.vanished;
	}

	/** True if {@code target} is vanished and {@code viewer} may not see it. */
	public static boolean isHiddenFrom(ServerPlayer target, ServerPlayer viewer) {
		return target != viewer && isVanished(target) && !Perms.check(viewer, PermissionNodes.VANISH_SEE);
	}

	public static boolean canSee(ServerPlayer viewer, ServerPlayer target) {
		return !isHiddenFrom(target, viewer);
	}

	public static boolean canSee(CommandSourceStack source, ServerPlayer target) {
		if (!isVanished(target) || source.getEntity() == target) {
			return true;
		}

		return Perms.check(source, PermissionNodes.VANISH_SEE);
	}

	public static void setVanished(ServerPlayer player, boolean vanished) {
		PlayerData data = PlayerDataStore.get(player);

		if (data.vanished == vanished) {
			return;
		}

		data.vanished = vanished;
		data.markDirty();
		MinecraftServer server = player.level().getServer();

		if (ConfigManager.config().vanish.fakeMessages) {
			String key = vanished ? "join-quit.quit" : "join-quit.join";

			for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
				if (viewer != player && !Perms.check(viewer, PermissionNodes.VANISH_SEE)) {
					Messages.send(viewer, key, "displayname", DisplayNames.of(player), "player", DisplayNames.realName(player));
				}
			}
		}

		refresh(player);

		if (!vanished) {
			player.sendOverlayMessage(net.minecraft.network.chat.Component.empty());
		}
	}

	/** Re-sends tab list entries and entity visibility of {@code target} to every other player. */
	public static void refresh(ServerPlayer target) {
		MinecraftServer server = target.level().getServer();

		for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
			if (viewer != target) {
				refreshFor(target, viewer);
			}
		}
	}

	private static void refreshFor(ServerPlayer target, ServerPlayer viewer) {
		boolean visible = canSee(viewer, target);

		if (ConfigManager.config().vanish.hideFromTabList) {
			if (visible) {
				viewer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(target)));
			} else {
				viewer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(target.getUUID())));
			}
		}

		if (viewer.level() == target.level()) {
			ServerLevel level = target.level();
			Object tracked = ((ChunkMapAccessor) level.getChunkSource().chunkMap).essentials$getEntityMap().get(target.getId());

			if (tracked != null) {
				((TrackedEntityInvoker) tracked).essentials$updatePlayer(viewer);
			}
		}
	}

	/**
	 * After a player joins: hides them if they are still vanished, and hides already-vanished
	 * players from them. Vanilla has just sent everyone's tab entries, so this corrects them.
	 */
	public static void onJoin(ServerPlayer joined) {
		if (ConfigManager.config().vanish.persist) {
			if (isVanished(joined)) {
				refresh(joined);
			}
		} else {
			PlayerData data = PlayerDataStore.get(joined);

			if (data.vanished) {
				data.vanished = false;
				data.markDirty();
			}
		}

		if (!ConfigManager.config().vanish.hideFromTabList) {
			return;
		}

		for (ServerPlayer other : joined.level().getServer().getPlayerList().getPlayers()) {
			if (other != joined && isHiddenFrom(other, joined)) {
				joined.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(other.getUUID())));
			}
		}
	}

	/** Shows the "you are vanished" reminder. */
	public static void tick(MinecraftServer server) {
		if (++tickCounter < 40) {
			return;
		}

		tickCounter = 0;

		if (!ConfigManager.config().vanish.actionBar) {
			return;
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (isVanished(player)) {
				Messages.actionBar(player, "vanish.actionbar");
			}
		}
	}
}
