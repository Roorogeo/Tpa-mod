# Teleportation

Every Essentials teleport (homes, warps, spawn, TPA, /back, /top, /rtp, staff teleports, jails) goes
through the same pipeline:

```
command ──► combat check ──► cooldown check ──► warmup ──► load destination chunks ──► safety check ──► move ──► /back + sound + cooldown
              (player-initiated only)            (cancel on move / damage / combat / death)       (async)          (nearby search)
```

## Warmup (delay)

- Length: `teleport.delay-seconds` (default 3), or `commands.<name>.teleport-delay-seconds` for one command.
- The player gets `teleport.warmup` in chat and a countdown in the action bar (`teleport.warmup-actionbar`).
- Cancelled when the player:
  - moves more than `teleport.move-tolerance` blocks (`teleport.cancel-on-move`) — turning the camera is fine;
  - takes damage (`teleport.cancel-on-damage`);
  - enters combat (`combat.cancel-teleport-on-tag`);
  - dies;
  - starts another teleport (`teleport.cancelled-replaced`).
- Skipped with `essentials.teleport.delay.bypass`.

## Cooldown

- Length: `teleport.cooldown-seconds` (default 5). One shared cooldown covers every teleport command,
  except commands with their own `commands.<name>.teleport-cooldown-seconds` (by default only `/rtp`, 300 s),
  which track their own timer.
- Starts when the teleport happens (`teleport.cooldown-only-on-success`), so cancelled warmups cost nothing.
- Skipped with `essentials.teleport.cooldown.bypass`.

## Combat

Player-initiated teleports are refused while combat tagged (`teleport.in-combat`), unless the player has
`essentials.combat.command.bypass`. This applies at the moment the teleport starts *and* to the player
who moves when a `/tpa` is accepted later.

## Cross-dimension teleports

Locations store their dimension (`minecraft:overworld`, `minecraft:the_nether`, any datapack
dimension), and the player is moved with vanilla's dimension-changing teleport. If a stored dimension
no longer exists the player gets `teleport.world-missing`.

## Chunk loading

The destination chunk (and its neighbours) are loaded or generated **asynchronously** with a temporary
ticket. The server keeps ticking meanwhile. If it takes longer than
`teleport.chunk-load-timeout-seconds`, the teleport is cancelled (`teleport.chunk-timeout`).

## Safe destinations

With `teleport.safety.enabled`, the destination is checked:

- the player's hitbox fits without touching blocks (no suffocation);
- feet, head and ground aren't lava or a block from `teleport.safety.unsafe-blocks` (fire, magma, cactus, ...);
- water at the destination is refused when `teleport.safety.water-is-unsafe`;
- there is ground to stand on, unless the player can fly or is floating in water.

If the exact spot isn't safe, the nearest safe spot within `horizontal-radius` × `vertical-radius` is
used (searching the same height first, then up and down alternately). If none is found the teleport is
refused with `teleport.unsafe`. Creative and spectator players skip the check
(`skip-for-creative-and-spectator`), and so does anyone with `essentials.teleport.safety.bypass`.

## /back

The location before every Essentials teleport is stored (`back.record-teleports`), and so is the death
location for players with `essentials.back.ondeath` (`back.record-deaths`). After a death the player
gets `back.death-hint` when they respawn. `/back` itself is a normal teleport (warmup, cooldown,
combat check) and records the current spot, so `/back` twice goes back and forth.

## /top

Moves you onto the highest motion-blocking block (leaves ignored) in your column, through the normal
pipeline. Op-only by default.

## /rtp

1. Picks the dimension: yours if it is in `rtp.allowed-dimensions`, otherwise `rtp.target-dimension`.
2. Checks combat and the `/rtp` cooldown **before** searching.
3. Picks a random point between `min-radius` and `max-radius` from `rtp.center` (`spawn` or `"x,z"`),
   inside the world border if `respect-world-border`.
4. Loads that chunk asynchronously, finds the surface, skips `blacklisted-biomes` and unsafe spots.
5. Retries up to `max-attempts` times, then starts a normal teleport (warmup applies).

## Staff teleports

`/tp`, `/tphere`, `/tpall`, `/spawn <player>`, `/warp <name> <player>` and `/rtp <player>` move other
players **instantly**, ignoring their warmup, cooldown and combat tag. `/tp` for yourself uses
`commands.tp` (0/0 by default). Chunk loading and the safety check still apply.

## Sound

`teleport.sound` (default enderman teleport) plays at the destination for everyone nearby. Set it to
`""` to disable, or any sound id like `minecraft:entity.player.levelup`.
