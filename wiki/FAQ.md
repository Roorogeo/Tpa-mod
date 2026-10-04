# FAQ and Troubleshooting

## General

**Do players need to install anything?**
No. The mod is server-only (`"environment": "server"`). Vanilla 26.2 clients join normally.

**Do I need LuckPerms?**
No. Without a permission mod, every node falls back to its default: player commands are allowed for
everyone, staff commands and all bypass nodes need operator level 2. You can change any default in
`config.json` → `permissions.defaults` (see [Permissions](Permissions.md)). With LuckPerms, whatever
LuckPerms says wins.

**Does it run on a client or in singleplayer?**
No. It is a dedicated-server mod. (Fabric doesn't load server-only mods on the client, including for
singleplayer worlds.)

**Which Minecraft versions?**
26.2 only (`"minecraft": "~26.2"`). The mod uses Mojang's official names, which 26.x ships without
obfuscation.

## Commands

**Vanilla `/tp` (or `/msg`, `/list`, `/time`...) behaves differently / is gone.**
Essentials replaces vanilla commands that share a name with its own (listed in the server log at startup
and in [Technical Notes](Technical-Notes.md#replaced-vanilla-commands)). To get the vanilla one back, set
`commands.<name>.enabled` to `false` in `config.json` and restart. `/teleport` is never touched, so use it
in command blocks and datapacks if you want vanilla behavior.

**A player can't see a command they should have.**
Commands without permission are hidden from the command tree. Check the node with
`/lp user <name> permission check essentials.<node>`. The client gets its command tree on join, so after a
permission change the player may need to rejoin.

**A command says "Unknown or incomplete command".**
Either it is disabled (`commands.<name>.enabled: false`), or the player lacks the node and the command is
hidden. The console can always see every command.

**How do I rename a command or add an alias?**
`commands.<name>.aliases` in `config.json`, e.g. `"spawn": { "aliases": ["hub", "lobby"] }`, then
`/essentials reload`. Aliases share the command's permission.

**How do I put a cooldown on one command?**
`commands.<name>.cooldown-seconds`, e.g. `"heal": { "cooldown-seconds": 300 }`. Bypass:
`essentials.command.cooldown.bypass`.

**How do I turn off a whole feature (economy, kits, ...)?**
Disable its commands under `commands` (`balance`, `pay`, `baltop`, `eco` for the economy). There is no data
to remove; balances simply aren't used.

## Teleports

**"The destination ... is not safe and no safe spot was found nearby."**
The safety check couldn't find solid ground without lava/fire/etc. within `teleport.safety.horizontal-radius`
and `vertical-radius`. Fix the destination, raise the radii, or let staff skip the check with
`essentials.teleport.safety.bypass`. Creative and spectator players skip it by default.

**Teleports get cancelled.**
The player moved during the warmup (`teleport.cancel-on-move`, tolerance `teleport.move-tolerance`), took
damage (`teleport.cancel-on-damage`), or got combat-tagged. Set `teleport.delay-seconds` to `0` or grant
`essentials.teleport.delay.bypass` for instant teleports.

**`/rtp` takes a moment.**
It loads (and possibly generates) chunks asynchronously to find a safe spot, so the server doesn't freeze.
Each attempt waits up to `teleport.chunk-load-timeout-seconds`. Pre-generating the world makes it instant.

**`/back` doesn't go to where I died.**
Death locations need `essentials.back.ondeath` (default: everyone) and `back.record-deaths: true`.

## Combat

**A player was killed for logging out, but they were kicked.**
They shouldn't be: kicks, bans, `/tempban` and server shutdown never count. A **timeout** (connection lost)
does count unless `combat.punish-timeouts` is `false`.

**Mob damage doesn't tag players.**
That's the default. Set `combat.tag-on-mob-damage` to `true`.

**Staff get tagged.**
Grant `essentials.combat.bypass` (never tagged) or `essentials.combat.command.bypass` (tagged, but can use
blocked commands).

## Chat and names

**Nicknames don't show in the tab list.**
Check `tab-list.enabled` and the `tablist.format` message. The tab list shows the nickname for vanilla
clients through the `getTabListDisplayName` hook.

**Colors in chat don't work.**
Players need `essentials.chat.color` (and `essentials.chat.format` for bold/italic etc.,
`essentials.chat.magic` for `&k`). Without them, `&` codes stay as typed.

**Players can't select chat messages in the vanilla "Report Chat" screen.**
With the chat format enabled, Essentials delivers formatted chat as system messages, which are not signed
by the sender, so the report screen can't pick them. Set `chat.format-enabled` to `false` to leave chat to
vanilla (signed) while keeping mutes and ignore lists.

## Config and data

**I edited `config.json` and nothing changed.**
Run `/essentials reload`. If the file has a JSON error, the reload fails with `general.reload-failed`, the
error is logged, and the previous settings stay active.

**The server log says "Could not read config/essentials; using built-in defaults".**
`config.json` or `messages.json` has a syntax error at startup. Fix it (a JSON validator helps) and run
`/essentials reload`.

**I edited a player file / `warps.json` and my change disappeared.**
Those files are kept in memory and written back by the server. Stop the server, edit, then start it. See
[Data and Storage](Data-and-Storage.md#editing-files-by-hand).

**How do I reset a player?**
Stop the server and delete `config/essentials/userdata/<uuid>.json`.

**How do I reset messages to the defaults?**
Delete the key from `messages.json` (or the whole file) and run `/essentials reload`; missing keys are
filled with defaults and written back.

## Time and weather

**`/day` doesn't work in the Nether.**
In 26.x the Nether has no world clock. Use `/day minecraft:overworld`, or run it from the overworld.

**`/weather` has no world argument.**
Since 26.1, weather is shared by all dimensions.
