# Player Utilities

Every command below accepts a player argument only with its `.others` node; without it the argument
doesn't even tab-complete.

## /heal, /feed

`/heal [player]`: full health, hunger 20, saturation 20; also puts out fire
(`player.heal-extinguishes`) and removes harmful effects like poison or wither
(`player.heal-removes-negative-effects`). Dead players can't be healed.

`/feed [player]`: hunger and saturation to full.

Both are in `combat.blocked-commands` by default. Give them a cooldown with
`commands.heal.cooldown-seconds` (bypass `essentials.command.cooldown.bypass`).

## /fly

Toggles the ability to fly; turning it off also stops flying. Entering combat turns flight off for
survival/adventure players (`combat.disable-fly-on-tag`). Flight itself is stored by vanilla, so it
survives relogs. Note that switching game mode resets flight (vanilla behavior).

## /god

Toggles god mode: no damage at all (except `/kill`, the void and other damage that ignores
invulnerability). With `player.god-prevents-hunger` hunger stays full. Saved across relogs when
`player.persist-god`. Entering combat turns it off (`combat.disable-god-on-tag`).

## /speed

`/speed <0-10> [fly|walk] [player]`

| Value | Fly speed | Walk speed |
|---|---|---|
| 0 | 0 (frozen in the air) | 0 |
| 0.5 | half of vanilla | half of vanilla |
| 1 | vanilla (0.05) | vanilla (0.1) |
| 10 | `player.max-fly-speed` (0.5) | `player.max-walk-speed` (0.5) |

Values between 1 and 10 scale linearly. Without `fly|walk` the current mode is used (fly speed while
flying). Needs `essentials.speed` plus `essentials.speed.fly` and/or `essentials.speed.walk`. Walk
speed is stored as the player's base movement speed attribute, so it persists; reset with `/speed 1 walk`.

## /gamemode and shortcuts

`/gamemode survival|creative|adventure|spectator [player]` — also `s c a sp` and `0 1 2 3`.
`/gmc`, `/gms`, `/gma`, `/gmsp` are shortcuts. Every mode has its own node
(`essentials.gamemode.creative`, ...), and changing others needs `essentials.gamemode.others`.
Mode names in messages come from `gamemode.name.<mode>`.

## /afk

`/afk [message]` toggles AFK, with an optional message shown in the broadcast (`afk.now`,
`afk.message-suffix`). Staff with `essentials.afk.others` can toggle others with `/afk <player>`.

- Players go AFK automatically after `afk.auto-afk-seconds` (300) without activity, and are kicked
  after `afk.auto-kick-seconds` (off by default; exempt: `essentials.afk.kickexempt`).
- Activity is vanilla's idle tracker: moving, looking around, chatting, commands, using items.
- AFK players get `afk.actionbar`, a tag in `/list` and the tab list, and senders of private messages
  are told (`msg.target-afk`).
- `afk.invulnerable` protects AFK players from damage.

## /hat

Puts the held item on your head and the old helmet in your hand. With an empty hand it takes the hat
off. Helmets with Curse of Binding can't be taken off (except in creative).

## /repair

`/repair` or `/repair hand`: repairs the held item. `/repair all` (`essentials.repair.all`): every
damaged item in the inventory, armor and offhand. Items in `player.repair-blacklist` are skipped.

## Menus

| Command | Opens | Notes |
|---|---|---|
| `/enderchest` | Your ender chest | Blocked in combat by default |
| `/enderchest <player>` | Their ender chest | Read-only without `essentials.enderchest.modify` |
| `/workbench` | Crafting table | Works anywhere, nothing is placed |
| `/anvil` | Anvil | Never breaks, costs XP like a real anvil |
| `/invsee <player>` | 6-row chest showing their 36 slots, 4 armor slots and offhand | Read-only without `essentials.invsee.modify` |

All of these are vanilla screens, so vanilla clients open them normally. In read-only views every click
is refused and the screen is resynced.

/invsee layout: rows 1–4 are the inventory (hotbar first, like vanilla's slot order), then boots,
leggings, chestplate, helmet, offhand; the rest of the last row is locked.

## /clearinventory

Empties the inventory; armor and offhand too when `player.clear-inventory-includes-armor`.

## /suicide

Kills you (`suicide.done`), like `/kill` on yourself.

## Information

| Command | Shows |
|---|---|
| `/near [radius]` | Players in your dimension within `near.default-radius` (or the radius, with `essentials.near.radius`, up to `near.max-radius`), closest first with distances |
| `/seen <player>` | How long they've been online, or how long ago they were last seen (vanished players look offline) |
| `/whois <player>` | UUID, nickname, health, hunger, game mode, fly, god, location, balance, AFK, vanish, mute, jail, freeze, combat, first join, IP (`essentials.whois.ip`) — layout in `whois.body` |
| `/list` | Online players with nicknames and AFK tags. Vanished players only for `essentials.vanish.see` (tagged `[Hidden]`). Replaces vanilla /list |
| `/ping [player]` | Latency in ms |
| `/combat` | Remaining combat tag |
