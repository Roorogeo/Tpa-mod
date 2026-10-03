package com.roorogeo.essentials.command.chat;

import static net.minecraft.commands.Commands.argument;

import java.util.Locale;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.service.TabListService;
import com.roorogeo.essentials.text.DisplayNames;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.text.TextFormatter;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /nick <nickname|off>} or {@code /nick <player> <nickname|off>}. Colors need {@code essentials.nick.color}, styles
 * {@code essentials.nick.format}, {@code &k} needs {@code essentials.nick.magic}.
 */
public final class NickCommand extends EssentialsCommand {
	public NickCommand() {
		super("nick", PermissionNodes.NICK);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		// Greedy so nicknames can contain & codes; "/nick <player> <nickname>" is detected by the space.
		root.then(argument("nickname", StringArgumentType.greedyString())
				.suggests((ctx, builder) -> has(ctx.getSource(), PermissionNodes.NICK_OTHERS)
						? PlayerLookup.ONLINE.getSuggestions(ctx, builder)
						: builder.buildFuture())
				.executes(this.run(ctx -> {
					CommandSourceStack source = ctx.getSource();
					String input = StringArgumentType.getString(ctx, "nickname").trim();
					int space = input.indexOf(' ');

					if (space > 0) {
						if (!has(source, PermissionNodes.NICK_OTHERS)) {
							throw error("nick.invalid");
						}

						ServerPlayer target = onlinePlayer(source, input.substring(0, space));
						return nick(source, target, input.substring(space + 1).trim());
					}

					return nick(source, player(source), input);
				})));
	}

	private static int nick(CommandSourceStack source, ServerPlayer target, String nickname) throws CommandSyntaxException {
		PlayerData data = PlayerDataStore.get(target);
		boolean self = source.getEntity() == target;

		if (nickname.equalsIgnoreCase("off") || nickname.equalsIgnoreCase(DisplayNames.realName(target))) {
			data.nickname = null;
			data.markDirty();
			TabListService.refresh(target);
			send(source, self ? "nick.removed" : "nick.removed-other", "player", DisplayNames.realName(target));
			return 1;
		}

		validate(source, target, nickname);
		data.nickname = nickname;
		data.markDirty();
		TabListService.refresh(target);

		if (self) {
			send(source, "nick.set", "nick", DisplayNames.nickname(nickname));
		} else {
			send(source, "nick.set-other", "player", DisplayNames.realName(target), "nick", DisplayNames.nickname(nickname));
			Messages.send(target, "nick.changed-by", "nick", DisplayNames.nickname(nickname), "sender", source.getDisplayName());
		}

		return 1;
	}

	private static void validate(CommandSourceStack source, ServerPlayer target, String nickname) throws CommandSyntaxException {
		EssentialsConfig.Nick config = ConfigManager.config().nick;
		TextFormatter.Allowed allowed = new TextFormatter.Allowed(
				Perms.check(source, PermissionNodes.NICK_COLOR),
				Perms.check(source, PermissionNodes.NICK_FORMAT),
				Perms.check(source, PermissionNodes.NICK_MAGIC));

		if (TextFormatter.containsDisallowed(nickname, allowed)) {
			throw error("nick.invalid");
		}

		String plain = TextFormatter.strip(nickname);

		if (plain.length() > config.maxLength) {
			throw error("nick.too-long", "max", config.maxLength);
		}

		if (plain.length() < config.minLength) {
			throw error("nick.too-short", "min", config.minLength);
		}

		if (!plain.matches(config.allowedPattern)) {
			throw error("nick.invalid");
		}

		if (config.allowDuplicates) {
			return;
		}

		String lower = plain.toLowerCase(Locale.ROOT);

		for (PlayerData other : PlayerDataStore.all()) {
			if (other.uuid.equals(target.getUUID())) {
				continue;
			}

			boolean sameNick = other.nickname != null && TextFormatter.strip(other.nickname).toLowerCase(Locale.ROOT).equals(lower);

			if (sameNick || other.name.toLowerCase(Locale.ROOT).equals(lower)) {
				throw error("nick.taken", "nick", plain);
			}
		}
	}
}
