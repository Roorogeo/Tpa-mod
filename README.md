# TPA Mod

A simple, server-side Fabric mod for Minecraft **26.3** that adds teleport requests, homes and warps.
Every teleport has a **10 second warmup**: if you move during it, the teleport is cancelled.

Only requires **Fabric API**. Clients don't need to install it.

## Commands

| Command | Description |
|---|---|
| `/tpa <player>` | Ask to teleport to a player |
| `/tpahere <player>` | Ask a player to teleport to you |
| `/tpaccept [player]` | Accept a request (newest if no name given) |
| `/tpdeny [player]` | Deny a request |
| `/tpcancel` | Cancel your outgoing request |
| `/home [name]` | Teleport to a home (defaults to `home`) |
| `/sethome [name]` | Set a home at your position |
| `/delhome <name>` | Delete a home |
| `/homes` | List your homes (clickable) |
| `/warp <name>` | Teleport to a warp |
| `/warps` | List warps (clickable) |
| `/setwarp <name>` | Create or move a warp (op) |
| `/delwarp <name>` | Delete a warp (op) |
| `/tpamod reload` | Reload config and warps (op) |

Requests expire after 60 seconds and show clickable **[Accept]** / **[Deny]** buttons.

## Permissions

Permissions use Fabric API's permission API, which is supported by LuckPerms.
Without a permission mod, the defaults below apply ("op" = op level 2 or higher).

| Node | Default | |
|---|---|---|
| `tpamod.tpa` | everyone | `/tpa`, `/tpcancel` |
| `tpamod.tpahere` | everyone | `/tpahere` |
| `tpamod.tpaccept` | everyone | `/tpaccept`, `/tpdeny` |
| `tpamod.home` | everyone | `/home`, `/homes` |
| `tpamod.sethome` | everyone | `/sethome` |
| `tpamod.delhome` | everyone | `/delhome` |
| `tpamod.warp` | everyone | `/warp`, `/warps` |
| `tpamod.warp.<name>` | everyone | Use a specific warp (set to `false` to lock it) |
| `tpamod.setwarp` | op | `/setwarp` |
| `tpamod.delwarp` | op | `/delwarp` |
| `tpamod.reload` | op | `/tpamod reload` |
| `tpamod.bypass.warmup` | op | Teleport instantly, no warmup |
| `tpamod.homes.unlimited` | op | No home limit |
| `tpamod.homes.limit.<n>` | none | Allow `n` homes (highest granted wins) |

### Limiting homes

The home limit is resolved in this order:

1. `tpamod.homes.unlimited` → no limit
2. Integer meta `tpamod:max_homes`, e.g. `/lp group vip meta set tpamod:max_homes 10`
3. Highest granted `tpamod.homes.limit.<n>`, e.g. `/lp group vip permission set tpamod.homes.limit.10 true`
   (only the values listed in `homeLimitSteps` in the config are checked)
4. `defaultMaxHomes` from the config (3)

## Config

`config/tpamod.json`:

```json
{
  "warmupSeconds": 10,
  "moveTolerance": 0.2,
  "requestTimeoutSeconds": 60,
  "defaultMaxHomes": 3,
  "homeLimitSteps": [1, 2, 3, 5, 10, 15, 20, 25, 50, 100]
}
```

- `warmupSeconds`: set to `0` to disable the warmup.
- `moveTolerance`: blocks a player may drift during the warmup. Looking around never cancels.

## Storage

- Homes are stored on the player data (Fabric data attachment), so they are kept on death.
- Warps are stored in `<world>/tpamod/warps.json`.

## Building

Requires Java 25.

```sh
./gradlew build
```

The jar ends up in `build/libs/`.
