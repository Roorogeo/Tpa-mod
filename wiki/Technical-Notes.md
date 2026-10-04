# Technical Notes

How Essentials is put together, for server admins who want to know what it touches and for developers.

## Server-side only

`fabric.mod.json` declares `"environment": "server"`, so the mod is never loaded on a client. Nothing it
does needs a client mod:

| Feature | Built from |
|---|---|
| Commands, tab completion | Brigadier nodes the client receives in the normal command tree packet |
| Colored text, click/hover | Vanilla chat components |
| Action bar (combat timer, vanish, warmups) | Vanilla overlay messages |
| `/enderchest`, `/invsee`, `/workbench`, `/anvil` | Vanilla menus (chest, crafting table, anvil) |
| Tab list names | The vanilla player-info packet's display name |
| Vanish | Vanilla entity tracking and player-info remove packets |
| Fly/speed | Vanilla player abilities packet |

## Mixins

Essentials prefers Fabric API events and only uses a mixin where Fabric API has no event for the job.
There are four, all in `com.roorogeo.essentials.mixin`, all with `defaultRequire: 1` (if Mojang changes a
target, the server refuses to start with a clear error instead of silently losing a feature).

### 1. `ServerPlayerMixin` — vanish visibility and tab list names

Two `@Inject`s at the head of methods on `ServerPlayer`:

- **`broadcastToPlayer(ServerPlayer viewer)`** — vanilla's entity tracker asks this before it shows a
  player's body to another player. Returning `false` when the viewer may not see a vanished player hides
  the body from that one client while vanilla tracking keeps working for everything else (and for other
  mods). There is no Fabric event for "should this entity be visible to this player".
- **`getTabListDisplayName()`** — the name vanilla puts in the tab list (`null` = account name). Returning
  `tablist.format` (nickname, AFK tag) makes nicknames show in the tab list on vanilla clients. Only applied
  when `tab-list.enabled` is true; otherwise the method runs unchanged.

### 2. `ChunkMapAccessor` — read the entity tracker

An `@Accessor` for the private `ChunkMap.entityMap` (entity id → tracked entity). When someone vanishes or
reappears, Essentials needs to make every client re-evaluate visibility **right away**; without this,
vanilla only re-checks when the player crosses into another chunk section.

### 3. `TrackedEntityInvoker` — refresh one viewer

An `@Invoker` for `ChunkMap.TrackedEntity#updatePlayer(ServerPlayer)`, vanilla's own "should this viewer
see this entity" routine; it sends the spawn or remove packets as needed. `TrackedEntity` is a private
inner class, so a mixin is the only way to call it. Together with #2 this is how `/vanish` takes effect
instantly without re-implementing entity spawning.

### 4. `ServerCommonPacketListenerImplMixin` — kicks vs. combat logging

An `@Inject` at the head of `disconnect(DisconnectionDetails)`. Every disconnect the **server** starts —
`/kick`, bans, `/tempban`, server shutdown, kicks by other mods — goes through this method, while a
player who closes their game never does. Fabric's `ServerPlayConnectionEvents.DISCONNECT` doesn't say which
one happened, and the combat-log rule ("don't punish disconnects caused by a shutdown or a kick") needs
exactly that. The mixin only records the player's UUID; the disconnect itself is untouched. Keep-alive
timeouts (`disconnect.timeout`) are treated as the player's fault unless `combat.punish-timeouts` is false.
Disconnects while the server is shutting down (`!server.isRunning()`) are never punished either.

## Fabric API events used

| Event | Used for |
|---|---|
| `CommandRegistrationCallback` | Registering all commands (also re-fires on `/reload`) |
| `ServerLifecycleEvents.SERVER_STARTING / STOPPING / STOPPED` | Async data load, shutdown flagging, final save |
| `ServerLoginConnectionEvents.QUERY_START` + `LoginSynchronizer` | Holding logins until data is loaded |
| `ServerPlayConnectionEvents.INIT / DISCONNECT` | Session data, combat logging, AFK/TPA cleanup |
| `ServerPlayerEvents.JOIN / LEAVE / AFTER_RESPAWN` | Join messages, MOTD, first-join kit, spawn teleports, vanish/jail/freeze restore, respawn at spawn |
| `ServerLivingEntityEvents.ALLOW_DAMAGE` | Combat tagging (including projectiles, TNT, end crystals, potions, pets), god/vanish/freeze/AFK invulnerability, cancelling warmups on damage |
| `ServerLivingEntityEvents.AFTER_DEATH` | `/back` on death, clearing combat tags |
| `ServerMessageEvents.ALLOW_CHAT_MESSAGE` | Chat format, mutes, ignore, local chat, colors |
| `ServerMessageEvents.ALLOW_GAME_MESSAGE` | Replacing vanilla join/leave messages |
| `AttackBlockCallback`, `UseBlockCallback`, `UseItemCallback`, `UseEntityCallback`, `AttackEntityCallback`, `PlayerBlockBreakEvents.BEFORE` | Jail/freeze interaction blocking |
| `ServerTickEvents.END_SERVER_TICK` | Warmups, combat timers, AFK, jail time, freeze anchors, autosave |

## Replaced vanilla commands

Brigadier can't remove or overwrite a registered command, so when an enabled Essentials command (or alias)
has the same name as an existing one, Essentials removes the existing node from the dispatcher's root
(through reflection on Brigadier's `children`/`literals`/`arguments` maps) and registers its own. With the
default config this affects:

`/gamemode`, `/kick`, `/list`, `/me`, `/msg`, `/tell`, `/w`, `/time`, `/tp`, `/weather`

The server log lists them at startup:
`Replaced existing commands: /tp, /tell, /w, /msg, /me, /list, /kick, /time, /weather, /gamemode (...)`.

- To keep a vanilla command, set `commands.<name>.enabled` to `false` (or remove the colliding alias) and
  restart (or run vanilla `/reload`, which rebuilds the vanilla command tree).
- `/teleport` is left alone, so command blocks and datapacks that use `/teleport` keep vanilla behavior.
- Aliases that would collide with function-only commands like `/return` or `/execute` are not used by
  default. If you add an alias with the name of an existing command, it will replace that command.

## Permission checks

All checks go through `me.lucko:fabric-permissions-api` (bundled in the jar with `include`):

1. `Permissions.getPermissionValue(source, node)` — if a permission mod (LuckPerms) has an explicit
   true/false, that wins.
2. Otherwise the configurable default (see [Permissions](Permissions.md)): exact node in
   `permissions.defaults`, then the pattern (`essentials.kit.*`), then the built-in default.
3. `op` defaults use `Permissions.check(source, node, level)`, i.e. vanilla operator levels.

Command visibility uses the same check in Brigadier's `.requires(...)`, so players never see or
tab-complete commands or sub-commands they can't use. The client gets its command tree when it joins
(and Essentials resends it after `/essentials reload`), so after changing a player's permissions in a way
that should show or hide commands, they see the change after rejoining or the next tree resend.

## Threading

- The server thread never reads or writes Essentials files (see [Data and Storage](Data-and-Storage.md)).
- `/essentials reload` reads files on the I/O thread and applies the result on the server thread.
- Teleports wait for the destination chunk asynchronously (a chunk ticket plus the chunk future) before
  checking safety and moving the player, so `/rtp`, `/home` and `/warp` to unloaded areas don't stall
  the tick.

## Compatibility

- **LuckPerms** (Fabric): fully supported; every node in [PERMISSIONS.md](../PERMISSIONS.md) can be set per user or group.
- **Other permission mods** implementing fabric-permissions-api work the same way.
- **Other command mods**: if two mods register the same command name, whichever registers last wins.
  Essentials registers during `CommandRegistrationCallback`; disable or rename the Essentials command in
  `commands.<name>` to resolve conflicts.
- **Chat mods** that also cancel or reformat chat through `ServerMessageEvents` can conflict with the
  chat format; set `chat.format-enabled` to `false` to leave chat formatting to the other mod.
