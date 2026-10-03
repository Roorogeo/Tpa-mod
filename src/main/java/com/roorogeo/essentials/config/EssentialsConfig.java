package com.roorogeo.essentials.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The contents of {@code config/essentials/config.json}.
 *
 * <p>Field names are written to JSON in kebab-case ({@code delaySeconds} becomes {@code delay-seconds}).
 * Missing keys get the defaults below, and the file is rewritten after loading so new options
 * show up for server owners after an update. See the wiki page "Configuration" for every key.
 */
public final class EssentialsConfig {
	public Permissions permissions = new Permissions();
	public Teleport teleport = new Teleport();
	public Homes homes = new Homes();
	public Warps warps = new Warps();
	public Spawn spawn = new Spawn();
	public Rtp rtp = new Rtp();
	public Tpa tpa = new Tpa();
	public Back back = new Back();
	public Combat combat = new Combat();
	public Chat chat = new Chat();
	public Messaging messaging = new Messaging();
	public Mail mail = new Mail();
	public Nick nick = new Nick();
	public Afk afk = new Afk();
	public Economy economy = new Economy();
	public Kits kits = new Kits();
	public Mute mute = new Mute();
	public Jail jail = new Jail();
	public Freeze freeze = new Freeze();
	public Vanish vanish = new Vanish();
	public Tempban tempban = new Tempban();
	public Player player = new Player();
	public Near near = new Near();
	public TabList tabList = new TabList();
	public JoinQuit joinQuit = new JoinQuit();
	public World world = new World();
	public Storage storage = new Storage();
	/** Per-command settings, keyed by the command's primary name. */
	public Map<String, CommandSettings> commands = new LinkedHashMap<>();

	public static final class Permissions {
		/** Operator level that the {@code op} default means. 2 matches vanilla command blocks / gamemasters. */
		public int opLevel = 2;
		/**
		 * Overrides of the built-in defaults: node (or pattern like {@code essentials.kit.*}) to
		 * {@code all}, {@code op}, {@code op:<level>} or {@code none}. Filled with every node on first start.
		 */
		public Map<String, String> defaults = new LinkedHashMap<>();
	}

	public static final class Teleport {
		/** Warmup before a teleport happens. 0 teleports instantly. */
		public int delaySeconds = 3;
		/** Time a player must wait between teleports. 0 disables it. */
		public int cooldownSeconds = 5;
		/** Cancel the warmup if the player moves. */
		public boolean cancelOnMove = true;
		/** Blocks a player may drift during the warmup before it counts as moving. Looking around never cancels. */
		public double moveTolerance = 0.5;
		/** Cancel the warmup if the player takes damage. */
		public boolean cancelOnDamage = true;
		/** Only start the cooldown once a teleport actually happened (not when it was cancelled). */
		public boolean cooldownOnlyOnSuccess = true;
		/** Seconds to wait for the destination chunk to load before giving up. */
		public int chunkLoadTimeoutSeconds = 15;
		/** Sound played at the destination. Empty string disables it. */
		public String sound = "minecraft:entity.enderman.teleport";
		public float soundVolume = 1.0f;
		public float soundPitch = 1.0f;
		/** Show a countdown in the action bar during the warmup. */
		public boolean warmupActionBar = true;
		public Safety safety = new Safety();
	}

	public static final class Safety {
		/** Check that the destination is safe and look for a nearby safe spot if it isn't. */
		public boolean enabled = true;
		/** Skip the check for players in creative or spectator. */
		public boolean skipForCreativeAndSpectator = true;
		/** Horizontal search radius for a safe spot. */
		public int horizontalRadius = 3;
		/** Vertical search range (up and down) for a safe spot. */
		public int verticalRadius = 8;
		/** Blocks that are never safe to stand in or on, besides lava, fire and other damaging blocks. */
		public List<String> unsafeBlocks = new ArrayList<>(List.of(
				"minecraft:lava", "minecraft:fire", "minecraft:soul_fire", "minecraft:magma_block",
				"minecraft:cactus", "minecraft:campfire", "minecraft:soul_campfire", "minecraft:sweet_berry_bush",
				"minecraft:wither_rose", "minecraft:powder_snow", "minecraft:pointed_dripstone", "minecraft:end_portal",
				"minecraft:nether_portal", "minecraft:end_gateway"));
		/** Treat water at the destination as unsafe. */
		public boolean waterIsUnsafe = false;
	}

	public static final class Homes {
		/** Homes a player gets when no essentials.sethome.multiple.<n> node is granted. */
		public int defaultMaxHomes = 3;
		/** Hard cap nobody can exceed. -1 removes the cap (then essentials.sethome.unlimited means truly unlimited). */
		public int absoluteMaxHomes = 10;
		/** Highest <n> probed for essentials.sethome.multiple.<n> when absolute-max-homes is -1. */
		public int multipleScanLimit = 100;
		/** Name used by /sethome and /home without a name. */
		public String defaultHomeName = "home";
		/** Allowed home names (after lower-casing). */
		public String namePattern = "[a-z0-9_-]{1,32}";
		/** /home with no name and several homes: true lists them, false uses the default home name. */
		public boolean listWhenAmbiguous = true;
		/** Let players set homes in these dimensions only. Empty allows every dimension. */
		public List<String> allowedDimensions = new ArrayList<>();
	}

	public static final class Warps {
		public String namePattern = "[a-z0-9_-]{1,32}";
		/** Check essentials.warp.<name> when using a warp. */
		public boolean perWarpPermissions = true;
		/** Only list warps the player may use in /warps. */
		public boolean listOnlyUsable = true;
	}

	public static final class Spawn {
		/** Teleport new players to /spawn when they first join. */
		public boolean teleportOnFirstJoin = true;
		/** Teleport every player to /spawn on every join. */
		public boolean teleportOnJoin = false;
		/** Respawn at /spawn when the player has no bed or respawn anchor. */
		public boolean respawnAtSpawn = true;
	}

	public static final class Rtp {
		/** Minimum distance from the center. */
		public int minRadius = 200;
		/** Maximum distance from the center. */
		public int maxRadius = 5000;
		/** Center of the search: "spawn" (world spawn) or "x,z", e.g. "0,0". */
		public String center = "spawn";
		/** Locations tried before giving up. */
		public int maxAttempts = 12;
		/** Dimensions /rtp works in. Using /rtp elsewhere sends you to target-dimension. */
		public List<String> allowedDimensions = new ArrayList<>(List.of("minecraft:overworld"));
		/** Where /rtp sends players who use it in a dimension that isn't allowed. */
		public String targetDimension = "minecraft:overworld";
		/** Keep results inside the world border. */
		public boolean respectWorldBorder = true;
		/** Biomes never chosen, e.g. "minecraft:ocean". */
		public List<String> blacklistedBiomes = new ArrayList<>(List.of(
				"minecraft:ocean", "minecraft:deep_ocean", "minecraft:cold_ocean", "minecraft:deep_cold_ocean",
				"minecraft:frozen_ocean", "minecraft:deep_frozen_ocean", "minecraft:lukewarm_ocean",
				"minecraft:deep_lukewarm_ocean", "minecraft:warm_ocean", "minecraft:river", "minecraft:frozen_river"));
	}

	public static final class Tpa {
		/** Seconds before an unanswered request expires. */
		public int timeoutSeconds = 120;
		/** Show clickable [Accept] [Deny] buttons. */
		public boolean clickableButtons = true;
		/** Maximum pending outgoing requests per player. A new request beyond this replaces the oldest. */
		public int maxOutgoing = 1;
	}

	public static final class Back {
		/** Record the location before every Essentials teleport. */
		public boolean recordTeleports = true;
		/** Record the death location (needs essentials.back.ondeath). */
		public boolean recordDeaths = true;
	}

	public static final class Combat {
		public boolean enabled = true;
		/** Tag length in seconds. Each hit resets it. */
		public int durationSeconds = 30;
		/** Also tag players hit by mobs (not just players). */
		public boolean tagOnMobDamage = false;
		/** Commands (primary names or aliases, without the slash) blocked while tagged. */
		public List<String> blockedCommands = new ArrayList<>(List.of(
				"home", "spawn", "warp", "tpa", "tpahere", "tpaccept", "back", "rtp", "top",
				"fly", "god", "heal", "feed", "vanish", "enderchest"));
		public boolean cancelTeleportOnTag = true;
		public boolean disableFlyOnTag = true;
		public boolean disableGodOnTag = true;
		/** Show the remaining time in the action bar. */
		public boolean actionBar = true;
		public boolean messageOnStart = true;
		public boolean messageOnEnd = true;
		/** Kill players who disconnect while tagged so their items drop at the logout spot. */
		public boolean killOnLogout = true;
		/** Treat timeouts (pulled cable, crashed client) as combat logging. */
		public boolean punishTimeouts = true;
		/** Broadcast combat.logout-broadcast when someone combat logs. */
		public boolean broadcastLogout = true;
	}

	public static final class Chat {
		/** Format public chat with chat.format from messages.json. false leaves vanilla chat untouched (ignore lists still apply). */
		public boolean formatEnabled = true;
		/** Radius in blocks for local chat. -1 sends chat to everyone. */
		public int localRadius = -1;
		/** Prefix that sends a message to everyone when local chat is on, e.g. "!hello". */
		public String globalPrefix = "!";
		/** Log formatted chat to the console. */
		public boolean logToConsole = true;
	}

	public static final class Messaging {
		/** Let players /msg players who are vanished (to them it looks like the player is offline). */
		public boolean allowMessagingVanished = false;
		/** Tell the sender when the receiver is AFK. */
		public boolean notifyAfk = true;
	}

	public static final class Mail {
		public int maxMailsPerPlayer = 50;
		public int maxLength = 256;
		public int pageSize = 5;
		/** Tell players about unread mail when they join. */
		public boolean notifyOnJoin = true;
		/** Seconds between two /mail send from the same player. 0 disables it. */
		public int sendCooldownSeconds = 5;
	}

	public static final class Nick {
		public int maxLength = 16;
		public int minLength = 2;
		/** Allowed characters once color codes are removed. */
		public String allowedPattern = "[A-Za-z0-9_]+";
		/** Prepended to every nickname so players can tell it apart from real names. */
		public String prefix = "~";
		/** Allow two players to use the same nickname, or a nickname equal to someone else's real name. */
		public boolean allowDuplicates = false;
	}

	public static final class Afk {
		/** Seconds without activity before a player is marked AFK. 0 disables it. */
		public int autoAfkSeconds = 300;
		/** Seconds without activity before a player is kicked. 0 disables it. */
		public int autoKickSeconds = 0;
		/** Broadcast AFK changes to everyone (false: only tell the player). */
		public boolean broadcast = true;
		/** Tell AFK players in the action bar that they are AFK. */
		public boolean actionBar = true;
		/** AFK players are invulnerable. */
		public boolean invulnerable = false;
	}

	public static final class Economy {
		public boolean enabled = true;
		public double startingBalance = 100.0;
		public String currencySymbol = "$";
		public String currencyNameSingular = "dollar";
		public String currencyNamePlural = "dollars";
		/** How amounts are shown. Placeholders: {symbol}, {amount}, {name}. */
		public String format = "{symbol}{amount}";
		public int decimalPlaces = 2;
		/** Group thousands, e.g. 1,000,000. */
		public boolean groupThousands = true;
		public double maxBalance = 1_000_000_000_000.0;
		public double minPayment = 0.01;
		/** Let /pay send money to offline players. */
		public boolean payOffline = true;
		public int baltopPageSize = 10;
	}

	public static final class Kits {
		/** Kit given to players on their first join. Empty disables it. */
		public String firstJoinKit = "";
		/** What happens to items that don't fit: "drop" drops them at the player's feet, "deny" refuses the kit. */
		public String overflow = "drop";
		/** Reserved names that can't be used for kits, because their node collides with another node. */
		public List<String> reservedNames = new ArrayList<>(List.of("others", "cooldown"));
		public String namePattern = "[a-z0-9_-]{1,32}";
	}

	public static final class Mute {
		/** Commands muted players can't use. */
		public List<String> blockedCommands = new ArrayList<>(List.of("msg", "reply", "me", "mail", "broadcast"));
		/** Duration used by /mute without one. "permanent" or a duration like 1h30m. */
		public String defaultDuration = "permanent";
	}

	public static final class Jail {
		/** Where players go on release: "previous" (where they were jailed from) or "spawn". */
		public String releaseLocation = "previous";
		/** Count time while the player is offline. */
		public boolean countOfflineTime = false;
		/** Commands jailed players may still use. Everything else from Essentials is blocked. */
		public List<String> allowedCommands = new ArrayList<>(List.of("msg", "reply", "mail", "list", "ping", "balance", "seen", "realname"));
		public boolean allowChat = true;
		/** Block breaking/placing blocks, using items and attacking. */
		public boolean preventInteraction = true;
		/** Blocks a jailed player may move from the jail before being pulled back. */
		public double radius = 6.0;
	}

	public static final class Freeze {
		public boolean preventInteraction = true;
		/** Block every Essentials command except allowed-commands. */
		public boolean blockCommands = true;
		public List<String> allowedCommands = new ArrayList<>(List.of("msg", "reply"));
		/** Frozen players can't take damage. */
		public boolean invulnerable = true;
	}

	public static final class Vanish {
		/** Remove vanished players from the tab list of players without essentials.vanish.see. */
		public boolean hideFromTabList = true;
		/** Hide the join/leave message of vanished players. */
		public boolean silentJoinQuit = true;
		/** Broadcast a fake leave/join message when vanishing/reappearing. */
		public boolean fakeMessages = true;
		/** Stay vanished after relogging. */
		public boolean persist = true;
		/** Vanished players can't take damage. */
		public boolean invulnerable = true;
		/** Show vanish.actionbar to vanished players. */
		public boolean actionBar = true;
	}

	public static final class Tempban {
		/** Longest duration allowed. Empty or "permanent" has no limit. */
		public String maxDuration = "";
	}

	public static final class Player {
		public boolean healRemovesNegativeEffects = true;
		public boolean healExtinguishes = true;
		/** God mode also keeps hunger full. */
		public boolean godPreventsHunger = true;
		/** Persist god mode across relogs. */
		public boolean persistGod = true;
		/** /speed 10 maps to this fly speed (vanilla default 0.05). */
		public float maxFlySpeed = 0.5f;
		/** /speed 10 maps to this walk speed (vanilla default 0.1). */
		public float maxWalkSpeed = 0.5f;
		/** /clearinventory also clears armor and offhand. */
		public boolean clearInventoryIncludesArmor = true;
		/** Items repairable with /repair are those that can take damage; list item ids here to exclude some. */
		public List<String> repairBlacklist = new ArrayList<>();
	}

	public static final class Near {
		public int defaultRadius = 200;
		public int maxRadius = 1000;
	}

	public static final class TabList {
		/** Show nicknames and the AFK tag in the tab list (uses tab-list.format from messages.json). */
		public boolean enabled = true;
	}

	public static final class JoinQuit {
		/** Replace vanilla join/leave messages with join-quit.* from messages.json. */
		public boolean customMessages = true;
		/** Send the MOTD (motd lines from messages.json) on join. */
		public boolean motd = true;
		/** Broadcast join-quit.first-join for a player's first join. */
		public boolean firstJoinBroadcast = true;
	}

	public static final class World {
		/** Default duration of /sun and /rain in seconds. 0 lets vanilla pick a random length. */
		public int weatherDurationSeconds = 0;
	}

	public static final class Storage {
		/** Seconds between saving changed player data. Data is also saved on logout and stop. */
		public int autosaveSeconds = 300;
		/** Pretty-print data files. */
		public boolean prettyPrint = true;
	}

	/** Settings that every command has. */
	public static final class CommandSettings {
		/** false leaves the command unregistered (and keeps a vanilla command of the same name). */
		public boolean enabled = true;
		/** Extra names for the command. */
		public List<String> aliases = new ArrayList<>();
		/** Seconds between uses (bypass: essentials.command.cooldown.bypass). 0 disables it. */
		public int cooldownSeconds = 0;
		/** Teleport commands only: warmup in seconds, -1 uses teleport.delay-seconds. */
		public int teleportDelaySeconds = -1;
		/** Teleport commands only: cooldown in seconds, -1 uses teleport.cooldown-seconds. A command with its own cooldown tracks it separately. */
		public int teleportCooldownSeconds = -1;

		public CommandSettings() {
		}

		public CommandSettings(List<String> aliases, int teleportDelaySeconds, int teleportCooldownSeconds) {
			this.aliases = new ArrayList<>(aliases);
			this.teleportDelaySeconds = teleportDelaySeconds;
			this.teleportCooldownSeconds = teleportCooldownSeconds;
		}
	}

	/** Default per-command settings, used for commands missing from the file. */
	public static Map<String, CommandSettings> defaultCommands() {
		Map<String, CommandSettings> map = new LinkedHashMap<>();
		// Teleport
		map.put("spawn", settings());
		map.put("setspawn", settings());
		map.put("home", settings("h"));
		map.put("sethome", settings());
		map.put("delhome", settings("remhome", "rmhome"));
		map.put("homes", settings());
		map.put("warp", settings());
		map.put("setwarp", settings("createwarp"));
		map.put("delwarp", settings("remwarp", "rmwarp"));
		map.put("warps", settings());
		map.put("tpa", settings("tpask", "call"));
		map.put("tpahere", settings());
		map.put("tpaccept", settings("tpyes"));
		map.put("tpdeny", settings("tpno"));
		map.put("tpacancel", settings("tpcancel"));
		map.put("tp", new CommandSettings(List.of("tpo"), 0, 0));
		map.put("tphere", new CommandSettings(List.of("s", "tpohere"), 0, 0));
		map.put("tpall", new CommandSettings(List.of(), 0, 0));
		map.put("back", settings("return"));
		map.put("top", settings());
		map.put("rtp", new CommandSettings(List.of("wild", "randomtp"), -1, 300));
		// Chat
		map.put("msg", settings("tell", "w", "m", "t", "whisper", "pm"));
		map.put("reply", settings("r"));
		map.put("mail", settings("email"));
		map.put("ignore", settings("unignore"));
		map.put("nick", settings("nickname"));
		map.put("realname", settings());
		map.put("me", settings("action"));
		map.put("broadcast", settings("bc", "bcast"));
		map.put("socialspy", settings("spy"));
		// Player
		map.put("heal", settings());
		map.put("feed", settings("eat"));
		map.put("fly", settings());
		map.put("god", settings("godmode", "tgm"));
		map.put("speed", settings());
		map.put("gamemode", settings("gm"));
		map.put("gmc", settings());
		map.put("gms", settings());
		map.put("gma", settings());
		map.put("gmsp", settings());
		map.put("afk", settings("away"));
		map.put("hat", settings("head"));
		map.put("repair", settings("fix"));
		map.put("enderchest", settings("ec", "echest"));
		map.put("workbench", settings("wb", "craft"));
		map.put("anvil", settings());
		map.put("invsee", settings());
		map.put("clearinventory", settings("ci", "clearinvent"));
		map.put("suicide", settings());
		map.put("near", settings("nearby"));
		map.put("seen", settings());
		map.put("whois", settings());
		map.put("list", settings("online", "who", "playerlist"));
		map.put("ping", settings("pong"));
		map.put("combat", settings("combattag", "ct"));
		// Economy
		map.put("balance", settings("bal", "money"));
		map.put("pay", settings());
		map.put("baltop", settings("balancetop"));
		map.put("eco", settings("economy"));
		// Kits
		map.put("kit", settings());
		map.put("kits", settings());
		map.put("createkit", settings("mkkit"));
		map.put("delkit", settings("rmkit"));
		// Moderation
		map.put("mute", settings());
		map.put("unmute", settings());
		map.put("tempban", settings("tban"));
		map.put("kick", settings());
		map.put("jail", settings());
		map.put("jails", settings());
		map.put("setjail", settings("createjail"));
		map.put("deljail", settings("remjail", "rmjail"));
		map.put("unjail", settings());
		map.put("vanish", settings("v"));
		map.put("freeze", settings());
		map.put("sudo", settings());
		// World
		map.put("time", settings());
		map.put("day", settings());
		map.put("night", settings());
		map.put("weather", settings());
		map.put("sun", settings());
		map.put("rain", settings());
		// Admin
		map.put("essentials", settings("ess"));
		return map;
	}

	private static CommandSettings settings(String... aliases) {
		return new CommandSettings(List.of(aliases), -1, -1);
	}
}
