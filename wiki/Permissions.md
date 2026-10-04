# Permissions

The complete list of 155 nodes, with descriptions and defaults, is in
**[PERMISSIONS.md](../PERMISSIONS.md)** (generated from the code, so it is always exact). This page
explains how checks work.

## How a check is decided

Essentials asks [fabric-permissions-api](https://github.com/lucko/fabric-permissions-api) (bundled in
the jar), which any permission mod can answer. LuckPerms does.

1. **The permission mod has a value** for the node (true or false) → that value is used.
2. **No value** (or no permission mod at all) → the node's **fallback default** decides:
   1. `permissions.defaults` has the exact node (e.g. `essentials.kit.vip`) → use it;
   2. else `permissions.defaults` has the node's pattern (e.g. `essentials.kit.*`) → use it;
   3. else the built-in default from the code (the **Default** column of PERMISSIONS.md).

| Default value | Who passes |
|---|---|
| `all` / `true` | Everyone |
| `op` | Operators at `permissions.op-level` (2) or above |
| `op:<level>` | Operators at that level or above, e.g. `op:4` |
| `none` / `false` | Nobody except the server console |

The console and command blocks are checked like everyone else: the console has level 4 and passes
every `op` node, command blocks have level 2.

## Built-in defaults

- **Everyone**: basic player commands — `/spawn`, `/home`, `/sethome`, `/delhome`, `/homes`, `/warp`, `/warps`,
  `/tpa` and friends, `/back` (and `back.ondeath`), `/rtp`, `/msg`, `/reply`, `/mail` (read/send/clear),
  `/ignore`, `/realname`, `/me`, `/afk`, `/hat`, `/suicide`, `/seen`, `/list`, `/ping`, `/combat`,
  `/balance` (also of others), `/pay`, `/baltop`, `/kit`, `/kits`, `/time` (query only), and every
  `essentials.warp.<name>` / `essentials.kit.<name>`.
- **Operators (level 2)**: staff commands (`/tp`, `/setwarp`, `/mute`, `/vanish`, `/eco`, ...), every
  `.others` node, and every bypass/exempt node (`essentials.teleport.cooldown.bypass`,
  `essentials.teleport.delay.bypass`, `essentials.combat.bypass`, `essentials.combat.command.bypass`,
  `essentials.command.cooldown.bypass`, `essentials.kit.cooldown.bypass`, `*.exempt`, ...). Cosmetic
  extras such as `/nick` and the color nodes are also op-only by default.
- **Nobody**: `essentials.sethome.multiple.<number>` (you grant the number you want) and
  `essentials.teleport.safety.bypass`.

## Hidden commands

Each command's root node requires the command's permission, and every sub-feature (`/heal <player>`,
`/eco give`, `/speed <n> fly`, `/gamemode creative`, ...) requires its own node on its Brigadier
node. Players never see, and can't tab-complete, commands or arguments they can't use. LuckPerms
resends the command tree when permissions change, so changes show up without relogging.

## Pattern nodes

| Pattern | Example | Default |
|---|---|---|
| `essentials.warp.<name>` | `essentials.warp.arena` | everyone (deny to lock a warp) |
| `essentials.kit.<name>` | `essentials.kit.vip` | everyone (deny to lock a kit) |
| `essentials.sethome.multiple.<number>` | `essentials.sethome.multiple.10` | nobody |

To make **all** kits op-only by default and then open one:

```json
"permissions": {
  "defaults": {
    "essentials.kit.*": "op",
    "essentials.kit.starter": "all"
  }
}
```

## LuckPerms recipes

```
# Members: 5 homes, nickname with colors, no teleport warmup
/lp group member permission set essentials.sethome.multiple.5 true
/lp group member permission set essentials.nick true
/lp group member permission set essentials.nick.color true
/lp group member permission set essentials.teleport.delay.bypass true

# VIP kit and warp only for VIPs
/lp group default permission set essentials.kit.vip false
/lp group vip permission set essentials.kit.vip true
/lp group default permission set essentials.warp.vip false
/lp group vip permission set essentials.warp.vip true

# Helpers can mute, kick, jail and see vanished staff, but not ban
/lp group helper permission set essentials.mute true
/lp group helper permission set essentials.unmute true
/lp group helper permission set essentials.kick true
/lp group helper permission set essentials.jail true
/lp group helper permission set essentials.unjail true
/lp group helper permission set essentials.vanish.see true

# Staff ignore PvP restrictions
/lp group staff permission set essentials.combat.bypass true
/lp group staff permission set essentials.combat.command.bypass true

# Chat prefix shown by {prefix} in chat.format
/lp group vip meta setprefix "&6[VIP] "

# Take /back away from everyone (it is "all" by default)
/lp group default permission set essentials.back false
```

## Home limits

See [Homes](Homes.md#limits).

## Without a permission mod

Everything still works: `op`/`all`/`none` decide. To give everyone a staff command without LuckPerms,
change its default, e.g. `"essentials.near": "all"`. To keep a player command away from non-ops,
e.g. `"essentials.rtp": "op"`.
