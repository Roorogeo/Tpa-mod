package com.roorogeo.essentials.command.chat;

import static net.minecraft.commands.Commands.argument;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /ignore [player]}: toggles ignoring a player (chat, /msg, /mail, /tpa). Without a name it
 * lists who you ignore.
 */
public final class IgnoreCommand extends EssentialsCommand {
	public IgnoreCommand() {
		super("ignore", PermissionNodes.IGNORE);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			PlayerData data = PlayerDataStore.get(player(ctx.getSource()));

			if (data.ignored.isEmpty()) {
				send(ctx.getSource(), "ignore.list-empty");
				return 1;
			}

			List<String> names = new ArrayList<>();

			for (UUID id : data.ignored) {
				PlayerData other = PlayerDataStore.get(id);
				names.add(other == null ? id.toString() : other.name);
			}

			send(ctx.getSource(), "ignore.list", "players", String.join(", ", names));
			return 1;
		})).then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.KNOWN)
				.executes(this.run(ctx -> {
					ServerPlayer self = player(ctx.getSource());
					PlayerData target = knownPlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));

					if (target.uuid.equals(self.getUUID())) {
						throw error("ignore.self");
					}

					PlayerData data = PlayerDataStore.get(self);

					if (data.ignored.remove(target.uuid)) {
						data.markDirty();
						send(ctx.getSource(), "ignore.removed", "player", target.name);
						return 1;
					}

					ServerPlayer online = online(ctx.getSource(), target);

					if (online != null && Perms.check(online, PermissionNodes.IGNORE_EXEMPT)) {
						throw error("ignore.exempt", "player", target.name);
					}

					data.ignored.add(target.uuid);
					data.markDirty();
					send(ctx.getSource(), "ignore.added", "player", target.name);
					return 1;
				})));
	}
}
