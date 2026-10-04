package com.roorogeo.essentials.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.roorogeo.essentials.Essentials;
import com.roorogeo.essentials.command.admin.EssentialsAdminCommand;
import com.roorogeo.essentials.command.chat.BroadcastCommand;
import com.roorogeo.essentials.command.chat.IgnoreCommand;
import com.roorogeo.essentials.command.chat.MailCommand;
import com.roorogeo.essentials.command.chat.MeCommand;
import com.roorogeo.essentials.command.chat.MsgCommand;
import com.roorogeo.essentials.command.chat.NickCommand;
import com.roorogeo.essentials.command.chat.RealnameCommand;
import com.roorogeo.essentials.command.chat.ReplyCommand;
import com.roorogeo.essentials.command.chat.SocialSpyCommand;
import com.roorogeo.essentials.command.economy.BalanceCommand;
import com.roorogeo.essentials.command.economy.BaltopCommand;
import com.roorogeo.essentials.command.economy.EcoCommand;
import com.roorogeo.essentials.command.economy.PayCommand;
import com.roorogeo.essentials.command.kit.CreateKitCommand;
import com.roorogeo.essentials.command.kit.DelKitCommand;
import com.roorogeo.essentials.command.kit.KitCommand;
import com.roorogeo.essentials.command.kit.KitsCommand;
import com.roorogeo.essentials.command.moderation.DelJailCommand;
import com.roorogeo.essentials.command.moderation.FreezeCommand;
import com.roorogeo.essentials.command.moderation.JailCommand;
import com.roorogeo.essentials.command.moderation.JailsCommand;
import com.roorogeo.essentials.command.moderation.KickCommand;
import com.roorogeo.essentials.command.moderation.MuteCommand;
import com.roorogeo.essentials.command.moderation.SetJailCommand;
import com.roorogeo.essentials.command.moderation.SudoCommand;
import com.roorogeo.essentials.command.moderation.TempbanCommand;
import com.roorogeo.essentials.command.moderation.UnjailCommand;
import com.roorogeo.essentials.command.moderation.UnmuteCommand;
import com.roorogeo.essentials.command.moderation.VanishCommand;
import com.roorogeo.essentials.command.player.AfkCommand;
import com.roorogeo.essentials.command.player.AnvilCommand;
import com.roorogeo.essentials.command.player.ClearInventoryCommand;
import com.roorogeo.essentials.command.player.CombatCommand;
import com.roorogeo.essentials.command.player.EnderchestCommand;
import com.roorogeo.essentials.command.player.FeedCommand;
import com.roorogeo.essentials.command.player.FlyCommand;
import com.roorogeo.essentials.command.player.GamemodeCommand;
import com.roorogeo.essentials.command.player.GamemodeShortcutCommand;
import com.roorogeo.essentials.command.player.GodCommand;
import com.roorogeo.essentials.command.player.HatCommand;
import com.roorogeo.essentials.command.player.HealCommand;
import com.roorogeo.essentials.command.player.InvseeCommand;
import com.roorogeo.essentials.command.player.ListCommand;
import com.roorogeo.essentials.command.player.NearCommand;
import com.roorogeo.essentials.command.player.PingCommand;
import com.roorogeo.essentials.command.player.RepairCommand;
import com.roorogeo.essentials.command.player.SeenCommand;
import com.roorogeo.essentials.command.player.SpeedCommand;
import com.roorogeo.essentials.command.player.SuicideCommand;
import com.roorogeo.essentials.command.player.WhoisCommand;
import com.roorogeo.essentials.command.player.WorkbenchCommand;
import com.roorogeo.essentials.command.teleport.BackCommand;
import com.roorogeo.essentials.command.teleport.DelHomeCommand;
import com.roorogeo.essentials.command.teleport.DelWarpCommand;
import com.roorogeo.essentials.command.teleport.HomeCommand;
import com.roorogeo.essentials.command.teleport.HomesCommand;
import com.roorogeo.essentials.command.teleport.RtpCommand;
import com.roorogeo.essentials.command.teleport.SetHomeCommand;
import com.roorogeo.essentials.command.teleport.SetSpawnCommand;
import com.roorogeo.essentials.command.teleport.SetWarpCommand;
import com.roorogeo.essentials.command.teleport.SpawnCommand;
import com.roorogeo.essentials.command.teleport.TopCommand;
import com.roorogeo.essentials.command.teleport.TpAcceptCommand;
import com.roorogeo.essentials.command.teleport.TpCommand;
import com.roorogeo.essentials.command.teleport.TpDenyCommand;
import com.roorogeo.essentials.command.teleport.TpaCancelCommand;
import com.roorogeo.essentials.command.teleport.TpaCommand;
import com.roorogeo.essentials.command.teleport.TpaHereCommand;
import com.roorogeo.essentials.command.teleport.TpAllCommand;
import com.roorogeo.essentials.command.teleport.TpHereCommand;
import com.roorogeo.essentials.command.teleport.WarpCommand;
import com.roorogeo.essentials.command.teleport.WarpsCommand;
import com.roorogeo.essentials.command.world.DayCommand;
import com.roorogeo.essentials.command.world.NightCommand;
import com.roorogeo.essentials.command.world.RainCommand;
import com.roorogeo.essentials.command.world.SunCommand;
import com.roorogeo.essentials.command.world.TimeCommand;
import com.roorogeo.essentials.command.world.WeatherCommand;
import com.roorogeo.essentials.config.ConfigManager;
import com.roorogeo.essentials.config.EssentialsConfig;

import net.minecraft.world.level.GameType;

/**
 * Creates and registers every command according to {@code commands.<name>} in config.json.
 *
 * <p>When an enabled command or alias has the same name as an existing command (vanilla /tp, /msg,
 * /tell, /me, /list, /kick, /time, /weather, /gamemode, ...), the existing node is removed first so
 * the Essentials version, with its permission node, replaces it. Disable the command in config.json
 * to keep the vanilla one.
 */
public final class CommandRegistry {
	private static final List<String> REGISTERED = new ArrayList<>();

	private CommandRegistry() {
	}

	public static List<EssentialsCommand> createAll() {
		return List.of(
				// Teleport
				new SpawnCommand(), new SetSpawnCommand(), new HomeCommand(), new SetHomeCommand(), new DelHomeCommand(),
				new HomesCommand(), new WarpCommand(), new SetWarpCommand(), new DelWarpCommand(), new WarpsCommand(),
				new TpaCommand(), new TpaHereCommand(), new TpAcceptCommand(), new TpDenyCommand(), new TpaCancelCommand(),
				new TpCommand(), new TpHereCommand(), new TpAllCommand(), new BackCommand(), new TopCommand(), new RtpCommand(),
				// Chat
				new MsgCommand(), new ReplyCommand(), new MailCommand(), new IgnoreCommand(), new NickCommand(),
				new RealnameCommand(), new MeCommand(), new BroadcastCommand(), new SocialSpyCommand(),
				// Player
				new HealCommand(), new FeedCommand(), new FlyCommand(), new GodCommand(), new SpeedCommand(), new GamemodeCommand(),
				new GamemodeShortcutCommand("gmc", GameType.CREATIVE), new GamemodeShortcutCommand("gms", GameType.SURVIVAL),
				new GamemodeShortcutCommand("gma", GameType.ADVENTURE), new GamemodeShortcutCommand("gmsp", GameType.SPECTATOR),
				new AfkCommand(), new HatCommand(), new RepairCommand(), new EnderchestCommand(), new WorkbenchCommand(),
				new AnvilCommand(), new InvseeCommand(), new ClearInventoryCommand(), new SuicideCommand(), new NearCommand(),
				new SeenCommand(), new WhoisCommand(), new ListCommand(), new PingCommand(), new CombatCommand(),
				// Economy
				new BalanceCommand(), new PayCommand(), new BaltopCommand(), new EcoCommand(),
				// Kits
				new KitCommand(), new KitsCommand(), new CreateKitCommand(), new DelKitCommand(),
				// Moderation
				new MuteCommand(), new UnmuteCommand(), new TempbanCommand(), new KickCommand(), new JailCommand(),
				new JailsCommand(), new SetJailCommand(), new DelJailCommand(), new UnjailCommand(), new VanishCommand(),
				new FreezeCommand(), new SudoCommand(),
				// World
				new TimeCommand(), new DayCommand(), new NightCommand(), new WeatherCommand(), new SunCommand(), new RainCommand(),
				// Admin
				new EssentialsAdminCommand());
	}

	/** Registers all enabled commands (from Fabric's command registration event). */
	public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
		REGISTERED.clear();
		Map<String, EssentialsConfig.CommandSettings> settings = ConfigManager.config().commands;
		List<String> replaced = new ArrayList<>();
		int count = 0;

		for (EssentialsCommand command : createAll()) {
			EssentialsConfig.CommandSettings commandSettings = settings.getOrDefault(command.name(), new EssentialsConfig.CommandSettings());

			if (!commandSettings.enabled) {
				continue;
			}

			for (String label : command.labels(commandSettings)) {
				if (dispatcher.getRoot().getChild(label) != null) {
					replaced.add(label);
					removeNode(dispatcher.getRoot(), label);
				}

				dispatcher.register(command.create(label));
				REGISTERED.add(label);
			}

			count++;
		}

		Essentials.LOGGER.info("Registered {} Essentials commands ({} names including aliases)", count, REGISTERED.size());

		if (!replaced.isEmpty()) {
			Essentials.LOGGER.info("Replaced existing commands: /{} (disable or rename them under \"commands\" in config.json to keep the originals)",
					String.join(", /", replaced));
		}
	}

	/** Re-registers after /essentials reload so enable toggles and aliases apply immediately. */
	public static void reregister(MinecraftServer server) {
		CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();

		for (String label : REGISTERED) {
			removeNode(dispatcher.getRoot(), label);
		}

		registerAll(dispatcher);

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			server.getCommands().sendCommands(player);
		}
	}

	/**
	 * Brigadier has no API to remove a node; its three child maps are private. Removing the node from
	 * all of them is the standard way to replace an existing command.
	 */
	@SuppressWarnings("unchecked")
	private static void removeNode(RootCommandNode<CommandSourceStack> root, String name) {
		try {
			for (String fieldName : new String[] {"children", "literals", "arguments"}) {
				Field field = CommandNode.class.getDeclaredField(fieldName);
				field.setAccessible(true);
				((Map<String, ?>) field.get(root)).remove(name);
			}
		} catch (ReflectiveOperationException e) {
			Essentials.LOGGER.error("Could not replace the existing /{} command; the Essentials version may not work", name, e);
		}
	}
}
