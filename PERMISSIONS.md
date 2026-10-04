# Permission nodes

<!-- Generated from src/main/java/com/roorogeo/essentials/perm/PermissionNodes.java. Do not edit by hand. -->

Every command and every sub-feature of a command is gated by its own node, checked through
[fabric-permissions-api](https://github.com/lucko/fabric-permissions-api), so LuckPerms (or any other
permission mod using that API) controls everything. Commands you lack the node for are hidden from
the command tree, so they do not tab-complete.

## Defaults

The **Default** column is what happens when no permission mod is installed, or the permission mod has
no value for the node:

| Default | Meaning |
|---|---|
| `all` | Everyone |
| `op` | Operators with at least `permissions.op-level` (default `2`). The console always passes. |
| `none` | Nobody (only the console). Grant it with a permission mod. |

Every default can be changed in `config/essentials/config.json`:

```json
"permissions": {
  "op-level": 2,
  "defaults": {
    "essentials.nick": "all",
    "essentials.kit.*": "op",
    "essentials.kit.starter": "all",
    "essentials.heal": "op:3"
  }
}
```

Accepted values: `all` (or `true`), `op`, `op:<level>`, `none` (or `false`). For pattern nodes the
most specific key wins: `essentials.kit.starter` beats `essentials.kit.*`, which beats the built-in default.

Placeholders: `<name>` is a warp or kit name, `<number>` is a whole number.

## Teleport

| Node | Default | Description |
|---|---|---|
| `essentials.spawn` | `all` | Use /spawn to teleport to the server spawn. |
| `essentials.spawn.others` | `op` | Use /spawn <player> to send another player to spawn. |
| `essentials.setspawn` | `op` | Use /setspawn to set the server spawn to your position. |
| `essentials.home` | `all` | Use /home [name] to teleport to one of your homes. |
| `essentials.home.others` | `op` | Use /home <player>:<home> to teleport to another player's home. |
| `essentials.sethome` | `all` | Use /sethome [name] to set a home. |
| `essentials.sethome.others` | `op` | Use /sethome <player>:<home> to set a home for another player (ignores their limit). |
| `essentials.sethome.multiple.<number>` | `none` | Raise the home limit to <number>. The highest granted number wins, then it is clamped to homes.absolute-max-homes. |
| `essentials.sethome.unlimited` | `op` | Ignore per-group home limits. homes.absolute-max-homes still applies unless it is -1. |
| `essentials.delhome` | `all` | Use /delhome <name> to delete one of your homes. |
| `essentials.delhome.others` | `op` | Use /delhome <player>:<home> to delete another player's home. |
| `essentials.homes` | `all` | Use /homes to list your homes and see used/max. |
| `essentials.homes.others` | `op` | Use /homes <player> to list another player's homes. |
| `essentials.warp` | `all` | Use /warp <name> to teleport to a warp. |
| `essentials.warp.<name>` | `all` | Use the warp called <name>. Deny it to lock a single warp. |
| `essentials.warp.others` | `op` | Use /warp <name> <player> to send another player to a warp. |
| `essentials.warps` | `all` | Use /warps to list the warps you can use. |
| `essentials.setwarp` | `op` | Use /setwarp <name> to create or move a warp. |
| `essentials.delwarp` | `op` | Use /delwarp <name> to delete a warp. |
| `essentials.tpa` | `all` | Use /tpa <player> to ask to teleport to a player. |
| `essentials.tpahere` | `all` | Use /tpahere <player> to ask a player to teleport to you. |
| `essentials.tpaccept` | `all` | Use /tpaccept [player] to accept a teleport request. |
| `essentials.tpdeny` | `all` | Use /tpdeny [player] to deny a teleport request. |
| `essentials.tpacancel` | `all` | Use /tpacancel [player] to cancel your outgoing requests. |
| `essentials.tp` | `op` | Use /tp <player> to teleport yourself to a player instantly. |
| `essentials.tp.others` | `op` | Use /tp <player> <target> to teleport one player to another. |
| `essentials.tp.position` | `op` | Use /tp [player] <x> <y> <z> to teleport to coordinates. |
| `essentials.tphere` | `op` | Use /tphere <player> to pull a player to you. |
| `essentials.tpall` | `op` | Use /tpall [player] to pull every online player to you (or to <player>). |
| `essentials.back` | `all` | Use /back to return to your previous location. |
| `essentials.back.ondeath` | `all` | Your death location is recorded for /back. |
| `essentials.top` | `op` | Use /top to teleport to the highest block above you. |
| `essentials.rtp` | `all` | Use /rtp to teleport to a random safe location. |
| `essentials.rtp.others` | `op` | Use /rtp <player> to send another player to a random location. |
| `essentials.teleport.cooldown.bypass` | `op` | Skip the teleport cooldown. |
| `essentials.teleport.delay.bypass` | `op` | Skip the teleport warmup delay. |
| `essentials.teleport.safety.bypass` | `op` | Skip the safe-destination check and teleport even into unsafe spots. |

## Chat

| Node | Default | Description |
|---|---|---|
| `essentials.msg` | `all` | Use /msg <player> <message> to send a private message. |
| `essentials.msg.color` | `op` | Use & color codes in private messages. |
| `essentials.reply` | `all` | Use /reply <message> to answer the last private message. |
| `essentials.mail` | `all` | Use /mail (base node; each sub-command has its own node below). |
| `essentials.mail.read` | `all` | Use /mail read [page]. |
| `essentials.mail.send` | `all` | Use /mail send <player> <message>, also to offline players. |
| `essentials.mail.sendall` | `op` | Use /mail sendall <message> to mail every known player. |
| `essentials.mail.clear` | `all` | Use /mail clear to delete your mail. |
| `essentials.ignore` | `all` | Use /ignore <player> to hide a player's chat, messages, mail and teleport requests. |
| `essentials.ignore.exempt` | `op` | Cannot be ignored by other players. |
| `essentials.nick` | `op` | Use /nick <nickname\|off> to change your display name. |
| `essentials.nick.others` | `op` | Use /nick <nickname\|off> <player> to change another player's nickname. |
| `essentials.nick.color` | `op` | Use & color codes (0-9, a-f and &#RRGGBB) in nicknames. |
| `essentials.nick.format` | `op` | Use &l &m &n &o &r formatting codes in nicknames. |
| `essentials.nick.magic` | `op` | Use the &k obfuscated code in nicknames. |
| `essentials.realname` | `all` | Use /realname <nickname> to find who is behind a nickname. |
| `essentials.me` | `all` | Use /me <action>. |
| `essentials.broadcast` | `op` | Use /broadcast <message>. |
| `essentials.socialspy` | `op` | Use /socialspy to see other players' private messages and mail. |
| `essentials.chat.color` | `op` | Use & color codes (0-9, a-f and &#RRGGBB) in public chat. |
| `essentials.chat.format` | `op` | Use &l &m &n &o &r formatting codes in public chat. |
| `essentials.chat.magic` | `op` | Use the &k obfuscated code in public chat. |
| `essentials.chat.group.<name>` | `none` | Chat with the format chat.group-formats.<name> from config.json. The first group in config order that a player has wins. |

## Player

| Node | Default | Description |
|---|---|---|
| `essentials.heal` | `op` | Use /heal to restore your health, hunger and remove fire. |
| `essentials.heal.others` | `op` | Use /heal <player>. |
| `essentials.feed` | `op` | Use /feed to fill your hunger. |
| `essentials.feed.others` | `op` | Use /feed <player>. |
| `essentials.fly` | `op` | Use /fly to toggle flight. |
| `essentials.fly.others` | `op` | Use /fly <player>. |
| `essentials.god` | `op` | Use /god to toggle invulnerability. |
| `essentials.god.others` | `op` | Use /god <player>. |
| `essentials.speed` | `op` | Use /speed <0-10> (applies to flying or walking, whichever you are doing). |
| `essentials.speed.fly` | `op` | Change fly speed (/speed <n> fly). |
| `essentials.speed.walk` | `op` | Change walk speed (/speed <n> walk). |
| `essentials.speed.others` | `op` | Use /speed <n> <fly\|walk> <player>. |
| `essentials.gamemode` | `op` | Use /gamemode and the /gmc /gms /gma /gmsp shortcuts (each mode also needs its own node). |
| `essentials.gamemode.survival` | `op` | Switch to survival. |
| `essentials.gamemode.creative` | `op` | Switch to creative. |
| `essentials.gamemode.adventure` | `op` | Switch to adventure. |
| `essentials.gamemode.spectator` | `op` | Switch to spectator. |
| `essentials.gamemode.others` | `op` | Change another player's game mode. |
| `essentials.afk` | `all` | Use /afk [message] to toggle AFK. |
| `essentials.afk.others` | `op` | Use /afk <player> to toggle another player's AFK status. |
| `essentials.afk.kickexempt` | `op` | Never kicked for being AFK too long. |
| `essentials.hat` | `all` | Use /hat to wear the item in your hand. |
| `essentials.repair` | `op` | Use /repair [hand] to repair the item in your hand. |
| `essentials.repair.all` | `op` | Use /repair all to repair your whole inventory. |
| `essentials.enderchest` | `op` | Use /enderchest to open your ender chest anywhere. |
| `essentials.enderchest.others` | `op` | Use /enderchest <player> to view another player's ender chest. |
| `essentials.enderchest.modify` | `op` | Take and put items when viewing another player's ender chest. |
| `essentials.workbench` | `op` | Use /workbench to open a crafting table anywhere. |
| `essentials.anvil` | `op` | Use /anvil to open an anvil anywhere. |
| `essentials.invsee` | `op` | Use /invsee <player> to view a player's inventory. |
| `essentials.invsee.modify` | `op` | Take and put items with /invsee. |
| `essentials.clearinventory` | `op` | Use /clearinventory to empty your inventory. |
| `essentials.clearinventory.others` | `op` | Use /clearinventory <player>. |
| `essentials.suicide` | `all` | Use /suicide. |
| `essentials.near` | `op` | Use /near to list nearby players within near.default-radius. |
| `essentials.near.radius` | `op` | Use /near <radius> with a custom radius (up to near.max-radius). |
| `essentials.seen` | `all` | Use /seen <player> to see when a player was last online. |
| `essentials.whois` | `op` | Use /whois <player> to see detailed player information. |
| `essentials.whois.ip` | `op` | See IP addresses in /whois. |
| `essentials.list` | `all` | Use /list to see online players. |
| `essentials.ping` | `all` | Use /ping to see your latency. |
| `essentials.ping.others` | `op` | Use /ping <player>. |

## Combat

| Node | Default | Description |
|---|---|---|
| `essentials.combat.check` | `all` | Use /combat to see your remaining combat tag time. |
| `essentials.combat.bypass` | `op` | Never tagged as in combat. |
| `essentials.combat.command.bypass` | `op` | Use commands from combat.blocked-commands while tagged. |

## Economy

| Node | Default | Description |
|---|---|---|
| `essentials.balance` | `all` | Use /balance to see your balance. |
| `essentials.balance.others` | `all` | Use /balance <player>. |
| `essentials.pay` | `all` | Use /pay <player> <amount>. |
| `essentials.baltop` | `all` | Use /baltop [page]. |
| `essentials.eco` | `op` | Use /eco (each sub-command has its own node below). |
| `essentials.eco.give` | `op` | Use /eco give <player> <amount>. |
| `essentials.eco.take` | `op` | Use /eco take <player> <amount>. |
| `essentials.eco.set` | `op` | Use /eco set <player> <amount>. |
| `essentials.eco.reset` | `op` | Use /eco reset <player> to restore the starting balance. |

## Kits

| Node | Default | Description |
|---|---|---|
| `essentials.kit` | `all` | Use /kit <name>. |
| `essentials.kit.<name>` | `all` | Claim the kit called <name>. Deny it (or set its default to op) to restrict a kit. |
| `essentials.kit.others` | `op` | Use /kit <name> <player> to give a kit to someone else. |
| `essentials.kit.cooldown.bypass` | `op` | Ignore kit cooldowns and one-time limits. |
| `essentials.kits` | `all` | Use /kits to list the kits you can claim. |
| `essentials.createkit` | `op` | Use /createkit <name> <cooldown> to save your inventory as a kit. |
| `essentials.delkit` | `op` | Use /delkit <name>. |

## Moderation

| Node | Default | Description |
|---|---|---|
| `essentials.mute` | `op` | Use /mute <player> [duration] [reason]. |
| `essentials.mute.exempt` | `op` | Cannot be muted. |
| `essentials.unmute` | `op` | Use /unmute <player>. |
| `essentials.tempban` | `op` | Use /tempban <player> <duration> [reason]. |
| `essentials.tempban.exempt` | `op` | Cannot be temp-banned (checked while online). |
| `essentials.kick` | `op` | Use /kick <player> [reason]. |
| `essentials.kick.exempt` | `op` | Cannot be kicked with /kick. |
| `essentials.jail` | `op` | Use /jail <player> <jail> [duration] [reason]. |
| `essentials.jails` | `op` | Use /jails to list jails. |
| `essentials.jail.exempt` | `op` | Cannot be jailed while online. |
| `essentials.setjail` | `op` | Use /setjail <name> to create or move a jail. |
| `essentials.deljail` | `op` | Use /deljail <name>. |
| `essentials.unjail` | `op` | Use /unjail <player>. |
| `essentials.vanish` | `op` | Use /vanish to hide from other players. |
| `essentials.vanish.others` | `op` | Use /vanish <player>. |
| `essentials.vanish.see` | `op` | See vanished players in the world, tab list, /list and /near. |
| `essentials.freeze` | `op` | Use /freeze <player> to toggle freezing a player in place. |
| `essentials.freeze.exempt` | `op` | Cannot be frozen. |
| `essentials.sudo` | `op` | Use /sudo <player> <command\|c:message> to run a command or chat as another player. |
| `essentials.sudo.exempt` | `op` | Cannot be targeted by /sudo. |

## World

| Node | Default | Description |
|---|---|---|
| `essentials.time` | `all` | Use /time to see the current time. |
| `essentials.time.set` | `op` | Use /time set <day\|noon\|night\|midnight\|ticks> [world]. |
| `essentials.time.add` | `op` | Use /time add <ticks> [world]. |
| `essentials.day` | `op` | Use /day [world]. |
| `essentials.night` | `op` | Use /night [world]. |
| `essentials.weather` | `op` | Use /weather <clear\|rain\|thunder> [duration] (each type also needs its own node). |
| `essentials.weather.clear` | `op` | Set clear weather. |
| `essentials.weather.rain` | `op` | Set rain. |
| `essentials.weather.thunder` | `op` | Set a thunderstorm. |
| `essentials.sun` | `op` | Use /sun [duration]. |
| `essentials.rain` | `op` | Use /rain [duration]. |

## Admin

| Node | Default | Description |
|---|---|---|
| `essentials.essentials` | `op` | Use /essentials (shows version and sub-commands). |
| `essentials.reload` | `op` | Use /essentials reload to reload config.json, messages.json and kits.json. |
| `essentials.command.cooldown.bypass` | `op` | Ignore per-command cooldowns set in commands.<name>.cooldown-seconds. |

## LuckPerms examples

```
# Let the default group use /nick with colors
/lp group default permission set essentials.nick true
/lp group default permission set essentials.nick.color true

# Give VIPs 10 homes (clamped to homes.absolute-max-homes)
/lp group vip permission set essentials.sethome.multiple.10 true

# Lock the 'vip' kit and the 'arena' warp for everyone except VIPs
/lp group default permission set essentials.kit.vip false
/lp group vip permission set essentials.kit.vip true
/lp group default permission set essentials.warp.arena false

# Staff: no teleport warmup or cooldown, can use commands while in combat
/lp group staff permission set essentials.teleport.delay.bypass true
/lp group staff permission set essentials.teleport.cooldown.bypass true
/lp group staff permission set essentials.combat.command.bypass true
```
