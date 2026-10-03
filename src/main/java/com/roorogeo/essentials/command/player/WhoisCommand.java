package com.roorogeo.essentials.command.player;

import static net.minecraft.commands.Commands.argument;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.combat.CombatService;
import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.Location;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.service.AfkService;
import com.roorogeo.essentials.service.EconomyService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /whois <player>}: everything Essentials knows about a player (layout: {@code whois.body}).
 */
public final class WhoisCommand extends EssentialsCommand {
	public WhoisCommand() {
		super("whois", PermissionNodes.WHOIS);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.KNOWN)
				.executes(this.run(ctx -> {
					CommandSourceStack source = ctx.getSource();
					PlayerData data = knownPlayer(source, StringArgumentType.getString(ctx, "player"));
					ServerPlayer player = online(source, data);
					String unknown = "-";
					String yes = Messages.plain("whois.yes");
					String no = Messages.plain("whois.no");
					Location location = player != null ? Location.of(player) : data.logoutLocation;
					String date = new SimpleDateFormat(ConfigManager.message("time.date-format")).format(new Date(data.firstJoin));

					send(source, "whois.body",
							"player", data.name,
							"uuid", data.uuid.toString(),
							"nick", data.nickname == null ? Component.literal(unknown) : DisplayNames.nickname(data.nickname),
							"health", player != null ? String.format("%.1f", player.getHealth()) : unknown,
							"max-health", player != null ? String.format("%.1f", player.getMaxHealth()) : unknown,
							"food", player != null ? player.getFoodData().getFoodLevel() : unknown,
							"gamemode", player != null ? Messages.plain("gamemode.name." + player.gameMode().getName()) : unknown,
							"fly", player != null ? (player.getAbilities().mayfly ? yes : no) : unknown,
							"god", data.god ? yes : no,
							"location", location == null ? unknown : location.describe(),
							"balance", EconomyService.format(data.balance),
							"afk", player != null && AfkService.isAfk(player) ? yes : no,
							"vanished", data.vanished ? yes : no,
							"muted", data.isMuted() ? yes : no,
							"jailed", data.jail != null ? yes : no,
							"frozen", data.frozen ? yes : no,
							"combat", player != null && CombatService.isTagged(player) ? yes : no,
							"first-join", date,
							"ip", has(source, PermissionNodes.WHOIS_IP) ? data.lastIp : Messages.plain("whois.hidden"));
					return 1;
				})));
	}
}
