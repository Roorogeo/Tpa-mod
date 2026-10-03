package com.roorogeo.essentials.command.moderation;

import static net.minecraft.commands.Commands.argument;

import java.util.Date;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanListEntry;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.Durations;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /tempban <player> <duration> [reason]}: a ban that expires. It is stored in vanilla's
 * banned-players.json, so vanilla /pardon lifts it early.
 */
public final class TempbanCommand extends EssentialsCommand {
	public TempbanCommand() {
		super("tempban", PermissionNodes.TEMPBAN);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.KNOWN)
				.then(argument("duration", StringArgumentType.word())
						.executes(this.run(ctx -> ban(ctx, "")))
						.then(argument("reason", StringArgumentType.greedyString())
								.executes(this.run(ctx -> ban(ctx, StringArgumentType.getString(ctx, "reason")))))));
	}

	private static int ban(CommandContext<CommandSourceStack> ctx, String reasonText) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		PlayerData data = knownPlayer(source, StringArgumentType.getString(ctx, "player"));
		ServerPlayer online = online(source, data);

		if (online != null && Perms.check(online, PermissionNodes.TEMPBAN_EXEMPT)) {
			throw error("tempban.exempt", "player", data.name);
		}

		String rawDuration = StringArgumentType.getString(ctx, "duration");
		Long duration = Durations.parse(rawDuration);

		if (duration == null) {
			throw error("general.invalid-duration", "input", rawDuration);
		}

		String maxText = ConfigManager.config().tempban.maxDuration;
		Long max = maxText.isBlank() ? null : Durations.parse(maxText);

		if (max != null && max != Durations.PERMANENT && (duration == Durations.PERMANENT || duration > max)) {
			throw error("tempban.too-long", "max", Durations.format(max));
		}

		String reason = reason(reasonText);
		Date expires = duration == Durations.PERMANENT ? null : new Date(System.currentTimeMillis() + duration);
		NameAndId profile = new NameAndId(data.uuid, data.name);
		source.getServer().getPlayerList().getBans().add(new UserBanListEntry(profile, new Date(), source.getTextName(), expires, reason));
		String time = Durations.format(duration);
		send(source, "tempban.banned", "player", data.name, "time", time, "reason", reason);

		if (online != null) {
			online.connection.disconnect(Messages.get("tempban.kick-message", "time", time, "reason", reason));
		}

		return 1;
	}
}
