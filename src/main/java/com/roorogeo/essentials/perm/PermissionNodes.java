package com.roorogeo.essentials.perm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every permission node the mod checks, with its built-in fallback default and a description.
 *
 * <p>This class deliberately has no Minecraft imports: {@link #main(String[])} renders the
 * {@code PERMISSIONS.md} reference straight from this registry, so the documentation can never
 * drift from the code. Run it with {@code java src/main/java/com/roorogeo/essentials/perm/PermissionNodes.java PERMISSIONS.md}.
 *
 * <p>The defaults here are only used when no permission mod answers the check (or the permission
 * mod has no value for the node). Every default can be overridden in {@code config.json} under
 * {@code permissions.defaults}.
 */
public final class PermissionNodes {
	/** Who gets a node when no permission mod has an opinion. */
	public enum Level {
		/** Everyone. */
		ALL,
		/** Operators at or above {@code permissions.op-level} (default 2). */
		OP,
		/** Nobody, not even operators. Console still passes because it has the highest level. */
		NONE
	}

	/**
	 * A permission node.
	 *
	 * @param id node, e.g. {@code essentials.home.others}; pattern nodes end with {@code <name>} or {@code <number>}
	 * @param level built-in default
	 * @param category heading used in the generated reference
	 * @param description what the node allows
	 */
	public record Node(String id, Level level, String category, String description) {
		public boolean isPattern() {
			return this.id.contains("<");
		}

		/** For pattern nodes, the prefix before the placeholder, e.g. {@code essentials.kit.}. */
		public String patternPrefix() {
			int index = this.id.indexOf('<');
			return index < 0 ? this.id : this.id.substring(0, index);
		}

		/** The key used for a pattern in the {@code permissions.defaults} config, e.g. {@code essentials.kit.*}. */
		public String configKey() {
			return this.isPattern() ? this.patternPrefix() + "*" : this.id;
		}
	}

	private static final Map<String, Node> NODES = new LinkedHashMap<>();

	private static final String TELEPORT = "Teleport";
	private static final String CHAT = "Chat";
	private static final String PLAYER = "Player";
	private static final String ECONOMY = "Economy";
	private static final String KITS = "Kits";
	private static final String MODERATION = "Moderation";
	private static final String WORLD = "World";
	private static final String ADMIN = "Admin";
	private static final String COMBAT = "Combat";

	// ---------------------------------------------------------------- Teleport
	public static final Node SPAWN = add("essentials.spawn", Level.ALL, TELEPORT, "Use /spawn to teleport to the server spawn.");
	public static final Node SPAWN_OTHERS = add("essentials.spawn.others", Level.OP, TELEPORT, "Use /spawn <player> to send another player to spawn.");
	public static final Node SETSPAWN = add("essentials.setspawn", Level.OP, TELEPORT, "Use /setspawn to set the server spawn to your position.");
	public static final Node HOME = add("essentials.home", Level.ALL, TELEPORT, "Use /home [name] to teleport to one of your homes.");
	public static final Node HOME_OTHERS = add("essentials.home.others", Level.OP, TELEPORT, "Use /home <player>:<home> to teleport to another player's home.");
	public static final Node SETHOME = add("essentials.sethome", Level.ALL, TELEPORT, "Use /sethome [name] to set a home.");
	public static final Node SETHOME_OTHERS = add("essentials.sethome.others", Level.OP, TELEPORT, "Use /sethome <player>:<home> to set a home for another player (ignores their limit).");
	public static final Node SETHOME_MULTIPLE = add("essentials.sethome.multiple.<number>", Level.NONE, TELEPORT, "Raise the home limit to <number>. The highest granted number wins, then it is clamped to homes.absolute-max-homes.");
	public static final Node SETHOME_UNLIMITED = add("essentials.sethome.unlimited", Level.OP, TELEPORT, "Ignore per-group home limits. homes.absolute-max-homes still applies unless it is -1.");
	public static final Node DELHOME = add("essentials.delhome", Level.ALL, TELEPORT, "Use /delhome <name> to delete one of your homes.");
	public static final Node DELHOME_OTHERS = add("essentials.delhome.others", Level.OP, TELEPORT, "Use /delhome <player>:<home> to delete another player's home.");
	public static final Node HOMES = add("essentials.homes", Level.ALL, TELEPORT, "Use /homes to list your homes and see used/max.");
	public static final Node HOMES_OTHERS = add("essentials.homes.others", Level.OP, TELEPORT, "Use /homes <player> to list another player's homes.");
	public static final Node WARP = add("essentials.warp", Level.ALL, TELEPORT, "Use /warp <name> to teleport to a warp.");
	public static final Node WARP_NAMED = add("essentials.warp.<name>", Level.ALL, TELEPORT, "Use the warp called <name>. Deny it to lock a single warp.");
	public static final Node WARP_OTHERS = add("essentials.warp.others", Level.OP, TELEPORT, "Use /warp <name> <player> to send another player to a warp.");
	public static final Node WARPS = add("essentials.warps", Level.ALL, TELEPORT, "Use /warps to list the warps you can use.");
	public static final Node SETWARP = add("essentials.setwarp", Level.OP, TELEPORT, "Use /setwarp <name> to create or move a warp.");
	public static final Node DELWARP = add("essentials.delwarp", Level.OP, TELEPORT, "Use /delwarp <name> to delete a warp.");
	public static final Node TPA = add("essentials.tpa", Level.ALL, TELEPORT, "Use /tpa <player> to ask to teleport to a player.");
	public static final Node TPAHERE = add("essentials.tpahere", Level.ALL, TELEPORT, "Use /tpahere <player> to ask a player to teleport to you.");
	public static final Node TPACCEPT = add("essentials.tpaccept", Level.ALL, TELEPORT, "Use /tpaccept [player] to accept a teleport request.");
	public static final Node TPDENY = add("essentials.tpdeny", Level.ALL, TELEPORT, "Use /tpdeny [player] to deny a teleport request.");
	public static final Node TPACANCEL = add("essentials.tpacancel", Level.ALL, TELEPORT, "Use /tpacancel [player] to cancel your outgoing requests.");
	public static final Node TP = add("essentials.tp", Level.OP, TELEPORT, "Use /tp <player> to teleport yourself to a player instantly.");
	public static final Node TP_OTHERS = add("essentials.tp.others", Level.OP, TELEPORT, "Use /tp <player> <target> to teleport one player to another.");
	public static final Node TP_POSITION = add("essentials.tp.position", Level.OP, TELEPORT, "Use /tp [player] <x> <y> <z> to teleport to coordinates.");
	public static final Node TPHERE = add("essentials.tphere", Level.OP, TELEPORT, "Use /tphere <player> to pull a player to you.");
	public static final Node TPALL = add("essentials.tpall", Level.OP, TELEPORT, "Use /tpall [player] to pull every online player to you (or to <player>).");
	public static final Node BACK = add("essentials.back", Level.ALL, TELEPORT, "Use /back to return to your previous location.");
	public static final Node BACK_ONDEATH = add("essentials.back.ondeath", Level.ALL, TELEPORT, "Your death location is recorded for /back.");
	public static final Node TOP = add("essentials.top", Level.OP, TELEPORT, "Use /top to teleport to the highest block above you.");
	public static final Node RTP = add("essentials.rtp", Level.ALL, TELEPORT, "Use /rtp to teleport to a random safe location.");
	public static final Node RTP_OTHERS = add("essentials.rtp.others", Level.OP, TELEPORT, "Use /rtp <player> to send another player to a random location.");
	public static final Node TELEPORT_COOLDOWN_BYPASS = add("essentials.teleport.cooldown.bypass", Level.OP, TELEPORT, "Skip the teleport cooldown.");
	public static final Node TELEPORT_DELAY_BYPASS = add("essentials.teleport.delay.bypass", Level.OP, TELEPORT, "Skip the teleport warmup delay.");
	public static final Node TELEPORT_SAFETY_BYPASS = add("essentials.teleport.safety.bypass", Level.OP, TELEPORT, "Skip the safe-destination check and teleport even into unsafe spots.");

	// ---------------------------------------------------------------- Chat
	public static final Node MSG = add("essentials.msg", Level.ALL, CHAT, "Use /msg <player> <message> to send a private message.");
	public static final Node MSG_COLOR = add("essentials.msg.color", Level.OP, CHAT, "Use & color codes in private messages.");
	public static final Node REPLY = add("essentials.reply", Level.ALL, CHAT, "Use /reply <message> to answer the last private message.");
	public static final Node MAIL = add("essentials.mail", Level.ALL, CHAT, "Use /mail (base node; each sub-command has its own node below).");
	public static final Node MAIL_READ = add("essentials.mail.read", Level.ALL, CHAT, "Use /mail read [page].");
	public static final Node MAIL_SEND = add("essentials.mail.send", Level.ALL, CHAT, "Use /mail send <player> <message>, also to offline players.");
	public static final Node MAIL_SENDALL = add("essentials.mail.sendall", Level.OP, CHAT, "Use /mail sendall <message> to mail every known player.");
	public static final Node MAIL_CLEAR = add("essentials.mail.clear", Level.ALL, CHAT, "Use /mail clear to delete your mail.");
	public static final Node IGNORE = add("essentials.ignore", Level.ALL, CHAT, "Use /ignore <player> to hide a player's chat, messages, mail and teleport requests.");
	public static final Node IGNORE_EXEMPT = add("essentials.ignore.exempt", Level.OP, CHAT, "Cannot be ignored by other players.");
	public static final Node NICK = add("essentials.nick", Level.OP, CHAT, "Use /nick <nickname|off> to change your display name.");
	public static final Node NICK_OTHERS = add("essentials.nick.others", Level.OP, CHAT, "Use /nick <nickname|off> <player> to change another player's nickname.");
	public static final Node NICK_COLOR = add("essentials.nick.color", Level.OP, CHAT, "Use & color codes (0-9, a-f and &#RRGGBB) in nicknames.");
	public static final Node NICK_FORMAT = add("essentials.nick.format", Level.OP, CHAT, "Use &l &m &n &o &r formatting codes in nicknames.");
	public static final Node NICK_MAGIC = add("essentials.nick.magic", Level.OP, CHAT, "Use the &k obfuscated code in nicknames.");
	public static final Node REALNAME = add("essentials.realname", Level.ALL, CHAT, "Use /realname <nickname> to find who is behind a nickname.");
	public static final Node ME = add("essentials.me", Level.ALL, CHAT, "Use /me <action>.");
	public static final Node BROADCAST = add("essentials.broadcast", Level.OP, CHAT, "Use /broadcast <message>.");
	public static final Node SOCIALSPY = add("essentials.socialspy", Level.OP, CHAT, "Use /socialspy to see other players' private messages and mail.");
	public static final Node CHAT_COLOR = add("essentials.chat.color", Level.OP, CHAT, "Use & color codes (0-9, a-f and &#RRGGBB) in public chat.");
	public static final Node CHAT_FORMAT = add("essentials.chat.format", Level.OP, CHAT, "Use &l &m &n &o &r formatting codes in public chat.");
	public static final Node CHAT_MAGIC = add("essentials.chat.magic", Level.OP, CHAT, "Use the &k obfuscated code in public chat.");
	public static final Node CHAT_GROUP = add("essentials.chat.group.<name>", Level.NONE, CHAT, "Chat with the format chat.group-formats.<name> from config.json. The first group in config order that a player has wins.");

	// ---------------------------------------------------------------- Player
	public static final Node HEAL = add("essentials.heal", Level.OP, PLAYER, "Use /heal to restore your health, hunger and remove fire.");
	public static final Node HEAL_OTHERS = add("essentials.heal.others", Level.OP, PLAYER, "Use /heal <player>.");
	public static final Node FEED = add("essentials.feed", Level.OP, PLAYER, "Use /feed to fill your hunger.");
	public static final Node FEED_OTHERS = add("essentials.feed.others", Level.OP, PLAYER, "Use /feed <player>.");
	public static final Node FLY = add("essentials.fly", Level.OP, PLAYER, "Use /fly to toggle flight.");
	public static final Node FLY_OTHERS = add("essentials.fly.others", Level.OP, PLAYER, "Use /fly <player>.");
	public static final Node GOD = add("essentials.god", Level.OP, PLAYER, "Use /god to toggle invulnerability.");
	public static final Node GOD_OTHERS = add("essentials.god.others", Level.OP, PLAYER, "Use /god <player>.");
	public static final Node SPEED = add("essentials.speed", Level.OP, PLAYER, "Use /speed <0-10> (applies to flying or walking, whichever you are doing).");
	public static final Node SPEED_FLY = add("essentials.speed.fly", Level.OP, PLAYER, "Change fly speed (/speed <n> fly).");
	public static final Node SPEED_WALK = add("essentials.speed.walk", Level.OP, PLAYER, "Change walk speed (/speed <n> walk).");
	public static final Node SPEED_OTHERS = add("essentials.speed.others", Level.OP, PLAYER, "Use /speed <n> <fly|walk> <player>.");
	public static final Node GAMEMODE = add("essentials.gamemode", Level.OP, PLAYER, "Use /gamemode and the /gmc /gms /gma /gmsp shortcuts (each mode also needs its own node).");
	public static final Node GAMEMODE_SURVIVAL = add("essentials.gamemode.survival", Level.OP, PLAYER, "Switch to survival.");
	public static final Node GAMEMODE_CREATIVE = add("essentials.gamemode.creative", Level.OP, PLAYER, "Switch to creative.");
	public static final Node GAMEMODE_ADVENTURE = add("essentials.gamemode.adventure", Level.OP, PLAYER, "Switch to adventure.");
	public static final Node GAMEMODE_SPECTATOR = add("essentials.gamemode.spectator", Level.OP, PLAYER, "Switch to spectator.");
	public static final Node GAMEMODE_OTHERS = add("essentials.gamemode.others", Level.OP, PLAYER, "Change another player's game mode.");
	public static final Node AFK = add("essentials.afk", Level.ALL, PLAYER, "Use /afk [message] to toggle AFK.");
	public static final Node AFK_OTHERS = add("essentials.afk.others", Level.OP, PLAYER, "Use /afk <player> to toggle another player's AFK status.");
	public static final Node AFK_KICKEXEMPT = add("essentials.afk.kickexempt", Level.OP, PLAYER, "Never kicked for being AFK too long.");
	public static final Node HAT = add("essentials.hat", Level.ALL, PLAYER, "Use /hat to wear the item in your hand.");
	public static final Node REPAIR = add("essentials.repair", Level.OP, PLAYER, "Use /repair [hand] to repair the item in your hand.");
	public static final Node REPAIR_ALL = add("essentials.repair.all", Level.OP, PLAYER, "Use /repair all to repair your whole inventory.");
	public static final Node ENDERCHEST = add("essentials.enderchest", Level.OP, PLAYER, "Use /enderchest to open your ender chest anywhere.");
	public static final Node ENDERCHEST_OTHERS = add("essentials.enderchest.others", Level.OP, PLAYER, "Use /enderchest <player> to view another player's ender chest.");
	public static final Node ENDERCHEST_MODIFY = add("essentials.enderchest.modify", Level.OP, PLAYER, "Take and put items when viewing another player's ender chest.");
	public static final Node WORKBENCH = add("essentials.workbench", Level.OP, PLAYER, "Use /workbench to open a crafting table anywhere.");
	public static final Node ANVIL = add("essentials.anvil", Level.OP, PLAYER, "Use /anvil to open an anvil anywhere.");
	public static final Node INVSEE = add("essentials.invsee", Level.OP, PLAYER, "Use /invsee <player> to view a player's inventory.");
	public static final Node INVSEE_MODIFY = add("essentials.invsee.modify", Level.OP, PLAYER, "Take and put items with /invsee.");
	public static final Node CLEARINVENTORY = add("essentials.clearinventory", Level.OP, PLAYER, "Use /clearinventory to empty your inventory.");
	public static final Node CLEARINVENTORY_OTHERS = add("essentials.clearinventory.others", Level.OP, PLAYER, "Use /clearinventory <player>.");
	public static final Node SUICIDE = add("essentials.suicide", Level.ALL, PLAYER, "Use /suicide.");
	public static final Node NEAR = add("essentials.near", Level.OP, PLAYER, "Use /near to list nearby players within near.default-radius.");
	public static final Node NEAR_RADIUS = add("essentials.near.radius", Level.OP, PLAYER, "Use /near <radius> with a custom radius (up to near.max-radius).");
	public static final Node SEEN = add("essentials.seen", Level.ALL, PLAYER, "Use /seen <player> to see when a player was last online.");
	public static final Node WHOIS = add("essentials.whois", Level.OP, PLAYER, "Use /whois <player> to see detailed player information.");
	public static final Node WHOIS_IP = add("essentials.whois.ip", Level.OP, PLAYER, "See IP addresses in /whois.");
	public static final Node LIST = add("essentials.list", Level.ALL, PLAYER, "Use /list to see online players.");
	public static final Node PING = add("essentials.ping", Level.ALL, PLAYER, "Use /ping to see your latency.");
	public static final Node PING_OTHERS = add("essentials.ping.others", Level.OP, PLAYER, "Use /ping <player>.");

	// ---------------------------------------------------------------- Combat
	public static final Node COMBAT_CHECK = add("essentials.combat.check", Level.ALL, COMBAT, "Use /combat to see your remaining combat tag time.");
	public static final Node COMBAT_BYPASS = add("essentials.combat.bypass", Level.OP, COMBAT, "Never tagged as in combat.");
	public static final Node COMBAT_COMMAND_BYPASS = add("essentials.combat.command.bypass", Level.OP, COMBAT, "Use commands from combat.blocked-commands while tagged.");

	// ---------------------------------------------------------------- Economy
	public static final Node BALANCE = add("essentials.balance", Level.ALL, ECONOMY, "Use /balance to see your balance.");
	public static final Node BALANCE_OTHERS = add("essentials.balance.others", Level.ALL, ECONOMY, "Use /balance <player>.");
	public static final Node PAY = add("essentials.pay", Level.ALL, ECONOMY, "Use /pay <player> <amount>.");
	public static final Node BALTOP = add("essentials.baltop", Level.ALL, ECONOMY, "Use /baltop [page].");
	public static final Node ECO = add("essentials.eco", Level.OP, ECONOMY, "Use /eco (each sub-command has its own node below).");
	public static final Node ECO_GIVE = add("essentials.eco.give", Level.OP, ECONOMY, "Use /eco give <player> <amount>.");
	public static final Node ECO_TAKE = add("essentials.eco.take", Level.OP, ECONOMY, "Use /eco take <player> <amount>.");
	public static final Node ECO_SET = add("essentials.eco.set", Level.OP, ECONOMY, "Use /eco set <player> <amount>.");
	public static final Node ECO_RESET = add("essentials.eco.reset", Level.OP, ECONOMY, "Use /eco reset <player> to restore the starting balance.");

	// ---------------------------------------------------------------- Kits
	public static final Node KIT = add("essentials.kit", Level.ALL, KITS, "Use /kit <name>.");
	public static final Node KIT_NAMED = add("essentials.kit.<name>", Level.ALL, KITS, "Claim the kit called <name>. Deny it (or set its default to op) to restrict a kit.");
	public static final Node KIT_OTHERS = add("essentials.kit.others", Level.OP, KITS, "Use /kit <name> <player> to give a kit to someone else.");
	public static final Node KIT_COOLDOWN_BYPASS = add("essentials.kit.cooldown.bypass", Level.OP, KITS, "Ignore kit cooldowns and one-time limits.");
	public static final Node KITS_LIST = add("essentials.kits", Level.ALL, KITS, "Use /kits to list the kits you can claim.");
	public static final Node CREATEKIT = add("essentials.createkit", Level.OP, KITS, "Use /createkit <name> <cooldown> to save your inventory as a kit.");
	public static final Node DELKIT = add("essentials.delkit", Level.OP, KITS, "Use /delkit <name>.");

	// ---------------------------------------------------------------- Moderation
	public static final Node MUTE = add("essentials.mute", Level.OP, MODERATION, "Use /mute <player> [duration] [reason].");
	public static final Node MUTE_EXEMPT = add("essentials.mute.exempt", Level.OP, MODERATION, "Cannot be muted.");
	public static final Node UNMUTE = add("essentials.unmute", Level.OP, MODERATION, "Use /unmute <player>.");
	public static final Node TEMPBAN = add("essentials.tempban", Level.OP, MODERATION, "Use /tempban <player> <duration> [reason].");
	public static final Node TEMPBAN_EXEMPT = add("essentials.tempban.exempt", Level.OP, MODERATION, "Cannot be temp-banned (checked while online).");
	public static final Node KICK = add("essentials.kick", Level.OP, MODERATION, "Use /kick <player> [reason].");
	public static final Node KICK_EXEMPT = add("essentials.kick.exempt", Level.OP, MODERATION, "Cannot be kicked with /kick.");
	public static final Node JAIL = add("essentials.jail", Level.OP, MODERATION, "Use /jail <player> <jail> [duration] [reason].");
	public static final Node JAILS = add("essentials.jails", Level.OP, MODERATION, "Use /jails to list jails.");
	public static final Node JAIL_EXEMPT = add("essentials.jail.exempt", Level.OP, MODERATION, "Cannot be jailed while online.");
	public static final Node SETJAIL = add("essentials.setjail", Level.OP, MODERATION, "Use /setjail <name> to create or move a jail.");
	public static final Node DELJAIL = add("essentials.deljail", Level.OP, MODERATION, "Use /deljail <name>.");
	public static final Node UNJAIL = add("essentials.unjail", Level.OP, MODERATION, "Use /unjail <player>.");
	public static final Node VANISH = add("essentials.vanish", Level.OP, MODERATION, "Use /vanish to hide from other players.");
	public static final Node VANISH_OTHERS = add("essentials.vanish.others", Level.OP, MODERATION, "Use /vanish <player>.");
	public static final Node VANISH_SEE = add("essentials.vanish.see", Level.OP, MODERATION, "See vanished players in the world, tab list, /list and /near.");
	public static final Node FREEZE = add("essentials.freeze", Level.OP, MODERATION, "Use /freeze <player> to toggle freezing a player in place.");
	public static final Node FREEZE_EXEMPT = add("essentials.freeze.exempt", Level.OP, MODERATION, "Cannot be frozen.");
	public static final Node SUDO = add("essentials.sudo", Level.OP, MODERATION, "Use /sudo <player> <command|c:message> to run a command or chat as another player.");
	public static final Node SUDO_EXEMPT = add("essentials.sudo.exempt", Level.OP, MODERATION, "Cannot be targeted by /sudo.");

	// ---------------------------------------------------------------- World
	public static final Node TIME = add("essentials.time", Level.ALL, WORLD, "Use /time to see the current time.");
	public static final Node TIME_SET = add("essentials.time.set", Level.OP, WORLD, "Use /time set <day|noon|night|midnight|ticks> [world].");
	public static final Node TIME_ADD = add("essentials.time.add", Level.OP, WORLD, "Use /time add <ticks> [world].");
	public static final Node DAY = add("essentials.day", Level.OP, WORLD, "Use /day [world].");
	public static final Node NIGHT = add("essentials.night", Level.OP, WORLD, "Use /night [world].");
	public static final Node WEATHER = add("essentials.weather", Level.OP, WORLD, "Use /weather <clear|rain|thunder> [duration] (each type also needs its own node).");
	public static final Node WEATHER_CLEAR = add("essentials.weather.clear", Level.OP, WORLD, "Set clear weather.");
	public static final Node WEATHER_RAIN = add("essentials.weather.rain", Level.OP, WORLD, "Set rain.");
	public static final Node WEATHER_THUNDER = add("essentials.weather.thunder", Level.OP, WORLD, "Set a thunderstorm.");
	public static final Node SUN = add("essentials.sun", Level.OP, WORLD, "Use /sun [duration].");
	public static final Node RAIN = add("essentials.rain", Level.OP, WORLD, "Use /rain [duration].");

	// ---------------------------------------------------------------- Admin
	public static final Node ESSENTIALS = add("essentials.essentials", Level.OP, ADMIN, "Use /essentials (shows version and sub-commands).");
	public static final Node RELOAD = add("essentials.reload", Level.OP, ADMIN, "Use /essentials reload to reload config.json, messages.json and kits.json.");
	public static final Node COMMAND_COOLDOWN_BYPASS = add("essentials.command.cooldown.bypass", Level.OP, ADMIN, "Ignore per-command cooldowns set in commands.<name>.cooldown-seconds.");

	private PermissionNodes() {
	}

	private static Node add(String id, Level level, String category, String description) {
		Node node = new Node(id, level, category, description);

		if (NODES.put(id, node) != null) {
			throw new IllegalStateException("Duplicate permission node " + id);
		}

		return node;
	}

	public static Map<String, Node> all() {
		return Collections.unmodifiableMap(NODES);
	}

	/** Pattern nodes (with a placeholder), e.g. {@code essentials.kit.<name>}. */
	public static List<Node> patterns() {
		List<Node> list = new ArrayList<>();

		for (Node node : NODES.values()) {
			if (node.isPattern()) {
				list.add(node);
			}
		}

		return list;
	}

	/** Renders the PERMISSIONS.md reference. */
	public static String markdown() {
		StringBuilder out = new StringBuilder();
		out.append("# Permission nodes\n\n");
		out.append("<!-- Generated from src/main/java/com/roorogeo/essentials/perm/PermissionNodes.java. Do not edit by hand. -->\n\n");
		out.append("Every command and every sub-feature of a command is gated by its own node, checked through\n");
		out.append("[fabric-permissions-api](https://github.com/lucko/fabric-permissions-api), so LuckPerms (or any other\n");
		out.append("permission mod using that API) controls everything. Commands you lack the node for are hidden from\n");
		out.append("the command tree, so they do not tab-complete.\n\n");
		out.append("## Defaults\n\n");
		out.append("The **Default** column is what happens when no permission mod is installed, or the permission mod has\n");
		out.append("no value for the node:\n\n");
		out.append("| Default | Meaning |\n|---|---|\n");
		out.append("| `all` | Everyone |\n");
		out.append("| `op` | Operators with at least `permissions.op-level` (default `2`). The console always passes. |\n");
		out.append("| `none` | Nobody (only the console). Grant it with a permission mod. |\n\n");
		out.append("Every default can be changed in `config/essentials/config.json`:\n\n");
		out.append("```json\n\"permissions\": {\n  \"op-level\": 2,\n  \"defaults\": {\n    \"essentials.nick\": \"all\",\n    \"essentials.kit.*\": \"op\",\n    \"essentials.kit.starter\": \"all\",\n    \"essentials.heal\": \"op:3\"\n  }\n}\n```\n\n");
		out.append("Accepted values: `all` (or `true`), `op`, `op:<level>`, `none` (or `false`). For pattern nodes the\n");
		out.append("most specific key wins: `essentials.kit.starter` beats `essentials.kit.*`, which beats the built-in default.\n\n");
		out.append("Placeholders: `<name>` is a warp or kit name, `<number>` is a whole number.\n\n");

		String category = null;

		for (Node node : NODES.values()) {
			if (!node.category().equals(category)) {
				if (category != null) {
					out.append("\n");
				}

				category = node.category();
				out.append("## ").append(category).append("\n\n");
				out.append("| Node | Default | Description |\n|---|---|---|\n");
			}

			out.append("| `").append(node.id()).append("` | `").append(node.level().name().toLowerCase(java.util.Locale.ROOT))
					.append("` | ").append(node.description().replace("|", "\\|")).append(" |\n");
		}

		out.append("\n## LuckPerms examples\n\n");
		out.append("```\n");
		out.append("# Let the default group use /nick with colors\n");
		out.append("/lp group default permission set essentials.nick true\n");
		out.append("/lp group default permission set essentials.nick.color true\n\n");
		out.append("# Give VIPs 10 homes (clamped to homes.absolute-max-homes)\n");
		out.append("/lp group vip permission set essentials.sethome.multiple.10 true\n\n");
		out.append("# Lock the 'vip' kit and the 'arena' warp for everyone except VIPs\n");
		out.append("/lp group default permission set essentials.kit.vip false\n");
		out.append("/lp group vip permission set essentials.kit.vip true\n");
		out.append("/lp group default permission set essentials.warp.arena false\n\n");
		out.append("# Staff: no teleport warmup or cooldown, can use commands while in combat\n");
		out.append("/lp group staff permission set essentials.teleport.delay.bypass true\n");
		out.append("/lp group staff permission set essentials.teleport.cooldown.bypass true\n");
		out.append("/lp group staff permission set essentials.combat.command.bypass true\n");
		out.append("```\n");
		return out.toString();
	}

	/** Writes the markdown reference to the path given as the first argument (default {@code PERMISSIONS.md}). */
	public static void main(String[] args) throws IOException {
		Path path = Path.of(args.length > 0 ? args[0] : "PERMISSIONS.md");
		Files.writeString(path, markdown());
		System.out.println("Wrote " + NODES.size() + " nodes to " + path.toAbsolutePath());
	}
}
