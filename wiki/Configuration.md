# Configuration (`config/essentials/config.json`)

Every setting lives in `config/essentials/config.json`. The file is created on first start with every
key and its default. Keys use kebab-case. Missing keys get their default and are written back, so after
an update new options appear automatically. Apply changes with `/essentials reload` (no restart).

What reload applies:

| Change | Applies |
|---|---|
| Any setting in this file | Immediately after `/essentials reload` |
| `commands.<name>.enabled` and `aliases` | Immediately: commands are re-registered and every player's command tree is resent |
| `permissions.defaults`, `permissions.op-level` | Immediately (the default cache is cleared) |
| `messages.json` | Immediately |
| `kits.json` | Immediately |

> A vanilla command that Essentials replaced (e.g. `/tp`) only comes back after a restart or `/reload`
> when you disable the Essentials version.

Durations in this file are plain numbers of **seconds** unless a key says otherwise. Places where players
type durations (`/mute`, `/tempban`, `/jail`, `/createkit`) accept `30s`, `10m`, `2h`, `1d`, `1w`,
combinations like `1h30m`, a bare number (seconds) or `permanent`.

---

## `permissions`

```json
"permissions": {
  "op-level": 2,
  "defaults": {
    "essentials.spawn": "all",
    "essentials.kit.*": "all",
    "...": "..."
  }
}
```

| Key | Default | Meaning |
|---|---|---|
| `op-level` | `2` | Operator level that the `op` default means. Vanilla levels: 1 moderators, 2 gamemasters (command blocks), 3 admins, 4 owners. `/op` grants 4 unless `op-permission-level` in server.properties says otherwise. |
| `defaults` | every node | The fallback for each node when no permission mod has a value. Values: `all` (or `true`), `op`, `op:<level>`, `none` (or `false`). Filled with every node on first start. |

Pattern nodes use `*` in this map (`essentials.kit.*`, `essentials.warp.*`, `essentials.sethome.multiple.*`).
You can also add a specific instance, which beats the pattern: `"essentials.kit.vip": "op"`.

See [Permissions](Permissions.md) for the full lookup order.

---

## `teleport`

```json
"teleport": {
  "delay-seconds": 3,
  "cooldown-seconds": 5,
  "cancel-on-move": true,
  "move-tolerance": 0.5,
  "cancel-on-damage": true,
  "cooldown-only-on-success": true,
  "chunk-load-timeout-seconds": 15,
  "sound": "minecraft:entity.enderman.teleport",
  "sound-volume": 1.0,
  "sound-pitch": 1.0,
  "warmup-action-bar": true,
  "safety": { ... }
}
```

| Key | Default | Meaning |
|---|---|---|
| `delay-seconds` | `3` | Warmup before a player-initiated teleport happens. `0` = instant. Per command: `commands.<name>.teleport-delay-seconds`. Bypass: `essentials.teleport.delay.bypass`. |
| `cooldown-seconds` | `5` | Time between teleports. `0` = off. Shared by all teleport commands unless a command has its own (`commands.<name>.teleport-cooldown-seconds`). Bypass: `essentials.teleport.cooldown.bypass`. |
| `cancel-on-move` | `true` | Cancel the warmup if the player moves. Looking around never cancels. |
| `move-tolerance` | `0.5` | Blocks a player may drift during the warmup (knockback, slabs) before it counts as moving. |
| `cancel-on-damage` | `true` | Cancel the warmup when the player takes damage. |
| `cooldown-only-on-success` | `true` | Start the cooldown when the teleport happens. `false` starts it when the command is used, so cancelled warmups also cost a cooldown. |
| `chunk-load-timeout-seconds` | `15` | How long to wait for the destination chunk to load or generate. Chunks load asynchronously, so the server never freezes. |
| `sound` | `minecraft:entity.enderman.teleport` | Sound played at the destination. Any sound id; `""` disables it. |
| `sound-volume` / `sound-pitch` | `1.0` / `1.0` | Sound settings. |
| `warmup-action-bar` | `true` | Countdown in the action bar during the warmup (`teleport.warmup-actionbar` message). |

### `teleport.safety`

| Key | Default | Meaning |
|---|---|---|
| `enabled` | `true` | Check destinations and search for a nearby safe spot. If none is found the teleport is refused (`teleport.unsafe`). |
| `skip-for-creative-and-spectator` | `true` | Creative and spectator players skip the check. |
| `horizontal-radius` | `3` | How far sideways to search for a safe spot. |
| `vertical-radius` | `8` | How far up and down to search. |
| `unsafe-blocks` | lava, fire, soul fire, magma, cactus, campfires, berry bush, wither rose, powder snow, pointed dripstone, portals | Blocks never stood in or on. Lava is always unsafe. |
| `water-is-unsafe` | `false` | Treat water at feet or head height as unsafe. |

A spot is safe when the player's hitbox fits without touching blocks, nothing dangerous is at the feet,
head or ground, and there is ground to stand on (players who can fly don't need ground; floating in
water counts as safe unless `water-is-unsafe`). Bypass: `essentials.teleport.safety.bypass` (default
`none`).

---

## `homes`

| Key | Default | Meaning |
|---|---|---|
| `default-max-homes` | `3` | Homes for players without any `essentials.sethome.multiple.<n>` node. |
| `absolute-max-homes` | `10` | Hard cap nobody can exceed, whatever their nodes say. `-1` removes the cap. |
| `multiple-scan-limit` | `100` | Highest `<n>` checked for `essentials.sethome.multiple.<n>` (the absolute cap is used if it is higher). |
| `default-home-name` | `home` | Name used by `/sethome` and `/home` without a name. |
| `name-pattern` | `[a-z0-9_-]{1,32}` | Allowed home names (a Java regex, checked after lower-casing). |
| `list-when-ambiguous` | `true` | `/home` with several homes and none named `default-home-name` lists them. `false` uses `default-home-name`. |
| `allowed-dimensions` | `[]` | Dimensions where homes may be set, e.g. `["minecraft:overworld"]`. Empty = everywhere. |

Limit rules are explained on [Homes](Homes.md).

---

## `warps`

| Key | Default | Meaning |
|---|---|---|
| `name-pattern` | `[a-z0-9_-]{1,32}` | Allowed warp names. `others` is reserved. |
| `per-warp-permissions` | `true` | Each warp also needs `essentials.warp.<name>` (defaults to everyone). |
| `list-only-usable` | `true` | `/warps` only lists warps the player may use. |

## `spawn`

| Key | Default | Meaning |
|---|---|---|
| `teleport-on-first-join` | `true` | New players are moved to `/spawn` when they first join. |
| `teleport-on-join` | `false` | Every join moves the player to `/spawn`. |
| `respawn-at-spawn` | `true` | Players without a bed or charged respawn anchor respawn at `/spawn`. |

## `rtp`

| Key | Default | Meaning |
|---|---|---|
| `min-radius` | `200` | Minimum distance from the center. |
| `max-radius` | `5000` | Maximum distance from the center. |
| `center` | `spawn` | `spawn` (the dimension's spawn point) or `"x,z"`, e.g. `"0,0"`. |
| `max-attempts` | `12` | Random spots tried before giving up (`rtp.failed`). Each attempt loads its chunk asynchronously. |
| `allowed-dimensions` | `["minecraft:overworld"]` | Dimensions `/rtp` works in. |
| `target-dimension` | `minecraft:overworld` | Where players are sent when they use `/rtp` elsewhere. |
| `respect-world-border` | `true` | Never pick spots outside the world border. |
| `blacklisted-biomes` | all oceans and rivers | Biomes never chosen. |

The default `/rtp` cooldown is 300 seconds, set in `commands.rtp.teleport-cooldown-seconds`.

## `tpa`

| Key | Default | Meaning |
|---|---|---|
| `timeout-seconds` | `120` | Seconds before an unanswered request expires. |
| `clickable-buttons` | `true` | Show clickable `[Accept] [Deny]` and `[Cancel]` buttons. `false` shows `tpa.hint` instead. |
| `max-outgoing` | `1` | Pending outgoing requests per player; a new one beyond this replaces the oldest. |

## `back`

| Key | Default | Meaning |
|---|---|---|
| `record-teleports` | `true` | Remember the location before every Essentials teleport. |
| `record-deaths` | `true` | Remember the death location (only for players with `essentials.back.ondeath`). |

---

## `combat`

| Key | Default | Meaning |
|---|---|---|
| `enabled` | `true` | Turn combat tagging on or off. |
| `duration-seconds` | `30` | Tag length; every hit resets it. |
| `tag-on-mob-damage` | `false` | Mobs hurting a player also tag them. |
| `blocked-commands` | `home, spawn, warp, tpa, tpahere, tpaccept, back, rtp, top, fly, god, heal, feed, vanish, enderchest` | Essentials commands blocked while tagged. Use primary names or aliases (no slash). Bypass: `essentials.combat.command.bypass`. |
| `cancel-teleport-on-tag` | `true` | Getting tagged cancels a pending teleport warmup. |
| `disable-fly-on-tag` | `true` | Getting tagged turns off flight (not for creative/spectator). |
| `disable-god-on-tag` | `true` | Getting tagged turns off god mode. |
| `action-bar` | `true` | Show the remaining time in the action bar. |
| `message-on-start` / `message-on-end` | `true` / `true` | Chat messages when the tag starts and ends. |
| `kill-on-logout` | `true` | Kill players who disconnect while tagged, so their items drop where they logged out. Kicks, bans and server shutdown are never punished. |
| `punish-timeouts` | `true` | Treat a timed-out connection like a logout. |
| `broadcast-logout` | `true` | Broadcast `combat.logout-broadcast` when someone combat logs. |

Details: [Combat Tagging](Combat-Tagging.md).

---

## `chat`

| Key | Default | Meaning |
|---|---|---|
| `format-enabled` | `true` | Format public chat with `chat.format`. `false` keeps vanilla's signed chat (ignore lists, mutes and jail rules still apply). |
| `local-radius` | `-1` | Radius in blocks for local chat; `-1` = everyone hears everything. |
| `global-prefix` | `!` | With local chat on, messages starting with this go to everyone (`chat.global-format`). |
| `log-to-console` | `true` | Log formatted chat lines to the console. |

## `messaging`

| Key | Default | Meaning |
|---|---|---|
| `allow-messaging-vanished` | `false` | Allow `/msg` to vanished players by players who can't see them. |
| `notify-afk` | `true` | Tell senders when the receiver is AFK (`msg.target-afk`). |

## `mail`

| Key | Default | Meaning |
|---|---|---|
| `max-mails-per-player` | `50` | Mailbox size. |
| `max-length` | `256` | Longest mail in characters. |
| `page-size` | `5` | Mails per `/mail read` page. |
| `notify-on-join` | `true` | Tell players about unread mail when they join. |
| `send-cooldown-seconds` | `5` | Seconds between two `/mail send` (bypass: `essentials.command.cooldown.bypass`). |

## `nick`

| Key | Default | Meaning |
|---|---|---|
| `max-length` / `min-length` | `16` / `2` | Length limits, counted without color codes. |
| `allowed-pattern` | `[A-Za-z0-9_]+` | Allowed characters once color codes are removed. |
| `prefix` | `~` | Shown in front of every nickname (may contain `&` codes). `""` for none. |
| `allow-duplicates` | `false` | Allow nicknames that match another player's nickname or real name. |

## `afk`

| Key | Default | Meaning |
|---|---|---|
| `auto-afk-seconds` | `300` | Idle time before a player is marked AFK. `0` disables it. Idle = no movement, looking, chatting, commands or interaction (vanilla's own idle tracker). |
| `auto-kick-seconds` | `0` | Idle time before a kick. `0` disables it. Exempt: `essentials.afk.kickexempt`. |
| `broadcast` | `true` | Broadcast AFK changes (`afk.now`, `afk.back`). `false` only tells the player. |
| `action-bar` | `true` | Show `afk.actionbar` to AFK players. |
| `invulnerable` | `false` | AFK players can't be hurt. |

---

## `economy`

| Key | Default | Meaning |
|---|---|---|
| `enabled` | `true` | Disable to turn off every economy command (`economy.disabled`). |
| `starting-balance` | `100.0` | Balance of new players, and what `/eco reset` sets. |
| `currency-symbol` | `$` | `{symbol}` in `format`. |
| `currency-name-singular` / `-plural` | `dollar` / `dollars` | `{name}` in `format` (singular when the amount is exactly 1). |
| `format` | `{symbol}{amount}` | How amounts look, e.g. `{amount} {name}` → `1,000.00 dollars`. |
| `decimal-places` | `2` | Amounts are rounded to this many places. |
| `group-thousands` | `true` | `1,000,000.00` instead of `1000000.00`. |
| `max-balance` | `1000000000000` | Nobody can hold more. |
| `min-payment` | `0.01` | Smallest `/pay`. |
| `pay-offline` | `true` | `/pay` works for offline players. |
| `baltop-page-size` | `10` | Entries per `/baltop` page. |

## `kits`

| Key | Default | Meaning |
|---|---|---|
| `first-join-kit` | `""` | Kit given to new players on their first join (no cooldown is started). |
| `overflow` | `drop` | Items that don't fit: `drop` at the player's feet, or `deny` the whole kit when the inventory is too full. |
| `reserved-names` | `["others", "cooldown"]` | Names `/createkit` refuses (they would collide with `essentials.kit.others` / `essentials.kit.cooldown.bypass`). |
| `name-pattern` | `[a-z0-9_-]{1,32}` | Allowed kit names. |

The kits themselves are in `kits.json`, see [Kits](Kits.md).

---

## `mute`

| Key | Default | Meaning |
|---|---|---|
| `blocked-commands` | `msg, reply, me, mail, broadcast` | Commands muted players can't use (chat is always blocked). |
| `default-duration` | `permanent` | Used when `/mute` gets no duration. A duration like `1h`, or `permanent`. |

## `jail`

| Key | Default | Meaning |
|---|---|---|
| `release-location` | `previous` | Where released players go: `previous` (where they were jailed from) or `spawn`. |
| `count-offline-time` | `false` | Sentences also run while the player is offline. |
| `allowed-commands` | `msg, reply, mail, list, ping, balance, seen, realname` | The only Essentials commands jailed players may use. |
| `allow-chat` | `true` | Jailed players may chat. |
| `prevent-interaction` | `true` | Jailed players can't break, place, use items or blocks, or attack. |
| `radius` | `6.0` | Players leaving this radius around the jail are pulled back. |

## `freeze`

| Key | Default | Meaning |
|---|---|---|
| `prevent-interaction` | `true` | Frozen players can't break, place, use or attack. |
| `block-commands` | `true` | Frozen players can only use `allowed-commands`. |
| `allowed-commands` | `msg, reply` | Commands frozen players may use. |
| `invulnerable` | `true` | Frozen players can't be hurt. |

## `vanish`

| Key | Default | Meaning |
|---|---|---|
| `hide-from-tab-list` | `true` | Remove vanished players from the tab list of players who can't see them. |
| `silent-join-quit` | `true` | No join/leave messages for vanished players. |
| `fake-messages` | `true` | Vanishing broadcasts a fake leave message; reappearing a fake join message. |
| `persist` | `true` | Stay vanished after relogging. |
| `invulnerable` | `true` | Vanished players can't be hurt. |
| `action-bar` | `true` | Remind vanished players (`vanish.actionbar`). |

## `tempban`

| Key | Default | Meaning |
|---|---|---|
| `max-duration` | `""` | Longest `/tempban` allowed, e.g. `30d`. Empty = no limit. |

---

## `player`

| Key | Default | Meaning |
|---|---|---|
| `heal-removes-negative-effects` | `true` | `/heal` removes harmful effects. |
| `heal-extinguishes` | `true` | `/heal` puts out fire. |
| `god-prevents-hunger` | `true` | God mode keeps hunger full. |
| `persist-god` | `true` | God mode survives relogging. |
| `max-fly-speed` | `0.5` | Fly speed at `/speed 10` (vanilla 0.05 at `/speed 1`). |
| `max-walk-speed` | `0.5` | Walk speed at `/speed 10` (vanilla 0.1 at `/speed 1`). |
| `clear-inventory-includes-armor` | `true` | `/clearinventory` also clears armor and offhand. |
| `repair-blacklist` | `[]` | Item ids `/repair` never repairs, e.g. `["minecraft:elytra"]`. |

## `near`

| Key | Default | Meaning |
|---|---|---|
| `default-radius` | `200` | Radius of `/near` without an argument. |
| `max-radius` | `1000` | Largest radius allowed with `/near <radius>`. |

## `tab-list`

| Key | Default | Meaning |
|---|---|---|
| `enabled` | `true` | Show `tablist.format` (nickname and AFK tag) in the tab list. `false` leaves vanilla names. |

## `join-quit`

| Key | Default | Meaning |
|---|---|---|
| `custom-messages` | `true` | Replace vanilla join/leave messages with `join-quit.join` / `join-quit.quit`. |
| `motd` | `true` | Send the `motd` message on join. |
| `first-join-broadcast` | `true` | Use `join-quit.first-join` for a player's first join. |

## `world`

| Key | Default | Meaning |
|---|---|---|
| `weather-duration-seconds` | `0` | Length of `/sun`, `/rain` and `/weather` without a duration. `0` = vanilla's random length. |

## `storage`

| Key | Default | Meaning |
|---|---|---|
| `autosave-seconds` | `300` | How often changed player data is written. Data is also written on logout and on stop. |
| `pretty-print` | `true` | Indent the player files (easier to read and edit by hand). |

---

## `commands`

One entry per command, keyed by its primary name:

```json
"commands": {
  "home": {
    "enabled": true,
    "aliases": ["h"],
    "cooldown-seconds": 0,
    "teleport-delay-seconds": -1,
    "teleport-cooldown-seconds": -1
  }
}
```

| Key | Meaning |
|---|---|
| `enabled` | `false` doesn't register the command at all (and keeps a vanilla command with the same name). |
| `aliases` | Extra names. Each alias is a full copy of the command with the same permission. An alias that matches a vanilla command replaces it. |
| `cooldown-seconds` | Seconds between uses of this command. Bypass: `essentials.command.cooldown.bypass`. Separate from the teleport cooldown. |
| `teleport-delay-seconds` | Teleport commands only: warmup for this command. `-1` = `teleport.delay-seconds`. |
| `teleport-cooldown-seconds` | Teleport commands only: cooldown for this command, tracked separately from the shared one. `-1` = shared `teleport.cooldown-seconds`. |

### Default command table

| Command | Default aliases | Teleport warmup / cooldown |
|---|---|---|
| spawn | | shared |
| setspawn | | |
| home | h | shared |
| sethome | | |
| delhome | remhome, rmhome | |
| homes | | |
| warp | | shared |
| setwarp | createwarp | |
| delwarp | remwarp, rmwarp | |
| warps | | |
| tpa | tpask, call | shared (applies to the player who moves) |
| tpahere | | shared (applies to the player who moves) |
| tpaccept | tpyes | |
| tpdeny | tpno | |
| tpacancel | tpcancel | |
| tp | tpo | 0 / 0 |
| tphere | s, tpohere | 0 / 0 |
| tpall | | 0 / 0 |
| back | | shared |
| top | | shared |
| rtp | wild, randomtp | shared warmup / 300 s own cooldown |
| msg | tell, w, m, t, whisper, pm | |
| reply | r | |
| mail | email | |
| ignore | unignore | |
| nick | nickname | |
| realname | | |
| me | action | |
| broadcast | bc, bcast | |
| socialspy | spy | |
| heal | | |
| feed | eat | |
| fly | | |
| god | godmode, tgm | |
| speed | | |
| gamemode | gm | |
| gmc, gms, gma, gmsp | | |
| afk | away | |
| hat | head | |
| repair | fix | |
| enderchest | ec, echest | |
| workbench | wb, craft | |
| anvil | | |
| invsee | | |
| clearinventory | ci, clearinvent | |
| suicide | | |
| near | nearby | |
| seen | | |
| whois | | |
| list | online, who, playerlist | |
| ping | pong | |
| combat | combattag, ct | |
| balance | bal, money | |
| pay | | |
| baltop | balancetop | |
| eco | economy | |
| kit | | |
| kits | | |
| createkit | mkkit | |
| delkit | rmkit | |
| mute | | |
| unmute | | |
| tempban | tban | |
| kick | | |
| jail | | |
| jails | | |
| setjail | createjail | |
| deljail | remjail, rmjail | |
| unjail | | |
| vanish | v | |
| freeze | | |
| sudo | | |
| time | | |
| day | | |
| night | | |
| weather | | |
| sun | | |
| rain | | |
| essentials | ess | |

Staff teleports (`tp`, `tphere`, `tpall`, `/spawn <player>`, `/warp <name> <player>`, `/rtp <player>`)
move other players without warmup, cooldown or combat checks.

## Example: a hardcore PvP server

```json
"teleport": { "delay-seconds": 10, "cooldown-seconds": 60, "cancel-on-damage": true },
"combat": { "duration-seconds": 45, "tag-on-mob-damage": true, "punish-timeouts": true },
"homes": { "default-max-homes": 1, "absolute-max-homes": 3 },
"commands": {
  "rtp": { "enabled": true, "aliases": ["wild"], "cooldown-seconds": 0, "teleport-delay-seconds": 15, "teleport-cooldown-seconds": 1800 },
  "back": { "enabled": false, "aliases": [], "cooldown-seconds": 0, "teleport-delay-seconds": -1, "teleport-cooldown-seconds": -1 }
}
```

## Example: a relaxed survival server

```json
"teleport": { "delay-seconds": 0, "cooldown-seconds": 0, "safety": { "enabled": true } },
"combat": { "enabled": false },
"homes": { "default-max-homes": 5, "absolute-max-homes": -1 },
"permissions": { "defaults": { "essentials.nick": "all", "essentials.nick.color": "all", "essentials.top": "all" } }
```
