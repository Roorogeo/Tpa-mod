# Essentials for Fabric — Wiki

Essentials is a **server-side** Fabric mod for Minecraft **26.2** that brings the core of EssentialsX to
Fabric: teleports, homes, warps, teleport requests, chat formatting, private messages, mail, nicknames,
an economy, kits, moderation tools, world controls and combat tagging.

Players join with an **unmodified vanilla client**. Everything is built from commands, chat components,
vanilla menus (chests, crafting table, anvil) and vanilla packets.

Every command, and every sub-feature of a command, has its **own permission node** (checked through
[fabric-permissions-api](https://github.com/lucko/fabric-permissions-api), so LuckPerms works out of the box),
and **every behavior, number and message is configurable**.

## Pages

| Page | What it covers |
|---|---|
| [Installation](Installation.md) | Requirements, installing, first start, updating |
| [Configuration](Configuration.md) | Every key in `config.json`, with defaults and examples |
| [Messages](Messages.md) | `messages.json`, color codes, placeholders, every message key |
| [Permissions](Permissions.md) | How checks work, defaults, LuckPerms recipes (full node list: [PERMISSIONS.md](../PERMISSIONS.md)) |
| [Commands](Commands.md) | Every command: syntax, aliases, nodes, notes |
| [Teleportation](Teleportation.md) | Warmups, cooldowns, safe teleports, /back, /top, /rtp, admin teleports |
| [Homes](Homes.md) | /home, /sethome, home limits, other players' homes |
| [Warps and Spawn](Warps-and-Spawn.md) | /warp, per-warp permissions, /spawn, join and respawn behavior |
| [Teleport Requests](Teleport-Requests.md) | /tpa, /tpahere, /tpaccept, /tpdeny, /tpacancel |
| [Combat Tagging](Combat-Tagging.md) | Combat tags, blocked commands, combat logging |
| [Chat](Chat.md) | Chat format, local chat, colors, /msg, /reply, /mail, /ignore, /nick, /me, /broadcast, social spy |
| [Player Utilities](Player-Utilities.md) | /heal, /feed, /fly, /god, /speed, /gamemode, /afk, /hat, /repair, menus, /near, /seen, /whois, /list, /ping |
| [Economy](Economy.md) | Balances, /pay, /baltop, /eco, currency formatting |
| [Kits](Kits.md) | kits.json format, /kit, /createkit, cooldowns, first-join kit |
| [Moderation](Moderation.md) | /mute, /tempban, /kick, jails, /vanish, /freeze, /sudo |
| [World](World.md) | /time, /day, /night, /weather, /sun, /rain (world clocks in 26.x) |
| [Data and Storage](Data-and-Storage.md) | Files, formats, asynchronous saving, backups, editing by hand |
| [Technical Notes](Technical-Notes.md) | Mixins and why each exists, replaced vanilla commands, compatibility |
| [Building from Source](Building.md) | Toolchain versions, building, project layout |
| [FAQ and Troubleshooting](FAQ.md) | Common questions and problems |

## Quick start

1. Install Fabric Loader 0.19.5+ and Fabric API for 26.2 on the server, then drop `essentials-<version>.jar` into `mods/`.
2. (Optional) install LuckPerms for Fabric.
3. Start the server once. `config/essentials/` is created with `config.json`, `messages.json` and `kits.json`.
4. Stand somewhere nice and run `/setspawn`, then `/setwarp shop`, `/setjail jail`, `/createkit starter 1d`.
5. Adjust `config/essentials/config.json` and run `/essentials reload`.

## At a glance

- **Permissions**: 155 nodes. Player commands default to everyone, staff commands and all bypass nodes
  default to operators (level 2). Change any default in `config.json` → `permissions.defaults`.
- **Commands**: 82 commands, 144 names including default aliases. Turn any off, rename or alias it, or give it a cooldown in
  `config.json` → `commands`.
- **Messages**: 333 messages, all editable, with `&` colors, `&#RRGGBB` hex colors and placeholders.
- **Data**: one JSON file per player, read and written on a background thread; logins wait for data
  to load at startup so the server thread never touches the disk.
