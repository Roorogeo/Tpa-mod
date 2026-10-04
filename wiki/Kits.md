# Kits

A kit is a named set of items that players can claim with `/kit <name>`, with a cooldown per kit.

## Commands

| Command | Permission | Default |
|---|---|---|
| `/kit` | `essentials.kit` | everyone — without a name, lists your kits (same as `/kits`) |
| `/kit <name>` | `essentials.kit` **and** `essentials.kit.<name>` | everyone |
| `/kit <name> <player>` | `essentials.kit.others` | op — gives the kit without starting the target's cooldown |
| `/kits` | `essentials.kits` | everyone |
| `/createkit <name> [cooldown]` | `essentials.createkit` | op |
| `/delkit <name>` | `essentials.delkit` | op |
| — | `essentials.kit.cooldown.bypass` | op — ignore cooldowns and one-time limits |

`/kits` and the `/kit` tab completion only show kits you have `essentials.kit.<name>` for. In chat, each
kit name is clickable (claims it) and its hover text shows the item count and cooldown. Kits on cooldown
are shown grey with the time left.

## Creating a kit in game

1. Put the items in your inventory exactly as you want them (armor and offhand slots are included).
2. Run `/createkit <name> <cooldown>`.

| Cooldown argument | Meaning |
|---|---|
| `0` (or omitted) | No cooldown |
| `30m`, `12h`, `1d`, `1w`, `1d12h` | Duration (units `w d h m s`, combinable; a bare number is seconds) |
| `once` or `-1` | One-time kit: each player can claim it once, ever |

Running `/createkit` with an existing name replaces that kit. Names must match `kits.name-pattern`
(default `[a-z0-9_-]{1,32}`), and names in `kits.reserved-names` (`others`, `cooldown`) are refused because
they would collide with the `essentials.kit.others` and `essentials.kit.cooldown.bypass` nodes.

Items are saved with **all their components** — enchantments, custom names, lore, damage, dyed colors,
potion contents, shulker box contents, and so on.

## `kits.json`

Kits live in `config/essentials/kits.json`. On first start the file contains an example `starter` kit:

```json
{
  "starter": {
    "cooldown-seconds": 86400,
    "items": [
      { "id": "minecraft:stone_sword", "count": 1 },
      { "id": "minecraft:stone_pickaxe", "count": 1 },
      { "id": "minecraft:bread", "count": 16 }
    ]
  }
}
```

- `cooldown-seconds`: `0` = no cooldown, `-1` = one time, anything else = seconds between claims.
- `items`: vanilla item stacks in the same format as `/give` data and vanilla saves (the `ItemStack`
  codec). Components go under `components`:

```json
{
  "id": "minecraft:diamond_sword",
  "count": 1,
  "components": {
    "minecraft:custom_name": "Starter Blade",
    "minecraft:enchantments": { "minecraft:sharpness": 2, "minecraft:unbreaking": 3 }
  }
}
```

The easiest way to get the exact JSON for a complicated item is to create the kit in game with
`/createkit` and look at the file. Edit the file while the server runs, then `/essentials reload`.
Invalid items are skipped and logged (`Invalid item in kit <name>: ...`) instead of breaking the kit.

## Cooldowns

The time a player last claimed each kit is stored in their player file (`kit-uses`), so cooldowns survive
restarts and relogs. Changing a kit's cooldown applies to existing claims immediately: the remaining time
is always `last claim + current cooldown − now`.

Giving a kit to someone else with `/kit <name> <player>` never starts their cooldown.

## Full inventories

`kits.overflow` decides what happens when the items don't fit:

- `drop` (default): what doesn't fit is dropped at the player's feet (`kit.dropped`).
- `deny`: the kit is refused if the player has fewer free slots than the kit has items
  (`kit.inventory-full`), and no cooldown starts.

## First-join kit

Set `kits.first-join-kit` to a kit name (e.g. `"starter"`) to give it to every new player on their first
join. This doesn't need the kit's permission and doesn't start a cooldown, so the player can still claim
the kit normally.

## Restricting kits

Every kit is available to everyone by default (`essentials.kit.<name>` defaults to `all`). Common setups:

```json
"permissions": {
  "defaults": {
    "essentials.kit.*": "op",
    "essentials.kit.starter": "all"
  }
}
```

With LuckPerms:

```
/lp group default permission set essentials.kit.vip false
/lp group vip permission set essentials.kit.vip true
```

## Messages

`kit.received`, `kit.given`, `kit.not-found`, `kit.no-permission`, `kit.cooldown`, `kit.one-time`,
`kit.inventory-full`, `kit.dropped`, `kits.header`, `kits.entry`, `kits.entry-hover`, `kits.entry-cooldown`,
`kits.separator`, `kits.none`, `kits.one-time`, `createkit.created`, `createkit.empty`,
`createkit.invalid-name`, `createkit.reserved`, `delkit.deleted`. See [Messages](Messages.md).

## Command settings

Like every command, `kit`, `kits`, `createkit` and `delkit` can be disabled, renamed/aliased or given an
extra command cooldown under `commands` in `config.json` (see [Configuration](Configuration.md#commands)).
The command cooldown is separate from the per-kit cooldown.
