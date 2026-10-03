package com.roorogeo.essentials.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Built-in text for every player-facing message. Server owners override any of these in
 * {@code config/essentials/messages.json}; missing keys are added to the file with these defaults.
 *
 * <p>Formatting: {@code &0-&9 &a-&f} colors, {@code &#RRGGBB} hex colors, {@code &l &m &n &o &k}
 * styles, {@code &r} reset and {@code \n} for a new line. Words in braces like {@code {player}}
 * are placeholders; {tag} works in every message and inserts the "tag" message. Setting a message
 * to an empty string stops it from being sent.
 */
public final class DefaultMessages {
	private DefaultMessages() {
	}

	public static Map<String, String> create() {
		Map<String, String> m = new LinkedHashMap<>();

		// General
		m.put("tag", "&8[&6Essentials&8]&r ");
		m.put("general.no-permission", "&cYou don't have permission to do that.");
		m.put("general.player-only", "&cOnly players can use this command.");
		m.put("general.player-not-found", "&cPlayer &e{player}&c was not found.");
		m.put("general.player-never-joined", "&e{player}&c has never played on this server.");
		m.put("general.data-loading", "&cPlayer data is still loading, try again in a moment.");
		m.put("general.command-cooldown", "&cYou must wait &e{time}&c before using &e/{command}&c again.");
		m.put("general.combat-blocked", "&cYou can't use &e/{command}&c while in combat! &7({time} left)");
		m.put("general.muted-blocked", "&cYou can't use &e/{command}&c while muted.");
		m.put("general.jailed-blocked", "&cYou can't use &e/{command}&c while jailed.");
		m.put("general.frozen-blocked", "&cYou can't use &e/{command}&c while frozen.");
		m.put("general.invalid-duration", "&cInvalid duration &e{input}&c. Use e.g. 30s, 10m, 2h, 1d, 1w or permanent.");
		m.put("general.unknown-world", "&cUnknown world &e{world}&c.");
		m.put("general.reloaded", "&aReloaded config.json, messages.json and kits.json.");
		m.put("general.reload-failed", "&cReload failed: {error}. Check the server log.");
		m.put("general.version", "&6Essentials &e{version}&6. Sub-commands: &e/essentials reload");
		m.put("general.target-offline", "&e{player}&c is not online.");
		m.put("general.no-reason", "No reason given");

		// Durations
		m.put("time.days", "{n}d");
		m.put("time.hours", "{n}h");
		m.put("time.minutes", "{n}m");
		m.put("time.seconds", "{n}s");
		m.put("time.separator", " ");
		m.put("time.permanent", "permanent");
		m.put("time.now", "now");
		m.put("time.date-format", "yyyy-MM-dd HH:mm");

		// Teleport
		m.put("teleport.warmup", "&6Teleporting to &e{destination}&6 in &e{seconds}&6 seconds. Don't move!");
		m.put("teleport.warmup-actionbar", "&6Teleporting in &e{seconds}&6...");
		m.put("teleport.cancelled-move", "&cTeleport cancelled because you moved.");
		m.put("teleport.cancelled-damage", "&cTeleport cancelled because you took damage.");
		m.put("teleport.cancelled-combat", "&cTeleport cancelled because you entered combat.");
		m.put("teleport.cancelled-death", "&cTeleport cancelled because you died.");
		m.put("teleport.cancelled-replaced", "&cYour pending teleport to &e{destination}&c was replaced.");
		m.put("teleport.cooldown", "&cYou must wait &e{time}&c before teleporting again.");
		m.put("teleport.success", "&aTeleported to &e{destination}&a.");
		m.put("teleport.unsafe", "&cThe destination &e{destination}&c is not safe and no safe spot was found nearby.");
		m.put("teleport.chunk-timeout", "&cTeleport cancelled, &e{destination}&c took too long to load.");
		m.put("teleport.world-missing", "&cThe world of &e{destination}&c no longer exists.");
		m.put("teleport.destination-gone", "&cTeleport cancelled, &e{destination}&c is no longer available.");
		m.put("teleport.in-combat", "&cYou can't teleport while in combat! &7({time} left)");
		m.put("teleport.location-format", "{x}, {y}, {z} ({world})");

		// Spawn
		m.put("spawn.destination", "spawn");
		m.put("spawn.set", "&aSpawn set to your location.");
		m.put("spawn.sent-other", "&aSent &e{player}&a to spawn.");
		m.put("spawn.sent-by", "&6You were sent to spawn by &e{sender}&6.");

		// Homes
		m.put("home.destination", "home {home}");
		m.put("home.destination-other", "{player}'s home {home}");
		m.put("home.not-found", "&cYou don't have a home called &e{home}&c.");
		m.put("home.not-found-other", "&e{player}&c doesn't have a home called &e{home}&c.");
		m.put("home.none", "&cYou don't have any homes. Use &e/sethome [name]&c to set one.");
		m.put("home.set", "&aHome &e{home}&a set. &7({count}/{max})");
		m.put("home.updated", "&aHome &e{home}&a moved to your location.");
		m.put("home.set-other", "&aSet home &e{home}&a for &e{player}&a.");
		m.put("home.limit-reached", "&cYou have {count}/{max} homes. Delete one with /delhome.");
		m.put("home.over-limit", "&cYou have {count} homes but your limit is now {max}. Your homes still work, but delete some with /delhome before setting a new one.");
		m.put("home.deleted", "&aDeleted home &e{home}&a.");
		m.put("home.deleted-other", "&aDeleted &e{player}&a's home &e{home}&a.");
		m.put("home.invalid-name", "&cHome names may only contain lowercase letters, numbers, '_' and '-'.");
		m.put("home.dimension-not-allowed", "&cYou can't set homes in this dimension.");
		m.put("homes.header", "&6Homes &7({count}/{max})&6: ");
		m.put("homes.header-other", "&6{player}'s homes &7({count}/{max})&6: ");
		m.put("homes.entry", "&b{home}");
		m.put("homes.entry-hover", "&7{location}\n&eClick to teleport");
		m.put("homes.separator", "&7, ");
		m.put("homes.none", "&6You have no homes &7(0/{max})&6. Use &e/sethome [name]&6 to set one.");
		m.put("homes.none-other", "&e{player}&6 has no homes.");
		m.put("homes.unlimited", "unlimited");

		// Warps
		m.put("warp.destination", "warp {warp}");
		m.put("warp.not-found", "&cThere is no warp called &e{warp}&c.");
		m.put("warp.no-permission", "&cYou don't have permission to use warp &e{warp}&c.");
		m.put("warp.set", "&aCreated warp &e{warp}&a.");
		m.put("warp.updated", "&aMoved warp &e{warp}&a to your location.");
		m.put("warp.deleted", "&aDeleted warp &e{warp}&a.");
		m.put("warp.invalid-name", "&cWarp names may only contain lowercase letters, numbers, '_' and '-'.");
		m.put("warp.reserved-name", "&cThe name &e{warp}&c is reserved.");
		m.put("warp.sent-other", "&aSent &e{player}&a to warp &e{warp}&a.");
		m.put("warps.header", "&6Warps &7({count})&6: ");
		m.put("warps.entry", "&b{warp}");
		m.put("warps.entry-hover", "&7{location}\n&eClick to teleport");
		m.put("warps.separator", "&7, ");
		m.put("warps.none", "&6There are no warps you can use.");

		// TPA
		m.put("tpa.sent", "&6Request sent to &e{player}&6. It expires in &e{seconds}&6 seconds. ");
		m.put("tpa.received", "&e{player}&6 wants to teleport to you.");
		m.put("tpahere.received", "&e{player}&6 wants you to teleport to them.");
		m.put("tpa.buttons", "{accept} {deny} &7(expires in {seconds}s)");
		m.put("tpa.button-accept", "&a&l[Accept]");
		m.put("tpa.button-accept-hover", "&aClick to accept");
		m.put("tpa.button-deny", "&c&l[Deny]");
		m.put("tpa.button-deny-hover", "&cClick to deny");
		m.put("tpa.button-cancel", "&c[Cancel]");
		m.put("tpa.button-cancel-hover", "&cClick to cancel your request");
		m.put("tpa.hint", "&7Type &e/tpaccept {player}&7 or &e/tpdeny {player}&7.");
		m.put("tpa.self", "&cYou can't send a teleport request to yourself.");
		m.put("tpa.accepted", "&aYou accepted &e{player}&a's teleport request.");
		m.put("tpa.accepted-sender", "&e{player}&a accepted your teleport request.");
		m.put("tpa.denied", "&6You denied &e{player}&6's teleport request.");
		m.put("tpa.denied-sender", "&e{player}&c denied your teleport request.");
		m.put("tpa.cancelled", "&6Cancelled your teleport request to &e{player}&6.");
		m.put("tpa.cancelled-target", "&e{player}&6 cancelled their teleport request.");
		m.put("tpa.expired-sender", "&6Your teleport request to &e{player}&6 expired.");
		m.put("tpa.expired-target", "&6The teleport request from &e{player}&6 expired.");
		m.put("tpa.replaced", "&6Your request to &e{player}&6 was replaced by your new one.");
		m.put("tpa.player-left", "&6The teleport request with &e{player}&6 was cancelled because they left.");
		m.put("tpa.no-pending", "&cYou have no pending teleport requests.");
		m.put("tpa.no-pending-from", "&cYou have no pending teleport request from &e{player}&c.");
		m.put("tpa.no-outgoing", "&cYou have no outgoing teleport requests.");

		// Admin teleports
		m.put("tp.other", "&aTeleported &e{player}&a to &e{target}&a.");
		m.put("tp.notify", "&6You were teleported to &e{target}&6 by &e{sender}&6.");
		m.put("tp.position", "{x}, {y}, {z}");
		m.put("tphere.success", "&aTeleported &e{player}&a to you.");
		m.put("tpall.success", "&aTeleported &e{count}&a players to &e{target}&a.");
		m.put("tpall.none", "&cThere is nobody else to teleport.");

		// Back / top / rtp
		m.put("back.none", "&cYou don't have a previous location.");
		m.put("back.destination", "your previous location");
		m.put("back.death-hint", "&7Use &e/back&7 to return to where you died.");
		m.put("top.destination", "the top");
		m.put("top.none", "&cThere is no block above you to stand on.");
		m.put("rtp.searching", "&6Searching for a safe location...");
		m.put("rtp.failed", "&cCould not find a safe location, try again.");
		m.put("rtp.destination", "{x}, {y}, {z}");
		m.put("rtp.sent-other", "&aSent &e{player}&a to a random location.");

		// Combat
		m.put("combat.tagged", "&cYou are now in combat with &e{player}&c! Don't log out for &e{seconds}&c seconds.");
		m.put("combat.tagged-mob", "&cYou are now in combat! Don't log out for &e{seconds}&c seconds.");
		m.put("combat.untagged", "&aYou are no longer in combat.");
		m.put("combat.actionbar", "&cIn combat: &e{seconds}s");
		m.put("combat.status-tagged", "&cYou are in combat for another &e{seconds}&c seconds.");
		m.put("combat.status-free", "&aYou are not in combat.");
		m.put("combat.logout-broadcast", "&e{player}&c logged out during combat and was killed.");
		m.put("combat.fly-disabled", "&cFlight disabled because you entered combat.");
		m.put("combat.god-disabled", "&cGod mode disabled because you entered combat.");

		// Chat
		m.put("chat.format", "{prefix}{displayname}{suffix}&7: &f{message}");
		m.put("chat.local-format", "&7[L] {prefix}{displayname}{suffix}&7: &f{message}");
		m.put("chat.global-format", "&6[G] {prefix}{displayname}{suffix}&7: &f{message}");
		m.put("chat.nobody-heard", "&7Nobody is close enough to hear you. Start your message with &e{global-prefix}&7 to talk to everyone.");
		m.put("chat.muted", "&cYou are muted. &7Expires: {time}. Reason: {reason}");
		m.put("chat.jailed", "&cYou can't chat while jailed.");
		m.put("chat.frozen", "&cYou can't chat while frozen.");

		// Private messages
		m.put("msg.format-sender", "&7[&eme &7-> &e{receiver}&7] &f{message}");
		m.put("msg.format-receiver", "&7[&e{sender} &7-> &eme&7] &f{message}");
		m.put("msg.format-spy", "&8[Spy] &7{sender} -> {receiver}: {message}");
		m.put("msg.self", "&cYou can't message yourself.");
		m.put("msg.no-reply", "&cYou have nobody to reply to.");
		m.put("msg.target-afk", "&7{player} is AFK and may not respond.");

		// Mail
		m.put("mail.sent", "&aMail sent to &e{player}&a.");
		m.put("mail.sent-all", "&aMail sent to &e{count}&a players.");
		m.put("mail.received", "&6You have new mail from &e{sender}&6. &7Type &e/mail read&7.");
		m.put("mail.unread-join", "&6You have &e{count}&6 unread mail. Type &e/mail read&6.");
		m.put("mail.header", "&6Mail &7(page {page}/{pages})&6:");
		m.put("mail.entry", "&7[{time}] &e{sender}&7: &f{message}");
		m.put("mail.none", "&6You have no mail.");
		m.put("mail.cleared", "&aYour mail was cleared.");
		m.put("mail.inbox-full", "&e{player}&c's mailbox is full.");
		m.put("mail.too-long", "&cMail can be at most &e{max}&c characters long.");
		m.put("mail.spy", "&8[MailSpy] &7{sender} -> {receiver}: {message}");

		// Ignore
		m.put("ignore.added", "&aYou are now ignoring &e{player}&a.");
		m.put("ignore.removed", "&aYou are no longer ignoring &e{player}&a.");
		m.put("ignore.self", "&cYou can't ignore yourself.");
		m.put("ignore.exempt", "&cYou can't ignore &e{player}&c.");
		m.put("ignore.list", "&6Ignored players: &e{players}");
		m.put("ignore.list-empty", "&6You are not ignoring anyone.");

		// Nicknames
		m.put("nick.set", "&aYour nickname is now &r{nick}&a.");
		m.put("nick.set-other", "&aSet &e{player}&a's nickname to &r{nick}&a.");
		m.put("nick.changed-by", "&6Your nickname was changed to &r{nick}&6 by &e{sender}&6.");
		m.put("nick.removed", "&aYour nickname was removed.");
		m.put("nick.removed-other", "&aRemoved &e{player}&a's nickname.");
		m.put("nick.too-long", "&cNicknames can be at most &e{max}&c characters long.");
		m.put("nick.too-short", "&cNicknames must be at least &e{min}&c characters long.");
		m.put("nick.invalid", "&cThat nickname contains characters that aren't allowed.");
		m.put("nick.taken", "&cThe nickname &e{nick}&c is already in use.");
		m.put("realname.result", "&r{nick}&6 is &e{player}&6.");
		m.put("realname.not-found", "&cNobody online has the nickname &e{nick}&c.");

		// Me / broadcast / socialspy
		m.put("me.format", "&5* {displayname} &5{message}");
		m.put("broadcast.format", "&8[&4Broadcast&8] &a{message}");
		m.put("socialspy.enabled", "&aSocial spy enabled.");
		m.put("socialspy.disabled", "&6Social spy disabled.");

		// Player commands
		m.put("heal.healed", "&aYou have been healed.");
		m.put("heal.healed-other", "&aHealed &e{player}&a.");
		m.put("heal.dead", "&cYou can't heal a dead player.");
		m.put("feed.fed", "&aYour hunger has been satisfied.");
		m.put("feed.fed-other", "&aFed &e{player}&a.");
		m.put("fly.enabled", "&aFlight enabled.");
		m.put("fly.disabled", "&6Flight disabled.");
		m.put("fly.enabled-other", "&aEnabled flight for &e{player}&a.");
		m.put("fly.disabled-other", "&6Disabled flight for &e{player}&6.");
		m.put("god.enabled", "&aGod mode enabled.");
		m.put("god.disabled", "&6God mode disabled.");
		m.put("god.enabled-other", "&aEnabled god mode for &e{player}&a.");
		m.put("god.disabled-other", "&6Disabled god mode for &e{player}&6.");
		m.put("speed.set", "&aYour {type} speed is now &e{speed}&a.");
		m.put("speed.set-other", "&aSet &e{player}&a's {type} speed to &e{speed}&a.");
		m.put("speed.type-fly", "fly");
		m.put("speed.type-walk", "walk");
		m.put("gamemode.set", "&aYour game mode is now &e{mode}&a.");
		m.put("gamemode.set-other", "&aSet &e{player}&a's game mode to &e{mode}&a.");
		m.put("gamemode.no-permission-mode", "&cYou don't have permission to use &e{mode}&c.");
		m.put("gamemode.name.survival", "Survival");
		m.put("gamemode.name.creative", "Creative");
		m.put("gamemode.name.adventure", "Adventure");
		m.put("gamemode.name.spectator", "Spectator");
		m.put("afk.now", "&7* {displayname}&7 is now AFK{message}");
		m.put("afk.back", "&7* {displayname}&7 is no longer AFK");
		m.put("afk.message-suffix", ": {message}");
		m.put("afk.kick-reason", "You were kicked for being AFK too long.");
		m.put("afk.actionbar", "&7You are AFK");
		m.put("hat.success", "&aEnjoy your new hat!");
		m.put("hat.removed", "&aYou took off your hat.");
		m.put("hat.empty", "&cHold an item to wear it as a hat.");
		m.put("hat.binding", "&cYou can't remove a hat with Curse of Binding.");
		m.put("repair.hand", "&aRepaired &e{item}&a.");
		m.put("repair.all", "&aRepaired &e{count}&a items.");
		m.put("repair.none", "&cThere is nothing to repair.");
		m.put("repair.not-repairable", "&cThis item can't be repaired.");
		m.put("enderchest.title", "Ender Chest");
		m.put("enderchest.title-other", "{player}'s Ender Chest");
		m.put("workbench.title", "Crafting");
		m.put("anvil.title", "Repair & Name");
		m.put("invsee.title", "{player}'s Inventory");
		m.put("invsee.self", "&cOpen your own inventory with E.");
		m.put("clearinventory.cleared", "&aYour inventory was cleared.");
		m.put("clearinventory.cleared-other", "&aCleared &e{player}&a's inventory.");
		m.put("suicide.done", "&7Goodbye, cruel world.");
		m.put("near.header", "&6Players within &e{radius}&6 blocks: ");
		m.put("near.entry", "{displayname} &7({distance}m)");
		m.put("near.separator", "&7, ");
		m.put("near.none", "&6Nobody is within &e{radius}&6 blocks.");
		m.put("near.radius-too-large", "&cThe maximum radius is &e{max}&c.");
		m.put("seen.online", "&e{player}&6 is online and has been for &e{time}&6.");
		m.put("seen.offline", "&e{player}&6 was last seen &e{time}&6 ago.");
		m.put("whois.body", "&6===== Whois: &e{player}&6 =====\n"
				+ "&6UUID: &f{uuid}\n"
				+ "&6Nickname: &f{nick}\n"
				+ "&6Health: &f{health}/{max-health}  &6Hunger: &f{food}/20\n"
				+ "&6Game mode: &f{gamemode}  &6Fly: &f{fly}  &6God: &f{god}\n"
				+ "&6Location: &f{location}\n"
				+ "&6Balance: &f{balance}\n"
				+ "&6AFK: &f{afk}  &6Vanished: &f{vanished}\n"
				+ "&6Muted: &f{muted}  &6Jailed: &f{jailed}  &6Frozen: &f{frozen}\n"
				+ "&6In combat: &f{combat}\n"
				+ "&6First joined: &f{first-join}\n"
				+ "&6IP: &f{ip}");
		m.put("whois.hidden", "hidden");
		m.put("whois.yes", "yes");
		m.put("whois.no", "no");
		m.put("list.header", "&6There are &e{count}&6 out of &e{max}&6 players online:");
		m.put("list.players", "{players}");
		m.put("list.entry", "{displayname}{afk}{vanished}");
		m.put("list.separator", "&7, ");
		m.put("list.afk-tag", " &7[AFK]");
		m.put("list.vanished-tag", " &8[Hidden]");
		m.put("ping.self", "&6Your ping is &e{ping}&6 ms.");
		m.put("ping.other", "&e{player}&6's ping is &e{ping}&6 ms.");
		m.put("tablist.format", "{displayname}{afk}");
		m.put("tablist.afk-tag", " &7[AFK]");

		// Economy
		m.put("economy.disabled", "&cThe economy is disabled.");
		m.put("balance.self", "&6Balance: &a{balance}");
		m.put("balance.other", "&e{player}&6's balance: &a{balance}");
		m.put("pay.sent", "&aYou sent &e{amount}&a to &e{player}&a.");
		m.put("pay.received", "&aYou received &e{amount}&a from &e{player}&a.");
		m.put("pay.insufficient", "&cYou don't have enough money. Balance: &e{balance}");
		m.put("pay.self", "&cYou can't pay yourself.");
		m.put("pay.invalid-amount", "&cInvalid amount &e{amount}&c.");
		m.put("pay.minimum", "&cThe minimum payment is &e{amount}&c.");
		m.put("pay.offline-disabled", "&cYou can only pay players who are online.");
		m.put("pay.max-balance", "&e{player}&c can't hold that much money.");
		m.put("baltop.header", "&6Top balances &7(page {page}/{pages})&6:");
		m.put("baltop.entry", "&7{rank}. &e{player}&7: &a{balance}");
		m.put("baltop.total", "&6Server total: &a{total}");
		m.put("eco.give", "&aGave &e{amount}&a to &e{player}&a. New balance: &e{balance}");
		m.put("eco.take", "&aTook &e{amount}&a from &e{player}&a. New balance: &e{balance}");
		m.put("eco.set", "&aSet &e{player}&a's balance to &e{balance}&a.");
		m.put("eco.reset", "&aReset &e{player}&a's balance to &e{balance}&a.");
		m.put("eco.insufficient", "&e{player}&c only has &e{balance}&c.");
		m.put("eco.max-balance", "&cThat would put &e{player}&c over the maximum balance of &e{max}&c.");

		// Kits
		m.put("kit.received", "&aYou received kit &e{kit}&a.");
		m.put("kit.given", "&aGave kit &e{kit}&a to &e{player}&a.");
		m.put("kit.not-found", "&cThere is no kit called &e{kit}&c.");
		m.put("kit.no-permission", "&cYou don't have permission to use kit &e{kit}&c.");
		m.put("kit.cooldown", "&cYou can use kit &e{kit}&c again in &e{time}&c.");
		m.put("kit.one-time", "&cYou already claimed kit &e{kit}&c.");
		m.put("kit.inventory-full", "&cYour inventory is too full for kit &e{kit}&c.");
		m.put("kit.dropped", "&6Some items didn't fit and were dropped at your feet.");
		m.put("kits.header", "&6Kits: ");
		m.put("kits.entry", "&b{kit}");
		m.put("kits.entry-hover", "&7{count} items, cooldown {cooldown}\n&eClick to claim");
		m.put("kits.entry-cooldown", "&7{kit} ({time})");
		m.put("kits.separator", "&7, ");
		m.put("kits.none", "&6There are no kits you can use.");
		m.put("kits.one-time", "one time");
		m.put("createkit.created", "&aSaved kit &e{kit}&a with &e{count}&a items and a cooldown of &e{cooldown}&a.");
		m.put("createkit.empty", "&cYour inventory is empty.");
		m.put("createkit.invalid-name", "&cKit names may only contain lowercase letters, numbers, '_' and '-'.");
		m.put("createkit.reserved", "&cThe name &e{kit}&c is reserved.");
		m.put("delkit.deleted", "&aDeleted kit &e{kit}&a.");

		// Moderation
		m.put("mute.muted", "&aMuted &e{player}&a for &e{time}&a. Reason: &f{reason}");
		m.put("mute.notify", "&cYou have been muted for &e{time}&c. Reason: &f{reason}");
		m.put("mute.exempt", "&cYou can't mute &e{player}&c.");
		m.put("mute.expired", "&aYour mute has expired.");
		m.put("unmute.unmuted", "&aUnmuted &e{player}&a.");
		m.put("unmute.notify", "&aYou have been unmuted.");
		m.put("unmute.not-muted", "&e{player}&c is not muted.");
		m.put("tempban.banned", "&aBanned &e{player}&a for &e{time}&a. Reason: &f{reason}");
		m.put("tempban.kick-message", "&cYou are banned for {time}.\n&7Reason: &f{reason}");
		m.put("tempban.exempt", "&cYou can't ban &e{player}&c.");
		m.put("tempban.too-long", "&cThe maximum ban duration is &e{max}&c.");
		m.put("kick.kicked", "&aKicked &e{player}&a. Reason: &f{reason}");
		m.put("kick.default-reason", "Kicked by an operator.");
		m.put("kick.exempt", "&cYou can't kick &e{player}&c.");
		m.put("jail.jailed", "&aJailed &e{player}&a in &e{jail}&a for &e{time}&a. Reason: &f{reason}");
		m.put("jail.notify", "&cYou have been jailed for &e{time}&c. Reason: &f{reason}");
		m.put("jail.time-left", "&cYou are jailed. Time left: &e{time}");
		m.put("jail.released", "&aYou have been released from jail.");
		m.put("jail.unjailed", "&aReleased &e{player}&a from jail.");
		m.put("jail.not-jailed", "&e{player}&c is not jailed.");
		m.put("jail.not-found", "&cThere is no jail called &e{jail}&c.");
		m.put("jail.set", "&aJail &e{jail}&a set to your location.");
		m.put("jail.deleted", "&aDeleted jail &e{jail}&a.");
		m.put("jail.exempt", "&cYou can't jail &e{player}&c.");
		m.put("jail.invalid-name", "&cJail names may only contain lowercase letters, numbers, '_' and '-'.");
		m.put("jail.list", "&6Jails: &e{jails}");
		m.put("jail.list-empty", "&6There are no jails. Create one with &e/setjail <name>&6.");
		m.put("jail.escape", "&cYou can't leave the jail.");
		m.put("jail.no-interact", "&cYou can't do that while jailed.");
		m.put("vanish.enabled", "&aYou are now vanished.");
		m.put("vanish.disabled", "&6You are now visible.");
		m.put("vanish.enabled-other", "&aVanished &e{player}&a.");
		m.put("vanish.disabled-other", "&6Made &e{player}&6 visible.");
		m.put("vanish.actionbar", "&7You are vanished");
		m.put("freeze.frozen", "&aFroze &e{player}&a.");
		m.put("freeze.unfrozen", "&aUnfroze &e{player}&a.");
		m.put("freeze.notify-frozen", "&cYou have been frozen by a staff member. Don't log out.");
		m.put("freeze.notify-unfrozen", "&aYou can move again.");
		m.put("freeze.exempt", "&cYou can't freeze &e{player}&c.");
		m.put("freeze.no-interact", "&cYou can't do that while frozen.");
		m.put("sudo.command", "&aMade &e{player}&a run &e/{command}&a.");
		m.put("sudo.chat", "&aMade &e{player}&a say: &f{message}");
		m.put("sudo.exempt", "&cYou can't use sudo on &e{player}&c.");

		// World
		m.put("world-time.query", "&6The time in &e{world}&6 is &e{time}&6 (&e{ticks}&6 ticks, day &e{day}&6).");
		m.put("world-time.set", "&aSet the time in &e{world}&a to &e{time}&a.");
		m.put("world-time.no-clock", "&cThe world &e{world}&c has no clock, so its time can't change.");
		m.put("world-time.added", "&aAdded &e{ticks}&a ticks in &e{world}&a.");
		m.put("weather.set", "&aSet the weather in &e{world}&a to &e{weather}&a.");
		m.put("weather.name.clear", "clear");
		m.put("weather.name.rain", "rain");
		m.put("weather.name.thunder", "thunder");

		// Join / quit
		m.put("join-quit.join", "&e{displayname}&e joined the game");
		m.put("join-quit.quit", "&e{displayname}&e left the game");
		m.put("join-quit.first-join", "&dWelcome &e{player}&d to the server! &7(player #{count})");
		m.put("motd", "&6Welcome, &e{displayname}&6!\n&7There are &e{online}&7 players online. Type &e/help&7 for commands.");

		return m;
	}
}
