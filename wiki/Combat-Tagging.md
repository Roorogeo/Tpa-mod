# Combat Tagging

## When players get tagged

A player is **tagged** when they hurt another player or another player hurts them. Both are tagged
for `combat.duration-seconds` (30). Every new hit resets the timer for both.

Indirect damage counts and is credited to the player behind it:

| Source | Credited to |
|---|---|
| Melee | The attacker |
| Arrows, spectral arrows, tridents, snowballs, eggs, fireballs, wind charges | The shooter (projectile owner) |
| Splash and lingering potions, area effect clouds | The thrower |
| TNT | The player who lit it |
| End crystals | The player who broke the crystal (vanilla credits the explosion to them) |
| Tamed wolves, cats, parrots and other pets | The pet's owner |

Attribution follows owner chains (e.g. an arrow shot by a player's pet... owner of owner), up to 4 levels.
Hurting yourself (your own TNT, arrow straight up) never tags.

With `combat.tag-on-mob-damage: true`, mobs hurting a player also tag them (only the player; the
message is `combat.tagged-mob`).

Players with `essentials.combat.bypass` are never tagged (but the other player still is).

## While tagged

- The action bar shows `combat.actionbar` (remaining seconds), refreshed twice a second.
- Commands in `combat.blocked-commands` are refused with `general.combat-blocked`. Default list:
  `/home /spawn /warp /tpa /tpahere /tpaccept /back /rtp /top /fly /god /heal /feed /vanish /enderchest`.
  Aliases of blocked commands are blocked too. Bypass: `essentials.combat.command.bypass`.
- Player-initiated teleports are refused (`teleport.in-combat`), including a `/tpa` accepted by the
  other player while you are tagged.
- `/combat` (`essentials.combat.check`) shows the remaining time.

## Entering combat

When a player goes from untagged to tagged:

- `combat.tagged` is sent (names the opponent);
- a pending teleport warmup is cancelled (`combat.cancel-teleport-on-tag`);
- flight is turned off for survival/adventure players (`combat.disable-fly-on-tag`);
- god mode is turned off (`combat.disable-god-on-tag`).

When the tag runs out: `combat.untagged`.

## Combat logging

If a tagged player **disconnects**, they are killed at the spot where they logged out, so their items
drop there (vanilla death rules apply, including `keepInventory`), and `combat.logout-broadcast` is
broadcast (`combat.broadcast-logout`).

Not punished:

- kicks (`/kick`, vanilla `/kick`, kicks from other mods or anti-cheats),
- bans and `/tempban`,
- the server shutting down or restarting,
- timeouts, if `combat.punish-timeouts` is `false` (a pulled network cable counts as combat logging
  by default).

How it tells them apart: every disconnect the *server* starts goes through one vanilla method, which a
small mixin watches (see [Technical Notes](Technical-Notes.md)). A player closing the game never goes
through it.

## Death

Dying clears the tag immediately.

## Configuration summary

```json
"combat": {
  "enabled": true,
  "duration-seconds": 30,
  "tag-on-mob-damage": false,
  "blocked-commands": ["home", "spawn", "warp", "tpa", "tpahere", "tpaccept", "back", "rtp", "top", "fly", "god", "heal", "feed", "vanish", "enderchest"],
  "cancel-teleport-on-tag": true,
  "disable-fly-on-tag": true,
  "disable-god-on-tag": true,
  "action-bar": true,
  "message-on-start": true,
  "message-on-end": true,
  "kill-on-logout": true,
  "punish-timeouts": true,
  "broadcast-logout": true
}
```

| Node | Default | Effect |
|---|---|---|
| `essentials.combat.bypass` | op | Never tagged |
| `essentials.combat.command.bypass` | op | Can use blocked commands and teleport while tagged |
| `essentials.combat.check` | everyone | `/combat` |
