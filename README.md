# Essentials for Fabric

A **server-side** Fabric mod for Minecraft **26.2** that brings the core of EssentialsX to Fabric:
teleports, homes, warps, teleport requests, chat, private messages, mail, nicknames, an economy, kits,
moderation tools, world controls and combat tagging.

- **Vanilla clients join without installing anything.** The mod is `"environment": "server"`.
- **A permission node for every command and every sub-feature**, checked through
  [fabric-permissions-api](https://github.com/lucko/fabric-permissions-api) (bundled), so
  [LuckPerms](https://luckperms.net) works out of the box. Commands a player can't use are hidden from
  them, including tab completion.
- **Everything is configurable**: every number and toggle in `config.json`, every message (with `&` colors,
  hex colors and placeholders) in `messages.json`, every permission default, and every command can be
  disabled, aliased or given a cooldown.
- **No blocking I/O**: data is loaded at startup and saved on a background thread, flushed on stop.

## Requirements

| | Version |
|---|---|
| Minecraft server | 26.2 |
| Java | 25 |
| Fabric Loader | 0.19.5+ |
| Fabric API | 0.161.0+26.2 |
| fabric-permissions-api | 0.7.0 (bundled in the jar) |
| LuckPerms | optional |

## Install

1. Set up a Fabric 26.2 server and put Fabric API in `mods/`.
2. Put `essentials-<version>.jar` in `mods/` (and LuckPerms, if you want it).
3. Start the server. `config/essentials/` is created with `config.json`, `messages.json` and `kits.json`.
4. In game: `/setspawn`, `/setwarp <name>`, `/setjail <name>`, `/createkit <name> <cooldown>`.
5. Edit the config and apply it with `/essentials reload`.

## Commands

| Category | Commands |
|---|---|
| Teleport | `/spawn` `/setspawn` `/home` `/sethome` `/delhome` `/homes` `/warp` `/setwarp` `/delwarp` `/warps` `/tpa` `/tpahere` `/tpaccept` `/tpdeny` `/tpacancel` `/tp` `/tphere` `/tpall` `/back` `/top` `/rtp` |
| Chat | `/msg` `/reply` `/mail` `/ignore` `/nick` `/realname` `/me` `/broadcast` `/socialspy` |
| Player | `/heal` `/feed` `/fly` `/god` `/speed` `/gamemode` (`/gmc` `/gms` `/gma` `/gmsp`) `/afk` `/hat` `/repair` `/enderchest` `/workbench` `/anvil` `/invsee` `/clearinventory` `/suicide` `/near` `/seen` `/whois` `/list` `/ping` `/combat` |
| Economy | `/balance` `/pay` `/baltop` `/eco give\|take\|set\|reset` |
| Kits | `/kit` `/kits` `/createkit` `/delkit` |
| Moderation | `/mute` `/unmute` `/tempban` `/kick` `/jail` `/jails` `/setjail` `/deljail` `/unjail` `/vanish` `/freeze` `/sudo` |
| World | `/time` `/day` `/night` `/weather` `/sun` `/rain` |
| Admin | `/essentials reload` |

Full syntax, aliases and nodes: [wiki/Commands.md](wiki/Commands.md).

## Highlights

- **Teleports** with configurable warmup (cancelled on move or damage), cooldowns (shared or per command),
  cross-dimension support, asynchronous chunk loading and a safe-destination search. `/back` remembers
  the location before every teleport and on death.
- **Home limits** from `essentials.sethome.multiple.<n>` (highest wins), clamped to
  `homes.absolute-max-homes`; `essentials.sethome.unlimited`; overwriting a home never counts against
  the limit; `/homes` shows `used/max`.
- **Combat tagging**: PvP damage (melee, arrows, tridents, potions, end crystals, TNT, tamed pets) tags both
  players for 30 s (configurable), shows a countdown in the action bar, blocks a configurable command
  list, cancels warmups, turns off fly and god mode, and kills players who log out while tagged (never on
  kicks or shutdown).
- **Chat**: format, local chat radius, colors by permission, private messages with social spy, mail,
  ignore lists, nicknames (also in the tab list), AFK.
- **Economy** with `/pay`, `/baltop` and admin `/eco`; **kits** with per-kit cooldowns or one-time kits;
  **moderation** with mutes, temp bans (vanilla ban list), jails, vanish, freeze and sudo.

## Documentation

| | |
|---|---|
| [PERMISSIONS.md](PERMISSIONS.md) | Every permission node, what it does and its default (generated from the code) |
| [Wiki home](wiki/Home.md) | Start here |
| [Installation](wiki/Installation.md) · [Configuration](wiki/Configuration.md) · [Messages](wiki/Messages.md) · [Permissions](wiki/Permissions.md) | Setup and customization |
| [Commands](wiki/Commands.md) · [Teleportation](wiki/Teleportation.md) · [Homes](wiki/Homes.md) · [Warps and Spawn](wiki/Warps-and-Spawn.md) · [Teleport Requests](wiki/Teleport-Requests.md) | Teleport features |
| [Combat Tagging](wiki/Combat-Tagging.md) · [Chat](wiki/Chat.md) · [Player Utilities](wiki/Player-Utilities.md) · [Economy](wiki/Economy.md) · [Kits](wiki/Kits.md) · [Moderation](wiki/Moderation.md) · [World](wiki/World.md) | Features |
| [Data and Storage](wiki/Data-and-Storage.md) · [Technical Notes](wiki/Technical-Notes.md) · [Building](wiki/Building.md) · [FAQ](wiki/FAQ.md) | Internals and help |

## Building

```
./gradlew build
```

The jar is written to `build/libs/essentials-<version>.jar`. Requires JDK 25. See
[wiki/Building.md](wiki/Building.md). After changing permission nodes, regenerate `PERMISSIONS.md` with
`java src/main/java/com/roorogeo/essentials/perm/PermissionNodes.java PERMISSIONS.md` (CI checks it).

## License

MIT, see [LICENSE](LICENSE).
