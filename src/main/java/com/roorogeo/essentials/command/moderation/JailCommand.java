package com.roorogeo.essentials.command.moderation;

import static net.minecraft.commands.Commands.argument;

import java.util.Locale;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.data.NamedLocations;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.service.JailService;
import com.roorogeo.essentials.util.Durations;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /jail <player> <jail> [duration] [reason]}: works on offline players too (they are jailed on
 * their next join). Without a duration the sentence lasts until /unjail.
 */
public final class JailCommand extends EssentialsCommand {
	public JailCommand() {
		super("jail", PermissionNodes.JAIL);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(argument("player", StringArgumentType.word())
				.suggests(PlayerLookup.KNOWN)
				.then(argument("jail", StringArgumentType.word())
						.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(NamedLocations.JAILS.all().keySet(), builder))
						.executes(this.run(ctx -> jail(ctx, null)))
						.then(argument("duration and reason", StringArgumentType.greedyString())
								.executes(this.run(ctx -> jail(ctx, StringArgumentType.getString(ctx, "duration and reason")))))));
	}

	private static int jail(CommandContext<CommandSourceStack> ctx, @Nullable String args) throws CommandSyntaxException {
		CommandSourceStack source = ctx.getSource();
		PlayerData data = knownPlayer(source, StringArgumentType.getString(ctx, "player"));
		String jail = StringArgumentType.getString(ctx, "jail").toLowerCase(Locale.ROOT);

		if (NamedLocations.JAILS.get(jail) == null) {
			throw error("jail.not-found", "jail", jail);
		}

		ServerPlayer online = online(source, data);

		if (online != null && Perms.check(online, PermissionNodes.JAIL_EXEMPT)) {
			throw error("jail.exempt", "player", data.name);
		}

		Sentence sentence = Sentence.parse(args, Durations.PERMANENT);
		String reason = reason(sentence.reason());
		JailService.jail(source.getServer(), data, jail, sentence.millis(), reason);
		send(source, "jail.jailed", "player", data.name, "jail", jail, "time", Durations.format(sentence.millis()), "reason", reason);
		return 1;
	}
}
