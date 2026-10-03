package com.roorogeo.essentials.command.moderation;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.Durations;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /mute <player> [duration] [reason]}: blocks chat and the commands in {@code mute.blocked-commands}.
 * Without a duration, {@code mute.default-duration} is used. Muting a muted player updates the mute.
 */
public final class MuteCommand extends EssentialsCommand {
	public MuteCommand() {
		super("mute", PermissionNodes.MUTE);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.KNOWN)
				.executes(this.run(ctx -> mute(ctx.getSource(), StringArgumentType.getString(ctx, "player"), null)))
				.then(argument("duration and reason", StringArgumentType.greedyString())
						.executes(this.run(ctx -> mute(ctx.getSource(), StringArgumentType.getString(ctx, "player"),
								StringArgumentType.getString(ctx, "duration and reason"))))));
	}

	private static int mute(CommandSourceStack source, String name, @Nullable String args) throws CommandSyntaxException {
		PlayerData data = knownPlayer(source, name);
		ServerPlayer online = online(source, data);

		if (online != null && Perms.check(online, PermissionNodes.MUTE_EXEMPT)) {
			throw error("mute.exempt", "player", data.name);
		}

		String defaultText = ConfigManager.config().mute.defaultDuration;
		Long defaultMillis = Durations.parse(defaultText);
		Sentence sentence = Sentence.parse(args, defaultMillis == null ? Durations.PERMANENT : defaultMillis);
		data.mutedUntil = sentence.millis() == Durations.PERMANENT ? -1 : System.currentTimeMillis() + sentence.millis();
		data.muteReason = sentence.reason();
		data.markDirty();
		String time = Durations.format(sentence.millis());
		String reason = reason(sentence.reason());
		send(source, "mute.muted", "player", data.name, "time", time, "reason", reason);

		if (online != null) {
			Messages.send(online, "mute.notify", "time", time, "reason", reason);
		}

		return 1;
	}
}
