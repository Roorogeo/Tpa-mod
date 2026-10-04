# Messages (`config/essentials/messages.json`)

Every line of text a player can see comes from `messages.json`, except the public chat format, which is in `config.json` (see [Chat](Chat.md#public-chat)). The file is created with every key on
first start; keys added by updates are appended automatically, and your edits are kept. Apply edits with
`/essentials reload`.

## Formatting

| Code | Effect |
|---|---|
| `&0` – `&9`, `&a` – `&f` | Colors (same letters as vanilla section codes) |
| `&#RRGGBB` | Any hex color, e.g. `&#ff8800` |
| `&l` | **Bold** |
| `&o` | *Italic* |
| `&n` | Underline |
| `&m` | Strikethrough |
| `&k` | Obfuscated ("magic") |
| `&r` | Reset to plain white |
| `&&` | A literal `&` |
| `\n` | New line |

A color code resets bold/italic/etc. that came before it, like vanilla. Formats stack (`&c&l` = bold red).

| Color | Code | Color | Code |
|---|---|---|---|
| Black | `&0` | Dark gray | `&8` |
| Dark blue | `&1` | Blue | `&9` |
| Dark green | `&2` | Green | `&a` |
| Dark aqua | `&3` | Aqua | `&b` |
| Dark red | `&4` | Red | `&c` |
| Dark purple | `&5` | Light purple | `&d` |
| Gold | `&6` | Yellow | `&e` |
| Gray | `&7` | White | `&f` |

## Placeholders

Words in braces are replaced: `{player}`, `{time}`, `{home}`... Each message's available placeholders
are listed in the tables below (some messages get more placeholders than their default text uses). Two are special:

- `{tag}` works in **every** message and inserts the `tag` message (the `[Essentials]` prefix).
  Example: `"home.set": "{tag}&aHome &e{home}&a set."`.
- Values coming from players (names, chat text, reasons) are inserted as plain text, so players can't
  inject formatting. Nicknames and chat keep the colors their author was allowed to use.

Placeholders that a message doesn't support are left as written, which makes typos easy to spot. A
message key that doesn't exist shows the key itself.

## Turning messages off

Set a message to an empty string (`""`) and it is never sent. Examples: `"afk.actionbar": ""` removes the
AFK reminder, `"teleport.warmup-actionbar": ""` removes the countdown.

## Clickable messages

These messages become clickable automatically:

| Message | Click action | Hover text |
|---|---|---|
| `homes.entry` | `/home <name>` | `homes.entry-hover` |
| `warps.entry` | `/warp <name>` | `warps.entry-hover` |
| `kits.entry` | `/kit <name>` | `kits.entry-hover` |
| `tpa.button-accept` | `/tpaccept <player>` | `tpa.button-accept-hover` |
| `tpa.button-deny` | `/tpdeny <player>` | `tpa.button-deny-hover` |
| `tpa.button-cancel` | `/tpacancel <player>` | `tpa.button-cancel-hover` |

## Chat format

The public chat format (`format`, `group-formats`, `local-format`, `global-format`) is in `config.json` →
`chat`, not here. See [Chat](Chat.md#public-chat).

## Every message

333 messages.


### Tag

| Key | Default | Placeholders |
|---|---|---|
| `tag` | `&8[&6Essentials&8]&r ` |  |

### General

| Key | Default | Placeholders |
|---|---|---|
| `general.no-permission` | `&cYou don't have permission to do that.` |  |
| `general.player-only` | `&cOnly players can use this command.` |  |
| `general.player-not-found` | `&cPlayer &e{player}&c was not found.` | `{player}` |
| `general.player-never-joined` | `&e{player}&c has never played on this server.` | `{player}` |
| `general.command-cooldown` | `&cYou must wait &e{time}&c before using &e/{command}&c again.` | `{command}`, `{time}` |
| `general.combat-blocked` | `&cYou can't use &e/{command}&c while in combat! &7({time} left)` | `{command}`, `{time}` |
| `general.muted-blocked` | `&cYou can't use &e/{command}&c while muted.` | `{command}` |
| `general.jailed-blocked` | `&cYou can't use &e/{command}&c while jailed.` | `{command}` |
| `general.frozen-blocked` | `&cYou can't use &e/{command}&c while frozen.` | `{command}` |
| `general.invalid-duration` | `&cInvalid duration &e{input}&c. Use e.g. 30s, 10m, 2h, 1d, 1w or permanent.` | `{input}` |
| `general.unknown-world` | `&cUnknown world &e{world}&c.` | `{world}` |
| `general.reloaded` | `&aReloaded config.json, messages.json and kits.json.` |  |
| `general.reload-failed` | `&cReload failed: {error}. Check the server log.` | `{error}` |
| `general.version` | `&6Essentials &e{version}&6. Sub-commands: &e/essentials reload` | `{version}` |
| `general.target-offline` | `&e{player}&c is not online.` | `{player}` |
| `general.no-reason` | `No reason given` |  |
| `general.console-name` | `&dConsole` | — (the console's name in /msg, /reply, /mail and /me) |

### Durations and dates

`time.days`, `time.hours`, `time.minutes` and `time.seconds` use `{n}`. `time.date-format` is a Java [SimpleDateFormat](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/text/SimpleDateFormat.html) pattern used by /mail and /whois.

| Key | Default | Placeholders |
|---|---|---|
| `time.days` | `{n}d` | `{n}` |
| `time.hours` | `{n}h` | `{n}` |
| `time.minutes` | `{n}m` | `{n}` |
| `time.seconds` | `{n}s` | `{n}` |
| `time.separator` | ` ` |  |
| `time.permanent` | `permanent` |  |
| `time.now` | `now` |  |
| `time.date-format` | `yyyy-MM-dd HH:mm` |  |

### Teleporting

| Key | Default | Placeholders |
|---|---|---|
| `teleport.warmup` | `&6Teleporting to &e{destination}&6 in &e{seconds}&6 seconds. Don't move!` | `{destination}`, `{seconds}` |
| `teleport.warmup-actionbar` | `&6Teleporting in &e{seconds}&6...` | `{seconds}` |
| `teleport.cancelled-move` | `&cTeleport cancelled because you moved.` |  |
| `teleport.cancelled-damage` | `&cTeleport cancelled because you took damage.` |  |
| `teleport.cancelled-combat` | `&cTeleport cancelled because you entered combat.` |  |
| `teleport.cancelled-death` | `&cTeleport cancelled because you died.` |  |
| `teleport.cancelled-replaced` | `&cYour pending teleport to &e{destination}&c was replaced.` | `{destination}` |
| `teleport.cooldown` | `&cYou must wait &e{time}&c before teleporting again.` | `{time}` |
| `teleport.success` | `&aTeleported to &e{destination}&a.` | `{destination}` |
| `teleport.unsafe` | `&cThe destination &e{destination}&c is not safe and no safe spot was found nearby.` | `{destination}` |
| `teleport.chunk-timeout` | `&cTeleport cancelled, &e{destination}&c took too long to load.` | `{destination}` |
| `teleport.world-missing` | `&cThe world of &e{destination}&c no longer exists.` | `{destination}` |
| `teleport.destination-gone` | `&cTeleport cancelled, &e{destination}&c is no longer available.` | `{destination}` |
| `teleport.in-combat` | `&cYou can't teleport while in combat! &7({time} left)` | `{time}` |
| `teleport.location-format` | `{x}, {y}, {z} ({world})` | `{world}`, `{x}`, `{y}`, `{z}` |

### Spawn

| Key | Default | Placeholders |
|---|---|---|
| `spawn.destination` | `spawn` |  |
| `spawn.set` | `&aSpawn set to your location.` |  |
| `spawn.sent-other` | `&aSent &e{player}&a to spawn.` | `{player}` |
| `spawn.sent-by` | `&6You were sent to spawn by &e{sender}&6.` | `{sender}` |

### Homes

| Key | Default | Placeholders |
|---|---|---|
| `home.destination` | `home {home}` | `{home}` |
| `home.destination-other` | `{player}'s home {home}` | `{home}`, `{player}` |
| `home.not-found` | `&cYou don't have a home called &e{home}&c.` | `{home}` |
| `home.not-found-other` | `&e{player}&c doesn't have a home called &e{home}&c.` | `{home}`, `{player}` |
| `home.none` | `&cYou don't have any homes. Use &e/sethome [name]&c to set one.` |  |
| `home.set` | `&aHome &e{home}&a set. &7({count}/{max})` | `{count}`, `{home}`, `{max}` |
| `home.updated` | `&aHome &e{home}&a moved to your location.` | `{home}` |
| `home.set-other` | `&aSet home &e{home}&a for &e{player}&a.` | `{home}`, `{player}` |
| `home.limit-reached` | `&cYou have {count}/{max} homes. Delete one with /delhome.` | `{count}`, `{max}` |
| `home.over-limit` | `&cYou have {count} homes but your limit is now {max}. Your homes still work, but delete some with /delhome before setting a new one.` | `{count}`, `{max}` |
| `home.deleted` | `&aDeleted home &e{home}&a.` | `{home}` |
| `home.deleted-other` | `&aDeleted &e{player}&a's home &e{home}&a.` | `{home}`, `{player}` |
| `home.invalid-name` | `&cHome names may only contain lowercase letters, numbers, '_' and '-'.` |  |
| `home.dimension-not-allowed` | `&cYou can't set homes in this dimension.` |  |

### Home list

| Key | Default | Placeholders |
|---|---|---|
| `homes.header` | `&6Homes &7({count}/{max})&6: ` | `{count}`, `{max}` |
| `homes.header-other` | `&6{player}'s homes &7({count}/{max})&6: ` | `{count}`, `{max}`, `{player}` |
| `homes.entry` | `&b{home}` | `{home}`, `{location}` |
| `homes.entry-hover` | `&7{location}\n&eClick to teleport` | `{home}`, `{location}` |
| `homes.separator` | `&7, ` |  |
| `homes.none` | `&6You have no homes &7(0/{max})&6. Use &e/sethome [name]&6 to set one.` | `{max}` |
| `homes.none-other` | `&e{player}&6 has no homes.` | `{player}` |
| `homes.unlimited` | `unlimited` |  |

### Warps

| Key | Default | Placeholders |
|---|---|---|
| `warp.destination` | `warp {warp}` | `{warp}` |
| `warp.not-found` | `&cThere is no warp called &e{warp}&c.` | `{warp}` |
| `warp.no-permission` | `&cYou don't have permission to use warp &e{warp}&c.` | `{warp}` |
| `warp.set` | `&aCreated warp &e{warp}&a.` | `{warp}` |
| `warp.updated` | `&aMoved warp &e{warp}&a to your location.` | `{warp}` |
| `warp.deleted` | `&aDeleted warp &e{warp}&a.` | `{warp}` |
| `warp.invalid-name` | `&cWarp names may only contain lowercase letters, numbers, '_' and '-'.` |  |
| `warp.reserved-name` | `&cThe name &e{warp}&c is reserved.` | `{warp}` |
| `warp.sent-other` | `&aSent &e{player}&a to warp &e{warp}&a.` | `{player}`, `{warp}` |

### Warp list

| Key | Default | Placeholders |
|---|---|---|
| `warps.header` | `&6Warps &7({count})&6: ` | `{count}` |
| `warps.entry` | `&b{warp}` | `{location}`, `{warp}` |
| `warps.entry-hover` | `&7{location}\n&eClick to teleport` | `{location}`, `{warp}` |
| `warps.separator` | `&7, ` |  |
| `warps.none` | `&6There are no warps you can use.` |  |

### Teleport requests

| Key | Default | Placeholders |
|---|---|---|
| `tpa.sent` | `&6Request sent to &e{player}&6. It expires in &e{seconds}&6 seconds. ` | `{player}`, `{seconds}` |
| `tpa.received` | `&e{player}&6 wants to teleport to you.` | `{player}` |
| `tpa.buttons` | `{accept} {deny} &7(expires in {seconds}s)` | `{accept}`, `{deny}`, `{seconds}` |
| `tpa.button-accept` | `&a&l[Accept]` |  |
| `tpa.button-accept-hover` | `&aClick to accept` |  |
| `tpa.button-deny` | `&c&l[Deny]` |  |
| `tpa.button-deny-hover` | `&cClick to deny` |  |
| `tpa.button-cancel` | `&c[Cancel]` |  |
| `tpa.button-cancel-hover` | `&cClick to cancel your request` |  |
| `tpa.hint` | `&7Type &e/tpaccept {player}&7 or &e/tpdeny {player}&7.` | `{player}` |
| `tpa.self` | `&cYou can't send a teleport request to yourself.` |  |
| `tpa.accepted` | `&aYou accepted &e{player}&a's teleport request.` | `{player}` |
| `tpa.accepted-sender` | `&e{player}&a accepted your teleport request.` | `{player}` |
| `tpa.denied` | `&6You denied &e{player}&6's teleport request.` | `{player}` |
| `tpa.denied-sender` | `&e{player}&c denied your teleport request.` | `{player}` |
| `tpa.cancelled` | `&6Cancelled your teleport request to &e{player}&6.` | `{player}` |
| `tpa.cancelled-target` | `&e{player}&6 cancelled their teleport request.` | `{player}` |
| `tpa.expired-sender` | `&6Your teleport request to &e{player}&6 expired.` | `{player}` |
| `tpa.expired-target` | `&6The teleport request from &e{player}&6 expired.` | `{player}` |
| `tpa.replaced` | `&6Your request to &e{player}&6 was replaced by your new one.` | `{player}` |
| `tpa.player-left` | `&6The teleport request with &e{player}&6 was cancelled because they left.` | `{player}` |
| `tpa.no-pending` | `&cYou have no pending teleport requests.` |  |
| `tpa.no-pending-from` | `&cYou have no pending teleport request from &e{player}&c.` | `{player}` |
| `tpa.no-outgoing` | `&cYou have no outgoing teleport requests.` |  |

### Teleport requests (here)

| Key | Default | Placeholders |
|---|---|---|
| `tpahere.received` | `&e{player}&6 wants you to teleport to them.` | `{player}` |

### Staff teleports

| Key | Default | Placeholders |
|---|---|---|
| `tp.other` | `&aTeleported &e{player}&a to &e{target}&a.` | `{player}`, `{target}` |
| `tp.notify` | `&6You were teleported to &e{target}&6 by &e{sender}&6.` | `{sender}`, `{target}` |
| `tp.position` | `{x}, {y}, {z}` | `{x}`, `{y}`, `{z}` |

### /tphere

| Key | Default | Placeholders |
|---|---|---|
| `tphere.success` | `&aTeleported &e{player}&a to you.` | `{player}` |

### /tpall

| Key | Default | Placeholders |
|---|---|---|
| `tpall.success` | `&aTeleported &e{count}&a players to &e{target}&a.` | `{count}`, `{target}` |
| `tpall.none` | `&cThere is nobody else to teleport.` |  |

### /back

| Key | Default | Placeholders |
|---|---|---|
| `back.none` | `&cYou don't have a previous location.` |  |
| `back.destination` | `your previous location` |  |
| `back.death-hint` | `&7Use &e/back&7 to return to where you died.` |  |

### /top

| Key | Default | Placeholders |
|---|---|---|
| `top.destination` | `the top` |  |
| `top.none` | `&cThere is no block above you to stand on.` |  |

### /rtp

| Key | Default | Placeholders |
|---|---|---|
| `rtp.searching` | `&6Searching for a safe location...` |  |
| `rtp.failed` | `&cCould not find a safe location, try again.` |  |
| `rtp.destination` | `{x}, {y}, {z}` | `{x}`, `{y}`, `{z}` |
| `rtp.sent-other` | `&aSent &e{player}&a to a random location.` | `{player}` |

### Combat tagging

| Key | Default | Placeholders |
|---|---|---|
| `combat.tagged` | `&cYou are now in combat with &e{player}&c! Don't log out for &e{seconds}&c seconds.` | `{player}`, `{seconds}` |
| `combat.tagged-mob` | `&cYou are now in combat! Don't log out for &e{seconds}&c seconds.` | `{seconds}` |
| `combat.untagged` | `&aYou are no longer in combat.` |  |
| `combat.actionbar` | `&cIn combat: &e{seconds}s` | `{seconds}` |
| `combat.status-tagged` | `&cYou are in combat for another &e{seconds}&c seconds.` | `{seconds}` |
| `combat.status-free` | `&aYou are not in combat.` |  |
| `combat.logout-broadcast` | `&e{player}&c logged out during combat and was killed.` | `{player}` |
| `combat.fly-disabled` | `&cFlight disabled because you entered combat.` |  |
| `combat.god-disabled` | `&cGod mode disabled because you entered combat.` |  |

### Public chat

| Key | Default | Placeholders |
|---|---|---|
| `chat.nobody-heard` | `&7Nobody is close enough to hear you. Start your message with &e{global-prefix}&7 to talk to everyone.` | `{global-prefix}` |
| `chat.muted` | `&cYou are muted. &7Expires: {time}. Reason: {reason}` | `{reason}`, `{time}` |
| `chat.jailed` | `&cYou can't chat while jailed.` |  |

### Private messages

| Key | Default | Placeholders |
|---|---|---|
| `msg.format-sender` | `&7[&eme &7-> &e{receiver}&7] &f{message}` | `{message}`, `{receiver}`, `{sender}` |
| `msg.format-receiver` | `&7[&e{sender} &7-> &eme&7] &f{message}` | `{message}`, `{receiver}`, `{sender}` |
| `msg.format-spy` | `&8[Spy] &7{sender} -> {receiver}: {message}` | `{message}`, `{receiver}`, `{sender}` |
| `msg.self` | `&cYou can't message yourself.` |  |
| `msg.no-reply` | `&cYou have nobody to reply to.` |  |
| `msg.target-afk` | `&7{player} is AFK and may not respond.` | `{player}` |

### Mail

| Key | Default | Placeholders |
|---|---|---|
| `mail.sent` | `&aMail sent to &e{player}&a.` | `{player}` |
| `mail.sent-all` | `&aMail sent to &e{count}&a players.` | `{count}` |
| `mail.received` | `&6You have new mail from &e{sender}&6. &7Type &e/mail read&7.` | `{sender}` |
| `mail.unread-join` | `&6You have &e{count}&6 unread mail. Type &e/mail read&6.` | `{count}` |
| `mail.header` | `&6Mail &7(page {page}/{pages})&6:` | `{page}`, `{pages}` |
| `mail.entry` | `&7[{time}] &e{sender}&7: &f{message}` | `{message}`, `{sender}`, `{time}` |
| `mail.none` | `&6You have no mail.` |  |
| `mail.cleared` | `&aYour mail was cleared.` |  |
| `mail.inbox-full` | `&e{player}&c's mailbox is full.` | `{player}` |
| `mail.too-long` | `&cMail can be at most &e{max}&c characters long.` | `{max}` |
| `mail.spy` | `&8[MailSpy] &7{sender} -> {receiver}: {message}` | `{message}`, `{receiver}`, `{sender}` |

### Ignore

| Key | Default | Placeholders |
|---|---|---|
| `ignore.added` | `&aYou are now ignoring &e{player}&a.` | `{player}` |
| `ignore.removed` | `&aYou are no longer ignoring &e{player}&a.` | `{player}` |
| `ignore.self` | `&cYou can't ignore yourself.` |  |
| `ignore.exempt` | `&cYou can't ignore &e{player}&c.` | `{player}` |
| `ignore.list` | `&6Ignored players: &e{players}` | `{players}` |
| `ignore.list-empty` | `&6You are not ignoring anyone.` |  |

### Nicknames

| Key | Default | Placeholders |
|---|---|---|
| `nick.set` | `&aYour nickname is now &r{nick}&a.` | `{nick}` |
| `nick.set-other` | `&aSet &e{player}&a's nickname to &r{nick}&a.` | `{nick}`, `{player}` |
| `nick.changed-by` | `&6Your nickname was changed to &r{nick}&6 by &e{sender}&6.` | `{nick}`, `{sender}` |
| `nick.removed` | `&aYour nickname was removed.` | `{player}` |
| `nick.removed-other` | `&aRemoved &e{player}&a's nickname.` | `{player}` |
| `nick.too-long` | `&cNicknames can be at most &e{max}&c characters long.` | `{max}` |
| `nick.too-short` | `&cNicknames must be at least &e{min}&c characters long.` | `{min}` |
| `nick.invalid` | `&cThat nickname contains characters that aren't allowed.` |  |
| `nick.taken` | `&cThe nickname &e{nick}&c is already in use.` | `{nick}` |

### /realname

| Key | Default | Placeholders |
|---|---|---|
| `realname.result` | `&r{nick}&6 is &e{player}&6.` | `{nick}`, `{player}` |
| `realname.not-found` | `&cNobody online has the nickname &e{nick}&c.` | `{nick}` |

### /me

| Key | Default | Placeholders |
|---|---|---|
| `me.format` | `&5* {displayname} &5{message}` | `{displayname}`, `{message}`, `{player}` |

### /broadcast

| Key | Default | Placeholders |
|---|---|---|
| `broadcast.format` | `&8[&4Broadcast&8] &a{message}` | `{message}`, `{sender}` |

### Social spy

| Key | Default | Placeholders |
|---|---|---|
| `socialspy.enabled` | `&aSocial spy enabled.` |  |
| `socialspy.disabled` | `&6Social spy disabled.` |  |

### /heal

| Key | Default | Placeholders |
|---|---|---|
| `heal.healed` | `&aYou have been healed.` |  |
| `heal.healed-other` | `&aHealed &e{player}&a.` | `{player}` |
| `heal.dead` | `&cYou can't heal a dead player.` |  |

### /feed

| Key | Default | Placeholders |
|---|---|---|
| `feed.fed` | `&aYour hunger has been satisfied.` |  |
| `feed.fed-other` | `&aFed &e{player}&a.` | `{player}` |

### /fly

| Key | Default | Placeholders |
|---|---|---|
| `fly.enabled` | `&aFlight enabled.` |  |
| `fly.disabled` | `&6Flight disabled.` |  |
| `fly.enabled-other` | `&aEnabled flight for &e{player}&a.` | `{player}` |
| `fly.disabled-other` | `&6Disabled flight for &e{player}&6.` | `{player}` |

### /god

| Key | Default | Placeholders |
|---|---|---|
| `god.enabled` | `&aGod mode enabled.` |  |
| `god.disabled` | `&6God mode disabled.` |  |
| `god.enabled-other` | `&aEnabled god mode for &e{player}&a.` | `{player}` |
| `god.disabled-other` | `&6Disabled god mode for &e{player}&6.` | `{player}` |

### /speed

| Key | Default | Placeholders |
|---|---|---|
| `speed.set` | `&aYour {type} speed is now &e{speed}&a.` | `{speed}`, `{type}` |
| `speed.set-other` | `&aSet &e{player}&a's {type} speed to &e{speed}&a.` | `{player}`, `{speed}`, `{type}` |
| `speed.type-fly` | `fly` |  |
| `speed.type-walk` | `walk` |  |

### Game modes

`gamemode.name.<mode>` are the names shown for each mode.

| Key | Default | Placeholders |
|---|---|---|
| `gamemode.set` | `&aYour game mode is now &e{mode}&a.` | `{mode}` |
| `gamemode.set-other` | `&aSet &e{player}&a's game mode to &e{mode}&a.` | `{mode}`, `{player}` |
| `gamemode.no-permission-mode` | `&cYou don't have permission to use &e{mode}&c.` | `{mode}` |
| `gamemode.name.survival` | `Survival` |  |
| `gamemode.name.creative` | `Creative` |  |
| `gamemode.name.adventure` | `Adventure` |  |
| `gamemode.name.spectator` | `Spectator` |  |

### AFK

| Key | Default | Placeholders |
|---|---|---|
| `afk.now` | `&7* {displayname}&7 is now AFK{message}` | `{displayname}`, `{message}`, `{player}` |
| `afk.back` | `&7* {displayname}&7 is no longer AFK` | `{displayname}`, `{message}`, `{player}` |
| `afk.message-suffix` | `: {message}` | `{message}` |
| `afk.kick-reason` | `You were kicked for being AFK too long.` |  |
| `afk.actionbar` | `&7You are AFK` |  |

### /hat

| Key | Default | Placeholders |
|---|---|---|
| `hat.success` | `&aEnjoy your new hat!` |  |
| `hat.removed` | `&aYou took off your hat.` |  |
| `hat.empty` | `&cHold an item to wear it as a hat.` |  |
| `hat.binding` | `&cYou can't remove a hat with Curse of Binding.` |  |

### /repair

| Key | Default | Placeholders |
|---|---|---|
| `repair.hand` | `&aRepaired &e{item}&a.` | `{item}` |
| `repair.all` | `&aRepaired &e{count}&a items.` | `{count}` |
| `repair.none` | `&cThere is nothing to repair.` |  |
| `repair.not-repairable` | `&cThis item can't be repaired.` |  |

### /enderchest

| Key | Default | Placeholders |
|---|---|---|
| `enderchest.title` | `Ender Chest` |  |
| `enderchest.title-other` | `{player}'s Ender Chest` | `{player}` |

### /workbench

| Key | Default | Placeholders |
|---|---|---|
| `workbench.title` | `Crafting` |  |

### /anvil

| Key | Default | Placeholders |
|---|---|---|
| `anvil.title` | `Repair & Name` |  |

### /invsee

| Key | Default | Placeholders |
|---|---|---|
| `invsee.title` | `{player}'s Inventory` | `{player}` |
| `invsee.self` | `&cOpen your own inventory with E.` |  |

### /clearinventory

| Key | Default | Placeholders |
|---|---|---|
| `clearinventory.cleared` | `&aYour inventory was cleared.` |  |
| `clearinventory.cleared-other` | `&aCleared &e{player}&a's inventory.` | `{player}` |

### /suicide

| Key | Default | Placeholders |
|---|---|---|
| `suicide.done` | `&7Goodbye, cruel world.` |  |

### /near

| Key | Default | Placeholders |
|---|---|---|
| `near.header` | `&6Players within &e{radius}&6 blocks: ` | `{radius}` |
| `near.entry` | `{displayname} &7({distance}m)` | `{displayname}`, `{distance}`, `{player}` |
| `near.separator` | `&7, ` |  |
| `near.none` | `&6Nobody is within &e{radius}&6 blocks.` | `{radius}` |
| `near.radius-too-large` | `&cThe maximum radius is &e{max}&c.` | `{max}` |

### /seen

| Key | Default | Placeholders |
|---|---|---|
| `seen.online` | `&e{player}&6 is online and has been for &e{time}&6.` | `{player}`, `{time}` |
| `seen.offline` | `&e{player}&6 was last seen &e{time}&6 ago.` | `{player}`, `{time}` |

### /whois

`whois.body` is a multi-line template: rearrange or remove lines freely.

| Key | Default | Placeholders |
|---|---|---|
| `whois.body` | `&6===== Whois: &e{player}&6 =====\n&6UUID: &f{uuid}\n&6Nickname: &f{nick}\n&6Health: &f{health}/{max-health}  &6Hunger: &f{food}/20\n&6Game mode: &f{gamemode}  &6Fly: &f{fly}  &6God: &f{god}\n&6Location: &f{location}\n&6Balance: &f{balance}\n&6AFK: &f{afk}  &6Vanished: &f{vanished}\n&6Muted: &f{muted}  &6Jailed: &f{jailed}  &6Frozen: &f{frozen}\n&6In combat: &f{combat}\n&6First joined: &f{first-join}\n&6IP: &f{ip}` | `{afk}`, `{balance}`, `{combat}`, `{first-join}`, `{fly}`, `{food}`, `{frozen}`, `{gamemode}`, `{god}`, `{health}`, `{ip}`, `{jailed}`, `{location}`, `{max-health}`, `{muted}`, `{nick}`, `{player}`, `{uuid}`, `{vanished}` |
| `whois.hidden` | `hidden` |  |
| `whois.yes` | `yes` |  |
| `whois.no` | `no` |  |

### /list

| Key | Default | Placeholders |
|---|---|---|
| `list.header` | `&6There are &e{count}&6 out of &e{max}&6 players online:` | `{count}`, `{max}` |
| `list.players` | `{players}` | `{players}` |
| `list.entry` | `{displayname}{afk}{vanished}` | `{afk}`, `{displayname}`, `{player}`, `{vanished}` |
| `list.separator` | `&7, ` |  |
| `list.afk-tag` | ` &7[AFK]` |  |
| `list.vanished-tag` | ` &8[Hidden]` |  |

### /ping

| Key | Default | Placeholders |
|---|---|---|
| `ping.self` | `&6Your ping is &e{ping}&6 ms.` | `{ping}` |
| `ping.other` | `&e{player}&6's ping is &e{ping}&6 ms.` | `{ping}`, `{player}` |

### Tab list

Used only when `tab-list.enabled` is true.

| Key | Default | Placeholders |
|---|---|---|
| `tablist.format` | `{displayname}{afk}` | `{afk}`, `{displayname}`, `{player}` |
| `tablist.afk-tag` | ` &7[AFK]` |  |

### Economy

| Key | Default | Placeholders |
|---|---|---|
| `economy.disabled` | `&cThe economy is disabled.` |  |

### /balance

| Key | Default | Placeholders |
|---|---|---|
| `balance.self` | `&6Balance: &a{balance}` | `{balance}` |
| `balance.other` | `&e{player}&6's balance: &a{balance}` | `{balance}`, `{player}` |

### /pay

| Key | Default | Placeholders |
|---|---|---|
| `pay.sent` | `&aYou sent &e{amount}&a to &e{player}&a.` | `{amount}`, `{player}` |
| `pay.received` | `&aYou received &e{amount}&a from &e{player}&a.` | `{amount}`, `{player}` |
| `pay.insufficient` | `&cYou don't have enough money. Balance: &e{balance}` | `{balance}` |
| `pay.self` | `&cYou can't pay yourself.` |  |
| `pay.invalid-amount` | `&cInvalid amount &e{amount}&c.` | `{amount}` |
| `pay.minimum` | `&cThe minimum payment is &e{amount}&c.` | `{amount}` |
| `pay.offline-disabled` | `&cYou can only pay players who are online.` |  |
| `pay.max-balance` | `&e{player}&c can't hold that much money.` | `{player}` |

### /baltop

| Key | Default | Placeholders |
|---|---|---|
| `baltop.header` | `&6Top balances &7(page {page}/{pages})&6:` | `{page}`, `{pages}` |
| `baltop.entry` | `&7{rank}. &e{player}&7: &a{balance}` | `{balance}`, `{player}`, `{rank}` |
| `baltop.total` | `&6Server total: &a{total}` | `{total}` |

### /eco

| Key | Default | Placeholders |
|---|---|---|
| `eco.give` | `&aGave &e{amount}&a to &e{player}&a. New balance: &e{balance}` | `{amount}`, `{balance}`, `{player}` |
| `eco.take` | `&aTook &e{amount}&a from &e{player}&a. New balance: &e{balance}` | `{amount}`, `{balance}`, `{player}` |
| `eco.set` | `&aSet &e{player}&a's balance to &e{balance}&a.` | `{balance}`, `{player}` |
| `eco.reset` | `&aReset &e{player}&a's balance to &e{balance}&a.` | `{balance}`, `{player}` |
| `eco.insufficient` | `&e{player}&c only has &e{balance}&c.` | `{balance}`, `{player}` |
| `eco.max-balance` | `&cThat would put &e{player}&c over the maximum balance of &e{max}&c.` | `{max}`, `{player}` |

### Kits

| Key | Default | Placeholders |
|---|---|---|
| `kit.received` | `&aYou received kit &e{kit}&a.` | `{kit}` |
| `kit.given` | `&aGave kit &e{kit}&a to &e{player}&a.` | `{kit}`, `{player}` |
| `kit.not-found` | `&cThere is no kit called &e{kit}&c.` | `{kit}` |
| `kit.no-permission` | `&cYou don't have permission to use kit &e{kit}&c.` | `{kit}` |
| `kit.cooldown` | `&cYou can use kit &e{kit}&c again in &e{time}&c.` | `{kit}`, `{time}` |
| `kit.one-time` | `&cYou already claimed kit &e{kit}&c.` | `{kit}` |
| `kit.inventory-full` | `&cYour inventory is too full for kit &e{kit}&c.` | `{kit}` |
| `kit.dropped` | `&6Some items didn't fit and were dropped at your feet.` |  |

### Kit list

| Key | Default | Placeholders |
|---|---|---|
| `kits.header` | `&6Kits: ` |  |
| `kits.entry` | `&b{kit}` | `{cooldown}`, `{count}`, `{kit}` |
| `kits.entry-hover` | `&7{count} items, cooldown {cooldown}\n&eClick to claim` | `{cooldown}`, `{count}`, `{kit}` |
| `kits.entry-cooldown` | `&7{kit} ({time})` | `{kit}`, `{time}` |
| `kits.separator` | `&7, ` |  |
| `kits.none` | `&6There are no kits you can use.` |  |
| `kits.one-time` | `one time` |  |

### /createkit

| Key | Default | Placeholders |
|---|---|---|
| `createkit.created` | `&aSaved kit &e{kit}&a with &e{count}&a items and a cooldown of &e{cooldown}&a.` | `{cooldown}`, `{count}`, `{kit}` |
| `createkit.empty` | `&cYour inventory is empty.` |  |
| `createkit.invalid-name` | `&cKit names may only contain lowercase letters, numbers, '_' and '-'.` |  |
| `createkit.reserved` | `&cThe name &e{kit}&c is reserved.` | `{kit}` |

### /delkit

| Key | Default | Placeholders |
|---|---|---|
| `delkit.deleted` | `&aDeleted kit &e{kit}&a.` | `{kit}` |

### Mute

| Key | Default | Placeholders |
|---|---|---|
| `mute.muted` | `&aMuted &e{player}&a for &e{time}&a. Reason: &f{reason}` | `{player}`, `{reason}`, `{time}` |
| `mute.notify` | `&cYou have been muted for &e{time}&c. Reason: &f{reason}` | `{reason}`, `{time}` |
| `mute.exempt` | `&cYou can't mute &e{player}&c.` | `{player}` |
| `mute.expired` | `&aYour mute has expired.` |  |

### Unmute

| Key | Default | Placeholders |
|---|---|---|
| `unmute.unmuted` | `&aUnmuted &e{player}&a.` | `{player}` |
| `unmute.notify` | `&aYou have been unmuted.` |  |
| `unmute.not-muted` | `&e{player}&c is not muted.` | `{player}` |

### /tempban

| Key | Default | Placeholders |
|---|---|---|
| `tempban.banned` | `&aBanned &e{player}&a for &e{time}&a. Reason: &f{reason}` | `{player}`, `{reason}`, `{time}` |
| `tempban.kick-message` | `&cYou are banned for {time}.\n&7Reason: &f{reason}` | `{reason}`, `{time}` |
| `tempban.exempt` | `&cYou can't ban &e{player}&c.` | `{player}` |
| `tempban.too-long` | `&cThe maximum ban duration is &e{max}&c.` | `{max}` |

### /kick

| Key | Default | Placeholders |
|---|---|---|
| `kick.kicked` | `&aKicked &e{player}&a. Reason: &f{reason}` | `{player}`, `{reason}` |
| `kick.default-reason` | `Kicked by an operator.` |  |
| `kick.exempt` | `&cYou can't kick &e{player}&c.` | `{player}` |

### Jails

| Key | Default | Placeholders |
|---|---|---|
| `jail.jailed` | `&aJailed &e{player}&a in &e{jail}&a for &e{time}&a. Reason: &f{reason}` | `{jail}`, `{player}`, `{reason}`, `{time}` |
| `jail.notify` | `&cYou have been jailed for &e{time}&c. Reason: &f{reason}` | `{reason}`, `{time}` |
| `jail.time-left` | `&cYou are jailed. Time left: &e{time}` | `{time}` |
| `jail.released` | `&aYou have been released from jail.` |  |
| `jail.unjailed` | `&aReleased &e{player}&a from jail.` | `{player}` |
| `jail.not-jailed` | `&e{player}&c is not jailed.` | `{player}` |
| `jail.not-found` | `&cThere is no jail called &e{jail}&c.` | `{jail}` |
| `jail.set` | `&aJail &e{jail}&a set to your location.` | `{jail}` |
| `jail.deleted` | `&aDeleted jail &e{jail}&a.` | `{jail}` |
| `jail.exempt` | `&cYou can't jail &e{player}&c.` | `{player}` |
| `jail.invalid-name` | `&cJail names may only contain lowercase letters, numbers, '_' and '-'.` |  |
| `jail.list` | `&6Jails: &e{jails}` | `{jails}` |
| `jail.list-empty` | `&6There are no jails. Create one with &e/setjail <name>&6.` |  |
| `jail.escape` | `&cYou can't leave the jail.` |  |
| `jail.no-interact` | `&cYou can't do that while jailed.` |  |

### Vanish

| Key | Default | Placeholders |
|---|---|---|
| `vanish.enabled` | `&aYou are now vanished.` |  |
| `vanish.disabled` | `&6You are now visible.` |  |
| `vanish.enabled-other` | `&aVanished &e{player}&a.` | `{player}` |
| `vanish.disabled-other` | `&6Made &e{player}&6 visible.` | `{player}` |
| `vanish.actionbar` | `&7You are vanished` |  |

### Freeze

| Key | Default | Placeholders |
|---|---|---|
| `freeze.frozen` | `&aFroze &e{player}&a.` | `{player}` |
| `freeze.unfrozen` | `&aUnfroze &e{player}&a.` | `{player}` |
| `freeze.notify-frozen` | `&cYou have been frozen by a staff member. Don't log out.` |  |
| `freeze.notify-unfrozen` | `&aYou can move again.` |  |
| `freeze.exempt` | `&cYou can't freeze &e{player}&c.` | `{player}` |
| `freeze.no-interact` | `&cYou can't do that while frozen.` |  |

### /sudo

| Key | Default | Placeholders |
|---|---|---|
| `sudo.command` | `&aMade &e{player}&a run &e/{command}&a.` | `{command}`, `{player}` |
| `sudo.chat` | `&aMade &e{player}&a say: &f{message}` | `{message}`, `{player}` |
| `sudo.exempt` | `&cYou can't use sudo on &e{player}&c.` | `{player}` |

### Time

| Key | Default | Placeholders |
|---|---|---|
| `world-time.query` | `&6The time in &e{world}&6 is &e{time}&6 (&e{ticks}&6 ticks, day &e{day}&6).` | `{day}`, `{ticks}`, `{time}`, `{world}` |
| `world-time.set` | `&aSet the time in &e{world}&a to &e{time}&a.` | `{time}`, `{world}` |
| `world-time.no-clock` | `&cThe world &e{world}&c has no clock, so its time can't change.` | `{world}` |
| `world-time.added` | `&aAdded &e{ticks}&a ticks in &e{world}&a.` | `{ticks}`, `{world}` |

### Weather

`weather.name.<type>` are the names shown for each weather type.

| Key | Default | Placeholders |
|---|---|---|
| `weather.set` | `&aSet the weather in &e{world}&a to &e{weather}&a.` | `{weather}`, `{world}` |
| `weather.name.clear` | `clear` |  |
| `weather.name.rain` | `rain` |  |
| `weather.name.thunder` | `thunder` |  |

### Join and quit

| Key | Default | Placeholders |
|---|---|---|
| `join-quit.join` | `&e{displayname}&e joined the game` | `{displayname}`, `{player}` |
| `join-quit.quit` | `&e{displayname}&e left the game` | `{displayname}`, `{player}` |
| `join-quit.first-join` | `&dWelcome &e{player}&d to the server! &7(player #{count})` | `{count}`, `{displayname}`, `{player}` |

### MOTD

| Key | Default | Placeholders |
|---|---|---|
| `motd` | `&6Welcome, &e{displayname}&6!\n&7There are &e{online}&7 players online. Type &e/help&7 for commands.` | `{displayname}`, `{online}`, `{player}` |
