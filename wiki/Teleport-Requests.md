# Teleport Requests (TPA)

| Command | Who moves | Permission |
|---|---|---|
| `/tpa <player>` | you → them | `essentials.tpa` |
| `/tpahere <player>` | them → you | `essentials.tpahere` |
| `/tpaccept [player]` | | `essentials.tpaccept` |
| `/tpdeny [player]` | | `essentials.tpdeny` |
| `/tpacancel [player]` | | `essentials.tpacancel` |

## Flow

1. `/tpa Steve` sends Steve `tpa.received` plus clickable **[Accept] [Deny]** buttons
   (`tpa.buttons`; or the text hint `tpa.hint` when `tpa.clickable-buttons` is false). You get
   `tpa.sent` with a **[Cancel]** button.
2. Steve clicks Accept (or types `/tpaccept`, or `/tpaccept YourName` when several requests are pending).
3. The player who moves goes through the normal teleport pipeline: combat check, cooldown and warmup
   all apply **to the mover**. For `/tpa` that is you; for `/tpahere` it is Steve. The destination is
   resolved when the warmup ends, so it follows the other player if they walk away.
4. If the destination player logs out or dies before the warmup ends, the teleport is cancelled
   (`teleport.destination-gone`).

## Rules

- Requests expire after `tpa.timeout-seconds` (120); both sides are told.
- A new request to the same player replaces the old one. With `tpa.max-outgoing` = 1 (default) a
  request to someone else replaces your previous one (`tpa.replaced`).
- `/tpaccept` and `/tpdeny` without a name pick the **newest** request.
- `/tpacancel` without a name cancels all your outgoing requests.
- If either player leaves, pending requests between them are cancelled (`tpa.player-left`).
- If the target **ignores** you, you see the normal "request sent" message but they never receive it.
- Vanished players can't be found by players who can't see them.
- Combat: `/tpa`, `/tpahere` and `/tpaccept` are in `combat.blocked-commands` by default. A tagged
  mover is also refused when the request is accepted.

## Per-command timing

`/tpa` and `/tpahere` warmups/cooldowns come from `commands.tpa` and `commands.tpahere`
(`teleport-delay-seconds`, `teleport-cooldown-seconds`), falling back to the shared `teleport.*` values.
