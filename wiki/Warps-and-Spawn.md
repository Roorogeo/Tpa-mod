# Warps and Spawn

## Warps

| Command | Permission |
|---|---|
| `/warp` | `essentials.warp` (lists warps) |
| `/warp <name>` | `essentials.warp` **and** `essentials.warp.<name>` |
| `/warp <name> <player>` | `essentials.warp.others` (instant, no warmup for the target) |
| `/warps` | `essentials.warps` |
| `/setwarp <name>` | `essentials.setwarp` |
| `/delwarp <name>` | `essentials.delwarp` |

Warps from a previous mod's `homewarps.json` are imported automatically on the first start; see
[Homes → Importing](Homes.md#importing-homes-from-homewarpsjson).

- Warps are server-wide and stored in `config/essentials/warps.json`.
- Names follow `warps.name-pattern`; `others` is reserved (it would clash with `essentials.warp.others`).
- Per-warp nodes default to **everyone**, so new warps are public. Lock one with
  `/lp group default permission set essentials.warp.<name> false`, or make all warps private by default:
  `"essentials.warp.*": "op"` in `permissions.defaults`, then open them one by one.
- `warps.per-warp-permissions: false` ignores the per-warp nodes entirely.
- `/warps` only shows warps you can use (`warps.list-only-usable`). Entries are clickable.

## Spawn

| Command | Permission |
|---|---|
| `/spawn` | `essentials.spawn` |
| `/spawn <player>` | `essentials.spawn.others` |
| `/setspawn` | `essentials.setspawn` |

`/setspawn` stores your exact position and facing in `spawn.json` **and** sets the vanilla world spawn,
so compasses and vanilla respawns agree. Until `/setspawn` is used, `/spawn` uses the vanilla world spawn.

| Setting | Default | Effect |
|---|---|---|
| `spawn.teleport-on-first-join` | `true` | New players start at /spawn. |
| `spawn.teleport-on-join` | `false` | Everyone is sent to /spawn on every join. |
| `spawn.respawn-at-spawn` | `true` | Players without a bed or anchor respawn at /spawn. |

Jailed players are never moved to spawn by these settings.
