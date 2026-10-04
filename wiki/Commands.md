# Commands

Syntax: `<required>`, `[optional]`, `a|b` = one of. Aliases are the defaults from `config.json` →
`commands.<name>.aliases`; any command can be disabled, aliased differently or given a cooldown there
(see [Configuration](Configuration.md#commands)). Players only see commands and arguments they have
the permission for.

Commands marked **replaces vanilla** remove the vanilla command of the same name while enabled.

## Teleport

| Command | Aliases | Permission | Description |
|---|---|---|---|
| `/spawn` | | `essentials.spawn` | Teleport to spawn. |
| `/spawn <player>` | | `essentials.spawn.others` | Send a player to spawn (instant). |
| `/setspawn` | | `essentials.setspawn` | Set spawn (also sets the vanilla world spawn). |
| `/home [name]` | h | `essentials.home` | Teleport to a home. No name: the default home, the only home, or the list. |
| `/home <player>:<name>` | | `essentials.home.others` | Teleport to another player's home (online or offline). |
| `/sethome [name]` | | `essentials.sethome` | Set or move a home. Limits apply to new homes only. |
| `/sethome <player>:<name>` | | `essentials.sethome.others` | Set a home for another player (no limit). |
| `/delhome <name>` | remhome, rmhome | `essentials.delhome` | Delete a home. |
| `/delhome <player>:<name>` | | `essentials.delhome.others` | Delete another player's home. |
| `/homes` | | `essentials.homes` | Clickable list with used/max. |
| `/homes <player>` | | `essentials.homes.others` | Another player's homes. |
| `/warp` | | `essentials.warp` | List warps. |
| `/warp <name>` | | `essentials.warp` + `essentials.warp.<name>` | Teleport to a warp. |
| `/warp <name> <player>` | | `essentials.warp.others` | Send a player to a warp (instant). |
| `/warps` | | `essentials.warps` | Clickable list of warps you can use. |
| `/setwarp <name>` | createwarp | `essentials.setwarp` | Create or move a warp. |
| `/delwarp <name>` | remwarp, rmwarp | `essentials.delwarp` | Delete a warp. |
| `/tpa <player>` | tpask, call | `essentials.tpa` | Ask to teleport to a player. |
| `/tpahere <player>` | | `essentials.tpahere` | Ask a player to teleport to you. |
| `/tpaccept [player]` | tpyes | `essentials.tpaccept` | Accept the newest request, or the one from a player. |
| `/tpdeny [player]` | tpno | `essentials.tpdeny` | Deny the newest request, or the one from a player. |
| `/tpacancel [player]` | tpcancel | `essentials.tpacancel` | Cancel all your outgoing requests, or the one to a player. |
| `/tp <player>` | tpo | `essentials.tp` | Teleport to a player (instant). **Replaces vanilla /tp** (`/teleport` stays vanilla). |
| `/tp <player> <target>` | | `essentials.tp.others` | Teleport one player to another. |
| `/tp <x> <y> <z>` | | `essentials.tp.position` | Teleport to coordinates (`~` works). |
| `/tp <player> <x> <y> <z>` | | `essentials.tp.others` + `essentials.tp.position` | Teleport a player to coordinates. |
| `/tphere <player>` | s, tpohere | `essentials.tphere` | Pull a player to you. |
| `/tpall [player]` | | `essentials.tpall` | Pull every player to you, or to a player. |
| `/back` | | `essentials.back` | Return to your last location (before a teleport, or your death with `essentials.back.ondeath`). |
| `/top` | | `essentials.top` | Teleport onto the highest block above you. |
| `/rtp` | wild, randomtp | `essentials.rtp` | Random safe location (own 5-minute cooldown by default). |
| `/rtp <player>` | | `essentials.rtp.others` | Send a player to a random location. |

## Chat

| Command | Aliases | Permission | Description |
|---|---|---|---|
| `/msg <player> <message>` | tell, w, m, t, whisper, pm | `essentials.msg` | Private message. Colors with `essentials.msg.color`. **Replaces vanilla /msg, /tell, /w**. |
| `/reply <message>` | r | `essentials.reply` | Reply to your last conversation (also works with the console). |
| `/mail` | email | `essentials.mail` | Same as `/mail read`. |
| `/mail read [page]` | | `essentials.mail.read` | Read your mail (marks it read). |
| `/mail send <player> <message>` | | `essentials.mail.send` | Mail a player, online or offline. |
| `/mail sendall <message>` | | `essentials.mail.sendall` | Mail everyone who ever joined. |
| `/mail clear` | | `essentials.mail.clear` | Delete your mail. |
| `/ignore` | unignore | `essentials.ignore` | List who you ignore. |
| `/ignore <player>` | | `essentials.ignore` | Toggle ignoring a player (chat, /me, /msg, /mail, /tpa). Can't ignore `essentials.ignore.exempt`. |
| `/nick <nickname>` | nickname | `essentials.nick` | Set your nickname. `off` (or your real name) removes it. Codes need `essentials.nick.color` / `.format` / `.magic`. |
| `/nick <player> <nickname>` | | `essentials.nick.others` | Set another player's nickname. |
| `/realname <nickname>` | | `essentials.realname` | Who is behind a nickname (partial match). |
| `/me <action>` | action | `essentials.me` | Emote. **Replaces vanilla /me**. |
| `/broadcast <message>` | bc, bcast | `essentials.broadcast` | Server-wide announcement (& codes allowed). |
| `/socialspy` | spy | `essentials.socialspy` | Toggle seeing private messages and mail of others. |

## Player

| Command | Aliases | Permission | Description |
|---|---|---|---|
| `/heal [player]` | | `essentials.heal` / `.others` | Health, hunger, fire, harmful effects. |
| `/feed [player]` | eat | `essentials.feed` / `.others` | Hunger and saturation. |
| `/fly [player]` | | `essentials.fly` / `.others` | Toggle flight. |
| `/god [player]` | godmode, tgm | `essentials.god` / `.others` | Toggle invulnerability. |
| `/speed <0-10>` | | `essentials.speed` + `.fly` or `.walk` | Set the speed of what you're doing (flying or walking). |
| `/speed <0-10> fly\|walk [player]` | | `essentials.speed.fly` / `.walk`, `.others` | Set a specific speed. |
| `/gamemode <mode> [player]` | gm | `essentials.gamemode` + `essentials.gamemode.<mode>` (+ `.others`) | Modes: `survival s 0`, `creative c 1`, `adventure a 2`, `spectator sp 3`. **Replaces vanilla /gamemode**. |
| `/gmc /gms /gma /gmsp [player]` | | `essentials.gamemode` + mode node (+ `.others`) | Shortcuts. |
| `/afk [message]` | away | `essentials.afk` | Toggle AFK, optionally with a message. |
| `/afk <player>` | | `essentials.afk.others` | Toggle another player's AFK. |
| `/hat` | head | `essentials.hat` | Wear the held item; empty hand takes the hat off. |
| `/repair [hand]` | fix | `essentials.repair` | Repair the held item. |
| `/repair all` | | `essentials.repair.all` | Repair everything you carry. |
| `/enderchest` | ec, echest | `essentials.enderchest` | Open your ender chest. |
| `/enderchest <player>` | | `essentials.enderchest.others` (+ `.modify` to edit) | View someone's ender chest. |
| `/workbench` | wb, craft | `essentials.workbench` | Crafting table anywhere. |
| `/anvil` | | `essentials.anvil` | Anvil anywhere (never breaks). |
| `/invsee <player>` | | `essentials.invsee` (+ `.modify` to edit) | View inventory, armor and offhand. |
| `/clearinventory [player]` | ci, clearinvent | `essentials.clearinventory` / `.others` | Empty an inventory. |
| `/suicide` | | `essentials.suicide` | Die. |
| `/near [radius]` | nearby | `essentials.near` (+ `.radius` for a custom radius) | Nearby players, closest first. |
| `/seen <player>` | | `essentials.seen` | Online time or last seen. |
| `/whois <player>` | | `essentials.whois` (+ `.ip`) | Everything about a player. |
| `/list` | online, who, playerlist | `essentials.list` | Online players. **Replaces vanilla /list**. |
| `/ping [player]` | pong | `essentials.ping` / `.others` | Latency. |
| `/combat` | combattag, ct | `essentials.combat.check` | Combat tag time left. |

## Economy

| Command | Aliases | Permission | Description |
|---|---|---|---|
| `/balance [player]` | bal, money | `essentials.balance` / `.others` | Show a balance. |
| `/pay <player> <amount>` | | `essentials.pay` | Pay a player (offline too, if `economy.pay-offline`). |
| `/baltop [page]` | balancetop | `essentials.baltop` | Richest players and server total. |
| `/eco give <player> <amount>` | economy | `essentials.eco` + `essentials.eco.give` | Add money. |
| `/eco take <player> <amount>` | | `essentials.eco` + `essentials.eco.take` | Remove money. |
| `/eco set <player> <amount>` | | `essentials.eco` + `essentials.eco.set` | Set a balance (0 allowed). |
| `/eco reset <player>` | | `essentials.eco` + `essentials.eco.reset` | Back to `economy.starting-balance`. |

## Kits

| Command | Aliases | Permission | Description |
|---|---|---|---|
| `/kit` | | `essentials.kit` | List kits. |
| `/kit <name>` | | `essentials.kit` + `essentials.kit.<name>` | Claim a kit (cooldown applies, bypass `essentials.kit.cooldown.bypass`). |
| `/kit <name> <player>` | | `essentials.kit.others` | Give a kit (no cooldown started). |
| `/kits` | | `essentials.kits` | Clickable list with cooldowns. |
| `/createkit <name> [cooldown]` | mkkit | `essentials.createkit` | Save your inventory as a kit. Cooldown: `0`, `1h`, `1d`, ..., or `once`. |
| `/delkit <name>` | rmkit | `essentials.delkit` | Delete a kit. |

## Moderation

| Command | Aliases | Permission | Description |
|---|---|---|---|
| `/mute <player> [duration] [reason]` | | `essentials.mute` | Mute (offline too). Exempt: `essentials.mute.exempt`. |
| `/unmute <player>` | | `essentials.unmute` | Unmute. |
| `/tempban <player> <duration> [reason]` | tban | `essentials.tempban` | Timed ban in vanilla's ban list. Exempt: `essentials.tempban.exempt`. |
| `/kick <player> [reason]` | | `essentials.kick` | Kick. Exempt: `essentials.kick.exempt`. **Replaces vanilla /kick**. |
| `/jail <player> <jail> [duration] [reason]` | | `essentials.jail` | Jail (offline too). Exempt: `essentials.jail.exempt`. |
| `/jails` | | `essentials.jail` | List jails. |
| `/setjail <name>` | createjail | `essentials.setjail` | Create or move a jail. |
| `/deljail <name>` | remjail, rmjail | `essentials.deljail` | Delete a jail and free its prisoners. |
| `/unjail <player>` | | `essentials.unjail` | Release. |
| `/vanish [player]` | v | `essentials.vanish` / `.others` | Toggle vanish. Seeing vanished players: `essentials.vanish.see`. |
| `/freeze <player>` | | `essentials.freeze` | Toggle freezing. Exempt: `essentials.freeze.exempt`. |
| `/sudo <player> <command>` | | `essentials.sudo` | Run a command as a player. Exempt: `essentials.sudo.exempt`. |
| `/sudo <player> c:<message>` | | `essentials.sudo` | Make a player chat. |

## World

| Command | Aliases | Permission | Description |
|---|---|---|---|
| `/time [world]` | | `essentials.time` | Show time, ticks and day. **Replaces vanilla /time**. |
| `/time set <day\|noon\|night\|midnight\|ticks> [world]` | | `essentials.time.set` | Set the time. Named times move forward to the next occurrence. |
| `/time add <time> [world]` | | `essentials.time.add` | Add time (`100`, `5s`, `1d`). |
| `/day [world]` | | `essentials.day` | Next morning. |
| `/night [world]` | | `essentials.night` | Next night. |
| `/weather clear\|rain\|thunder [duration]` | | `essentials.weather` + `essentials.weather.<type>` | Set the weather. **Replaces vanilla /weather**. |
| `/sun [duration]` | | `essentials.sun` | Clear weather. |
| `/rain [duration]` | | `essentials.rain` | Rain. |

## Admin

| Command | Aliases | Permission | Description |
|---|---|---|---|
| `/essentials` | ess | `essentials.essentials` | Version. |
| `/essentials reload` | | `essentials.reload` | Reload config.json, messages.json and kits.json, re-register commands. |

## Blocked while...

| State | What is blocked | Config |
|---|---|---|
| Combat tagged | Commands in `combat.blocked-commands` | Bypass `essentials.combat.command.bypass` |
| Muted | Chat and commands in `mute.blocked-commands` | |
| Jailed | Every Essentials command except `jail.allowed-commands`; chat if `jail.allow-chat` is false | |
| Frozen | Every Essentials command except `freeze.allowed-commands` (when `freeze.block-commands`) | |
| On cooldown | The command, for `commands.<name>.cooldown-seconds` | Bypass `essentials.command.cooldown.bypass` |
