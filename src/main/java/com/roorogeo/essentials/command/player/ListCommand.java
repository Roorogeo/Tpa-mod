package com.roorogeo.essentials.command.player;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.AfkService;
import com.roorogeo.essentials.service.VanishService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;

/**
 * {@code /list}: online players with nicknames and AFK tags. Vanished players are only shown (with a
 * tag) to those who can see them. Replaces vanilla /list.
 */
public final class ListCommand extends EssentialsCommand {
	public ListCommand() {
		super("list", PermissionNodes.LIST);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			CommandSourceStack source = ctx.getSource();
			List<ServerPlayer> visible = new ArrayList<>();

			for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
				if (VanishService.canSee(source, player)) {
					visible.add(player);
				}
			}

			send(source, "list.header", "count", visible.size(), "max", source.getServer().getPlayerList().getMaxPlayers());
			MutableComponent players = Component.empty();

			for (int i = 0; i < visible.size(); i++) {
				ServerPlayer player = visible.get(i);

				if (i > 0) {
					players.append(Messages.get("list.separator"));
				}

				players.append(Messages.get("list.entry",
						"displayname", DisplayNames.of(player),
						"player", DisplayNames.realName(player),
						"afk", AfkService.isAfk(player) ? Messages.get("list.afk-tag") : Component.empty(),
						"vanished", VanishService.isVanished(player) ? Messages.get("list.vanished-tag") : Component.empty()));
			}

			if (!visible.isEmpty()) {
				send(source, "list.players", "players", players);
			}

			return visible.size();
		}));
	}
}
