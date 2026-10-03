package com.roorogeo.essentials.command.teleport;

import static net.minecraft.commands.Commands.argument;

import java.util.Map;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.HomeService;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /homes [player]}: lists homes as clickable entries and shows used/max.
 */
public final class HomesCommand extends EssentialsCommand {
	public HomesCommand() {
		super("homes", PermissionNodes.HOMES);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> {
			ServerPlayer player = player(ctx.getSource());
			return list(ctx.getSource(), PlayerDataStore.get(player), false);
		})).then(argument("player", StringArgumentType.word())
				.requires(requires(PermissionNodes.HOMES_OTHERS))
				.suggests(PlayerLookup.KNOWN)
				.executes(this.run(ctx -> {
					PlayerData data = knownPlayer(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
					ServerPlayer self = ctx.getSource().getPlayer();
					return list(ctx.getSource(), data, self == null || !self.getUUID().equals(data.uuid));
				})));
	}

	/** Sends the home list of {@code data}. Used by /homes and by /home when it is ambiguous. */
	static int list(CommandSourceStack source, PlayerData data, boolean other) throws CommandSyntaxException {
		ServerPlayer owner = source.getServer().getPlayerList().getPlayer(data.uuid);
		String max = owner != null ? HomeService.describe(HomeService.maxHomes(owner)) : "?";
		Map<String, Location> homes = data.homes;

		if (homes.isEmpty()) {
			if (other) {
				send(source, "homes.none-other", "player", data.name);
			} else {
				send(source, "homes.none", "max", max);
			}

			return 1;
		}

		MutableComponent message = other
				? Messages.get("homes.header-other", "player", data.name, "count", homes.size(), "max", max)
				: Messages.get("homes.header", "count", homes.size(), "max", max);
		boolean first = true;

		for (Map.Entry<String, Location> entry : homes.entrySet()) {
			if (!first) {
				message.append(Messages.get("homes.separator"));
			}

			first = false;
			String command = other ? "/home " + data.name + ":" + entry.getKey() : "/home " + entry.getKey();
			message.append(Messages.button("homes.entry", "homes.entry-hover", command, "home", entry.getKey(), "location", entry.getValue().describe()));
		}

		source.sendSystemMessage(message);
		return homes.size();
	}
}
