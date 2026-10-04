# Installation

## Requirements

| Component | Version |
|---|---|
| Minecraft (server) | 26.2 |
| Java | 25 or newer |
| Fabric Loader | 0.19.5 or newer |
| Fabric API | 0.161.0+26.2 or newer for 26.2 |
| fabric-permissions-api | bundled inside the Essentials jar (0.7.0), nothing to install |
| LuckPerms (optional) | any Fabric build for 26.2 |

Clients need **nothing**: vanilla 26.2 clients can join. The mod declares `"environment": "server"`,
so it is never loaded on a client even if someone puts it in their mods folder.

## Installing

1. Install the Fabric server launcher for 26.2 (https://fabricmc.net/use/server/).
2. Put `fabric-api-<version>.jar` and `essentials-<version>.jar` in the server's `mods/` folder.
3. (Optional) Put LuckPerms in `mods/` too.
4. Start the server.

On the first start Essentials creates:

```
config/essentials/
├── config.json      every setting (see Configuration)
├── messages.json    every message (see Messages)
├── kits.json        kits, with an example "starter" kit
├── warps.json       created when the first warp is set
├── jails.json       created when the first jail is set
├── spawn.json       created by /setspawn
└── userdata/
    └── <uuid>.json  one file per player
```

## Switching from another home mod

If your previous home mod kept its homes in `homewarps.json`, leave that file in place when you swap the
jars. Essentials imports every home on its first start and renames the file afterwards. See
[Homes → Importing](Homes.md#importing-homes-from-homewarpsjson).

## First steps

```
/setspawn                     spawn point for /spawn, new players and respawns without a bed
/setwarp shop                 a warp anyone can use (essentials.warp.shop defaults to everyone)
/setjail jail                 a jail for /jail
/createkit starter 1d         your current inventory as a kit, claimable once per day
/essentials reload            after editing config.json or messages.json
```

## Permissions without a permission mod

Without LuckPerms, every node falls back to its default: player commands are allowed for everyone,
staff commands and every bypass node need operator level 2 (`/op` gives level 4 by default, which
includes level 2). Change the level in `permissions.op-level`, or the default of any single node in
`permissions.defaults`. See [Permissions](Permissions.md).

## With LuckPerms

LuckPerms answers every check. Nodes that LuckPerms has no value for still use the fallback default,
so a fresh LuckPerms install behaves exactly like no permission mod until you set nodes. Example:

```
/lp group default permission set essentials.nick true
/lp group vip permission set essentials.sethome.multiple.10 true
/lp group mod permission set essentials.mute true
```

Prefixes and suffixes from LuckPerms meta are available in the chat format as `{prefix}` and `{suffix}`.

## Updating

Replace the jar and restart. New settings and messages are added to `config.json` and
`messages.json` automatically with their defaults; your existing values are kept.

## Uninstalling

Remove the jar. Vanilla commands that Essentials replaced (/tp, /msg, /tell, /w, /me, /list, /kick,
/time, /weather, /gamemode) come back automatically. The `config/essentials` folder can be deleted.
Vanilla data is not modified, except that /tempban entries live in vanilla's `banned-players.json`
(they expire on their own, or remove them with `/pardon`).
