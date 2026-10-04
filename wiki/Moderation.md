# Moderation

Mutes, temporary bans, kicks, jails, vanish, freeze and sudo. All moderation commands default to
operators (level 2), and every "can't be targeted" protection has its own `.exempt` node.

## Durations

`/mute`, `/jail` and `/tempban` take durations made of `<number><unit>` parts:

| Unit | Meaning | Example |
|---|---|---|
| `w` | weeks | `2w` |
| `d` | days | `1d12h` |
| `h` | hours | `6h` |
| `m` | minutes | `30m` |
| `s` | seconds | `90s` (a bare `90` also means seconds) |
| `permanent`, `perm`, `forever` | no end | |

For `/mute` and `/jail` the duration is optional: if the first word after the player (and jail) parses as a
duration, it is the duration and the rest is the reason; otherwise the whole text is the reason. Empty
reasons are shown as `general.no-reason`.

## Mute

| Command | Permission |
|---|---|
| `/mute <player> [duration] [reason]` | `essentials.mute` |
| `/unmute <player>` | `essentials.unmute` |
| — | `essentials.mute.exempt` (can't be muted) |

- Works on offline players (the mute is stored in their player file).
- Without a duration, `mute.default-duration` is used (`permanent` by default).
- Muting someone who is already muted replaces their mute (new duration and reason).
- Muted players can't chat and can't use the commands in `mute.blocked-commands`
  (`msg, reply, me, mail, broadcast`). Add `sudo` there too if you ever let normal players use it.
- Mutes are wall-clock based: a 1h mute ends one hour later whether the player is online or not.
  The player is told when it runs out (`mute.expired`). `/whois` shows the remaining time.

## Temporary bans

| Command | Permission |
|---|---|
| `/tempban <player> <duration> [reason]` | `essentials.tempban` |
| — | `essentials.tempban.exempt` (checked while the target is online) |

The ban is written to **vanilla's** `banned-players.json` with an expiry date, so:

- vanilla handles the login check and shows the remaining time on the disconnect screen;
- vanilla `/pardon <player>` lifts it early;
- `/banlist` lists it.

`tempban.max-duration` (e.g. `"30d"`) limits how long a ban can be; `permanent` is then refused too. Leave it
empty for no limit. The online player is disconnected with `tempban.kick-message`; a tempban is a
server-initiated disconnect, so it never counts as combat logging.

## Kick

| Command | Permission |
|---|---|
| `/kick <player> [reason]` | `essentials.kick` |
| — | `essentials.kick.exempt` |

Replaces vanilla `/kick` (disable `commands.kick` to keep vanilla's). The reason defaults to
`kick.default-reason`. Kicked players are never punished as combat loggers.

## Jail

| Command | Permission |
|---|---|
| `/setjail <name>` | `essentials.setjail` — create or move a jail at your position |
| `/deljail <name>` | `essentials.deljail` — delete a jail; everyone in it is released |
| `/jails` | `essentials.jail` |
| `/jail <player> <jail> [duration] [reason]` | `essentials.jail` |
| `/unjail <player>` | `essentials.unjail` |
| — | `essentials.jail.exempt` (checked while the target is online) |

How a jail sentence works:

- The player is teleported to the jail (across dimensions, chunk loaded first). Offline players are
  jailed when they next join; join teleports to spawn are skipped for jailed players.
- They are kept within `jail.radius` blocks of the jail (default 6); walking, flying or being pushed out
  pulls them back (`jail.escape`).
- They may only use the Essentials commands in `jail.allowed-commands`
  (`msg, reply, mail, list, ping, balance, seen, realname`). Every other Essentials command is refused
  with `general.jailed-blocked`.
- With `jail.prevent-interaction` (default on) they can't break or place blocks, use items or blocks, or
  attack (`jail.no-interact`).
- With `jail.allow-chat: false` they can't use public chat either (`chat.jailed`).
- Time only runs while they are online, unless `jail.count-offline-time` is `true`. On join they are told
  how long is left (`jail.time-left`).
- When released (time up, `/unjail`, or the jail was deleted) they return to where they were jailed from
  (`jail.release-location: previous`) or to spawn (`spawn`).

Jails are stored in `config/essentials/jails.json` (same format as warps).

## Vanish

| Command | Permission |
|---|---|
| `/vanish` | `essentials.vanish` |
| `/vanish <player>` | `essentials.vanish.others` |
| — | `essentials.vanish.see` — see vanished players everywhere |

A vanished player is hidden from everyone without `essentials.vanish.see`:

- **in the world** — the server stops sending the player's entity to them (this uses the
  `ChunkMapAccessor`/`TrackedEntityInvoker` mixins and the `broadcastToPlayer` hook, see
  [Technical Notes](Technical-Notes.md));
- **in the tab list** (`vanish.hide-from-tab-list`);
- in `/list`, `/near`, player-name tab completion, `/msg`/`/tpa` targets ("player not found"), and
  `/seen` shows them as offline;
- **join/leave messages** are not shown (`vanish.silent-join-quit`), and toggling vanish broadcasts a fake
  leave/join message (`vanish.fake-messages`).

Other options: `vanish.persist` (still vanished after relogging), `vanish.invulnerable`,
`vanish.action-bar` (reminder in the action bar). Vanishing while combat-tagged is blocked by default
(`vanish` is in `combat.blocked-commands`).

## Freeze

| Command | Permission |
|---|---|
| `/freeze <player>` | `essentials.freeze` (toggles) |
| — | `essentials.freeze.exempt` |

Frozen players are held at the spot where they were frozen (they can still look around); they are
dismounted and stop flying. The frozen state is saved, so relogging doesn't escape it. Options:

- `freeze.prevent-interaction` — no breaking, placing, using or attacking (`freeze.no-interact`);
- `freeze.block-commands` + `freeze.allowed-commands` (`msg, reply`) — only those Essentials commands work;
- `freeze.invulnerable` — frozen players can't be hurt.

## Sudo

| Command | Permission |
|---|---|
| `/sudo <player> <command>` | `essentials.sudo` |
| `/sudo <player> c:<message>` | `essentials.sudo` |
| — | `essentials.sudo.exempt` |

`/sudo Steve spawn` runs `/spawn` as Steve, **with Steve's permissions** (it can't be used to give anyone
extra rights). `/sudo Steve c:hello` makes Steve say "hello" in chat, going through the chat format, mute and
ignore checks. With `chat.format-enabled: false` the message is sent as unsigned player chat (Steve's
client never signed it), so vanilla clients may show it with a "not secure" marker.

## Messages

`mute.*`, `unmute.*`, `tempban.*`, `kick.*`, `jail.*`, `vanish.*`, `freeze.*`, `sudo.*`, plus
`general.muted-blocked`, `general.jailed-blocked`, `general.frozen-blocked`, `chat.muted`, `chat.jailed`.
See [Messages](Messages.md).
