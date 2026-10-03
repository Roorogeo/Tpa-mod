package com.roorogeo.essentials.command.chat;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.command.EssentialsCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.service.ChatService;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.Durations;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * {@code /mail [read [page] | send <player> <message> | sendall <message> | clear]}.
 * Mail can be sent to offline players and is delivered when they join.
 */
public final class MailCommand extends EssentialsCommand {
	private static final Map<UUID, Long> LAST_SENT = new HashMap<>();

	public MailCommand() {
		super("mail", PermissionNodes.MAIL);
	}

	@Override
	protected void build(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.executes(this.run(ctx -> read(ctx.getSource(), 1)))
				.then(literal("read")
						.requires(requires(PermissionNodes.MAIL_READ))
						.executes(this.run(ctx -> read(ctx.getSource(), 1)))
						.then(argument("page", IntegerArgumentType.integer(1))
								.executes(this.run(ctx -> read(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page"))))))
				.then(literal("send")
						.requires(requires(PermissionNodes.MAIL_SEND))
						.then(argument("player", StringArgumentType.word())
								.suggests(PlayerLookup.KNOWN)
								.then(argument("message", StringArgumentType.greedyString())
										.executes(this.run(ctx -> send(ctx.getSource(), StringArgumentType.getString(ctx, "player"), StringArgumentType.getString(ctx, "message")))))))
				.then(literal("sendall")
						.requires(requires(PermissionNodes.MAIL_SENDALL))
						.then(argument("message", StringArgumentType.greedyString())
								.executes(this.run(ctx -> sendAll(ctx.getSource(), StringArgumentType.getString(ctx, "message"))))))
				.then(literal("clear")
						.requires(requires(PermissionNodes.MAIL_CLEAR))
						.executes(this.run(ctx -> {
							PlayerData data = PlayerDataStore.get(player(ctx.getSource()));
							data.mail.clear();
							data.markDirty();
							send(ctx.getSource(), "mail.cleared");
							return 1;
						})));
	}

	private static int read(CommandSourceStack source, int page) throws CommandSyntaxException {
		if (!has(source, PermissionNodes.MAIL_READ)) {
			throw error("general.no-permission");
		}

		PlayerData data = PlayerDataStore.get(player(source));
		List<PlayerData.Mail> mail = data.mail;

		if (mail.isEmpty()) {
			send(source, "mail.none");
			return 1;
		}

		int pageSize = Math.max(1, ConfigManager.config().mail.pageSize);
		int pages = (mail.size() + pageSize - 1) / pageSize;
		int current = Math.min(page, pages);
		SimpleDateFormat format = new SimpleDateFormat(ConfigManager.message("time.date-format"));
		send(source, "mail.header", "page", current, "pages", pages);

		for (int i = (current - 1) * pageSize; i < Math.min(mail.size(), current * pageSize); i++) {
			PlayerData.Mail entry = mail.get(i);
			send(source, "mail.entry", "time", format.format(new Date(entry.time)), "sender", entry.sender, "message", entry.message);
			entry.read = true;
		}

		data.markDirty();
		return 1;
	}

	private static void checkSendCooldown(CommandSourceStack source) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayer();
		int cooldown = ConfigManager.config().mail.sendCooldownSeconds;

		if (player == null || cooldown <= 0 || Perms.check(player, PermissionNodes.COMMAND_COOLDOWN_BYPASS)) {
			return;
		}

		long remaining = LAST_SENT.getOrDefault(player.getUUID(), 0L) + cooldown * 1000L - System.currentTimeMillis();

		if (remaining > 0) {
			throw error("general.command-cooldown", "command", "mail send", "time", Durations.format(remaining));
		}

		LAST_SENT.put(player.getUUID(), System.currentTimeMillis());
	}

	private static int send(CommandSourceStack source, String name, String message) throws CommandSyntaxException {
		EssentialsConfig.Mail config = ConfigManager.config().mail;

		if (message.length() > config.maxLength) {
			throw error("mail.too-long", "max", config.maxLength);
		}

		PlayerData target = knownPlayer(source, name);

		if (target.mail.size() >= config.maxMailsPerPlayer) {
			throw error("mail.inbox-full", "player", target.name);
		}

		checkSendCooldown(source);
		ServerPlayer sender = source.getPlayer();
		ServerPlayer online = online(source, target);
		boolean ignored = sender != null && online != null && ChatService.ignores(online, sender)
				|| sender != null && online == null && target.ignored.contains(sender.getUUID());

		if (!ignored) {
			deliver(source, target, message);
		}

		send(source, "mail.sent", "player", target.name);
		ChatService.spy(source.getServer(), "mail.spy", sender == null ? new UUID(0, 0) : sender.getUUID(), target.uuid,
				"sender", source.getTextName(), "receiver", target.name, "message", message);
		return 1;
	}

	private static void deliver(CommandSourceStack source, PlayerData target, String message) {
		ServerPlayer sender = source.getPlayer();
		target.mail.add(new PlayerData.Mail(source.getTextName(), sender == null ? null : sender.getUUID(), System.currentTimeMillis(), message));
		target.markDirty();
		ServerPlayer online = online(source, target);

		if (online != null) {
			Messages.send(online, "mail.received", "sender", Component.literal(source.getTextName()));
		}
	}

	private static int sendAll(CommandSourceStack source, String message) throws CommandSyntaxException {
		EssentialsConfig.Mail config = ConfigManager.config().mail;

		if (message.length() > config.maxLength) {
			throw error("mail.too-long", "max", config.maxLength);
		}

		int count = 0;

		for (PlayerData data : PlayerDataStore.all()) {
			if (data.mail.size() < config.maxMailsPerPlayer) {
				deliver(source, data, message);
				count++;
			}
		}

		send(source, "mail.sent-all", "count", count);
		return Math.max(1, count);
	}
}
