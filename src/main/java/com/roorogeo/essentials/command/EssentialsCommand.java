package com.roorogeo.essentials.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.combat.CombatService;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;
import com.roorogeo.essentials.data.PlayerData;
import com.roorogeo.essentials.data.PlayerDataStore;
import com.roorogeo.essentials.perm.PermissionNodes;
import com.roorogeo.essentials.perm.Perms;
import com.roorogeo.essentials.service.ChatService;
import com.roorogeo.essentials.service.FreezeService;
import com.roorogeo.essentials.service.JailService;
import com.roorogeo.essentials.text.Messages;
import com.roorogeo.essentials.util.Durations;
import com.roorogeo.essentials.util.PlayerLookup;

/**
 * Base class of every Essentials command.
 *
 * <p>It owns the parts every command shares:
 * <ul>
 *     <li><b>Permissions</b>: the root literal (and each alias) requires the command's node, so
 *     players without it never see or tab-complete the command; sub-features add their own
 *     {@link #requires(PermissionNodes.Node)} on their argument nodes.</li>
 *     <li><b>Blocking</b>: combat tag ({@code combat.blocked-commands}), mute
 *     ({@code mute.blocked-commands}), jail ({@code jail.allowed-commands}) and freeze
 *     ({@code freeze.allowed-commands}) are checked before running.</li>
 *     <li><b>Cooldowns</b>: {@code commands.<name>.cooldown-seconds}, bypassed with
 *     {@code essentials.command.cooldown.bypass}.</li>
 *     <li><b>Messages</b>: helpers that read from messages.json.</li>
 * </ul>
 * Subclasses implement {@link #build(LiteralArgumentBuilder)} and wrap their executors with
 * {@link #run(Action)}.
 */
public abstract class EssentialsCommand {
	private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

	private final String name;
	private final PermissionNodes.Node permission;

	protected EssentialsCommand(String name, PermissionNodes.Node permission) {
		this.name = name;
		this.permission = permission;
	}

	/** An executor body. Throw {@link #error(String, Object...)} to fail with a message. */
	@FunctionalInterface
	protected interface Action {
		int run(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
	}

	/** Primary name, also the key in {@code commands.<name>}. */
	public final String name() {
		return this.name;
	}

	public final PermissionNodes.Node permission() {
		return this.permission;
	}

	/** Adds arguments and executors to the root literal (called once per name and alias). */
	protected abstract void build(LiteralArgumentBuilder<CommandSourceStack> root);

	/** Builds the root node for one label (the primary name or an alias). */
	public final LiteralArgumentBuilder<CommandSourceStack> create(String label) {
		LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(label).requires(requires(this.permission));
		this.build(root);
		return root;
	}

	/** All labels from config: the name plus its aliases. */
	public final List<String> labels(EssentialsConfig.CommandSettings settings) {
		List<String> labels = new ArrayList<>();
		labels.add(this.name);

		for (String alias : settings.aliases) {
			String clean = alias.trim().toLowerCase(Locale.ROOT);

			if (!clean.isEmpty() && !labels.contains(clean)) {
				labels.add(clean);
			}
		}

		return labels;
	}

	// ------------------------------------------------------------------ permissions

	protected static Predicate<CommandSourceStack> requires(PermissionNodes.Node node) {
		return Perms.require(node);
	}

	protected static boolean has(CommandSourceStack source, PermissionNodes.Node node) {
		return Perms.check(source, node);
	}

	// ------------------------------------------------------------------ execution

	/** Wraps an executor with the shared checks and cooldown handling. */
	protected final Command<CommandSourceStack> run(Action action) {
		return ctx -> {
			CommandSourceStack source = ctx.getSource();
			ServerPlayer player = source.getPlayer();

			if (player != null) {
				String label = ctx.getNodes().isEmpty() ? this.name : ctx.getNodes().getFirst().getNode().getName();
				this.checkBlocked(player, label);
				this.checkCooldown(player, source);
			}

			int result = action.run(ctx);

			if (player != null && result > 0) {
				this.startCooldown(player);
			}

			return result;
		};
	}

	private boolean matches(List<String> list, String label) {
		for (String entry : list) {
			String clean = entry.trim().toLowerCase(Locale.ROOT);

			if (clean.startsWith("/")) {
				clean = clean.substring(1);
			}

			if (clean.equals(this.name) || clean.equals(label)) {
				return true;
			}
		}

		return false;
	}

	private void checkBlocked(ServerPlayer player, String label) throws CommandSyntaxException {
		EssentialsConfig config = ConfigManager.config();

		if (config.combat.enabled && CombatService.isTagged(player) && this.matches(config.combat.blockedCommands, label)
				&& !Perms.check(player, PermissionNodes.COMBAT_COMMAND_BYPASS)) {
			throw error("general.combat-blocked", "command", label, "time", Durations.format(CombatService.remainingMillis(player.getUUID())));
		}

		PlayerData data = PlayerDataStore.get(player);

		if (data.isMuted() && this.matches(config.mute.blockedCommands, label)) {
			throw error("general.muted-blocked", "command", label);
		}

		if (JailService.isJailed(player) && !this.matches(config.jail.allowedCommands, label)) {
			throw error("general.jailed-blocked", "command", label);
		}

		if (FreezeService.isFrozen(player) && config.freeze.blockCommands && !this.matches(config.freeze.allowedCommands, label)) {
			throw error("general.frozen-blocked", "command", label);
		}
	}

	private int cooldownSeconds() {
		EssentialsConfig.CommandSettings settings = ConfigManager.config().commands.get(this.name);
		return settings == null ? 0 : settings.cooldownSeconds;
	}

	private void checkCooldown(ServerPlayer player, CommandSourceStack source) throws CommandSyntaxException {
		if (this.cooldownSeconds() <= 0 || has(source, PermissionNodes.COMMAND_COOLDOWN_BYPASS)) {
			return;
		}

		Long until = COOLDOWNS.getOrDefault(player.getUUID(), Map.of()).get(this.name);
		long remaining = until == null ? 0 : until - System.currentTimeMillis();

		if (remaining > 0) {
			throw error("general.command-cooldown", "command", this.name, "time", Durations.format(remaining));
		}
	}

	private void startCooldown(ServerPlayer player) {
		int seconds = this.cooldownSeconds();

		if (seconds > 0) {
			COOLDOWNS.computeIfAbsent(player.getUUID(), k -> new HashMap<>()).put(this.name, System.currentTimeMillis() + seconds * 1000L);
		}
	}

	public static void forgetCooldowns(UUID player) {
		COOLDOWNS.remove(player);
	}

	// ------------------------------------------------------------------ helpers

	/** A failure carrying a message from messages.json; throw it from an executor. */
	protected static CommandSyntaxException error(String key, Object... placeholders) {
		return new SimpleCommandExceptionType(Messages.get(key, placeholders)).create();
	}

	protected static void send(CommandSourceStack source, String key, Object... placeholders) {
		Messages.send(source, key, placeholders);
	}

	/** The executing player, or a "players only" failure. */
	protected static ServerPlayer player(CommandSourceStack source) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayer();

		if (player == null) {
			throw error("general.player-only");
		}

		return player;
	}

	/** An online player the source can see, or a "not found" failure. */
	protected static ServerPlayer onlinePlayer(CommandSourceStack source, String name) throws CommandSyntaxException {
		ServerPlayer player = PlayerLookup.online(source, name);

		if (player == null) {
			throw error("general.player-not-found", "player", name);
		}

		return player;
	}

	/** Stored data of a player who has joined before (online or offline), or a failure. */
	protected static PlayerData knownPlayer(CommandSourceStack source, String name) throws CommandSyntaxException {
		PlayerData data = PlayerLookup.known(source.getServer(), name);

		if (data == null) {
			throw error("general.player-never-joined", "player", name);
		}

		return data;
	}

	protected static @Nullable ServerPlayer online(CommandSourceStack source, PlayerData data) {
		return source.getServer().getPlayerList().getPlayer(data.uuid);
	}

	/** The reason text or the "no reason" message. */
	protected static String reason(@Nullable String reason) {
		return ChatService.reasonOrDefault(reason);
	}
}
