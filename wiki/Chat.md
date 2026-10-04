# Chat

## Public chat

With `chat.format-enabled` (default), every chat line is rebuilt from `chat.format`:

```json
"chat.format": "{prefix}{displayname}{suffix}&7: &f{message}"
```

| Placeholder | Value |
|---|---|
| `{displayname}` | Nickname (with `nick.prefix`) or account name; hovering shows the real name |
| `{name}`, `{player}` | Account name |
| `{prefix}`, `{suffix}` | `prefix`/`suffix` meta from the permission mod (LuckPerms `setprefix`/`setsuffix`), & codes parsed |
| `{world}` | Dimension name |
| `{message}` | The message |

Colors in messages:

| Node | Allows |
|---|---|
| `essentials.chat.color` | `&0-&9 &a-&f` and `&#RRGGBB` |
| `essentials.chat.format` | `&l &m &n &o &r` |
| `essentials.chat.magic` | `&k` |

Codes a player isn't allowed to use stay in the message as typed.

Formatted chat is sent as system messages (that is how every chat-formatting plugin works with vanilla
clients), so vanilla's chat reporting/signing doesn't apply to it. Set `chat.format-enabled: false` to
keep vanilla's signed chat untouched; mutes, jail rules and ignore lists still work in that mode
(ignored players' messages are filtered per recipient while keeping the signature).

### Local chat

`chat.local-radius` ≥ 0 turns on local chat: only players in the same dimension within that many
blocks see a message (`chat.local-format`). Messages starting with `chat.global-prefix` (`!`) go to
everyone (`chat.global-format`). If nobody is in range the sender gets `chat.nobody-heard`.

## Private messages

| Command | Permission |
|---|---|
| `/msg <player> <message>` (`/tell`, `/w`, `/m`, `/t`, `/whisper`, `/pm`) | `essentials.msg` |
| `/reply <message>` (`/r`) | `essentials.reply` |

- Formats: `msg.format-sender`, `msg.format-receiver` (placeholders `{sender}`, `{receiver}`, `{message}`).
- `/reply` answers the last person you messaged or who messaged you. Works with the console both ways.
- Colors in private messages need `essentials.msg.color`.
- Messages to someone who ignores you look sent but aren't delivered.
- `msg.target-afk` warns you when the receiver is AFK (`messaging.notify-afk`).
- Vanished players can't be messaged by players who can't see them (`messaging.allow-messaging-vanished`).
- Muted players can't use `/msg`, `/reply`, `/me`, `/mail` or `/broadcast` (`mute.blocked-commands`).

## Social spy

`/socialspy` (`essentials.socialspy`) toggles a copy of every private message (`msg.format-spy`) and
every mail (`mail.spy`) between other players. The toggle is saved; spies who lose the permission stop
receiving copies.

## Mail

| Command | Permission |
|---|---|
| `/mail` or `/mail read [page]` | `essentials.mail` + `essentials.mail.read` |
| `/mail send <player> <message>` | `essentials.mail.send` |
| `/mail sendall <message>` | `essentials.mail.sendall` |
| `/mail clear` | `essentials.mail.clear` |

- Mail works for offline players and is kept in their data file.
- Unread mail is announced on join (`mail.unread-join`, `mail.notify-on-join`).
- Limits: `mail.max-mails-per-player`, `mail.max-length`, `mail.send-cooldown-seconds`.
- Dates use `time.date-format`.

## Ignore

`/ignore <player>` toggles ignoring; `/ignore` lists. Ignoring hides the player's public chat, `/me`,
private messages, mail and teleport requests. Staff with `essentials.ignore.exempt` can't be ignored
(and a player who already ignores them still sees them).

## Nicknames

| Command | Permission |
|---|---|
| `/nick <nickname>` / `/nick off` | `essentials.nick` |
| `/nick <player> <nickname\|off>` | `essentials.nick.others` |
| `/realname <nickname>` | `essentials.realname` |

- `nick.prefix` (`~`) is shown before every nickname so players can tell nicknames from real names.
- Length (`nick.min-length`, `nick.max-length`) and allowed characters (`nick.allowed-pattern`) are
  checked on the text without codes.
- Codes need `essentials.nick.color`, `essentials.nick.format`, `essentials.nick.magic`.
- Nicknames can't match another player's nickname or real name unless `nick.allow-duplicates`.
- Nicknames appear in chat, `/msg`, `/me`, `/list`, `/near`, join/quit messages and the tab list
  (`tab-list.enabled`, format `tablist.format`).

## /me and /broadcast

- `/me <action>` → `me.format`. Uses the chat color nodes. Replaces vanilla `/me`.
- `/broadcast <message>` → `broadcast.format`, & codes always allowed (staff command).

## Join and quit messages, MOTD

- `join-quit.custom-messages` replaces vanilla's yellow join/leave lines with `join-quit.join` and
  `join-quit.quit` (placeholders `{player}`, `{displayname}`).
- A player's very first join uses `join-quit.first-join` (`{count}` = number of players who ever joined).
- Vanished players join and leave silently (`vanish.silent-join-quit`).
- `motd` is sent on join (`join-quit.motd`), with `{player}`, `{displayname}`, `{online}`. Use `\n`
  for more lines.
