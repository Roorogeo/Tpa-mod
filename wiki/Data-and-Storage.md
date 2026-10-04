# Data and Storage

Everything Essentials stores is plain JSON under `config/essentials/`.

```
config/essentials/
├── config.json          settings (see Configuration)
├── messages.json        every player-facing message (see Messages)
├── kits.json            kits (see Kits)
├── warps.json           warps
├── jails.json           jails
├── spawn.json           the /setspawn location (absent until /setspawn is used)
└── userdata/
    ├── 6384e2b2-184b-3bf5-8ecc-f10ca7a6563c.json
    └── ...              one file per player, named by UUID
```

## How data is read and written

The server thread never touches the disk for Essentials data:

1. **Startup.** When the server starts, every file above is read on a background thread
   (`Essentials-IO`). Until that finishes, joining players wait in the login phase (Fabric's login
   synchronizer). With a few thousand player files this takes well under a second.
2. **While running.** All data lives in memory. When something changes, the player is marked *dirty*.
   Dirty players are serialized (on the server thread — fast, and it gives a consistent snapshot) and
   handed to the background writer:
   - when the player logs out,
   - every `storage.autosave-seconds` (default 300),
   - immediately for warps, jails, spawn, kits and `/essentials reload`.
3. **Writing.** The background writer writes each file to `<name>.tmp` first and then atomically moves it
   into place, so a crash mid-write never leaves a half-written file. If a file is queued again before
   it was written, only the newest version is written.
4. **Shutdown.** On server stop, every online player's `last-seen` is updated, everything dirty is queued,
   and the writer is flushed (waiting at most 30 seconds). This is the only moment the server thread waits
   for Essentials I/O, which is what "flush on stop" means.

Things that are deliberately **not** saved: pending teleport requests, teleport warmups and cooldowns,
command cooldowns, combat tags and AFK state. They reset on restart.

## Player files

```json
{
  "uuid": "6384e2b2-184b-3bf5-8ecc-f10ca7a6563c",
  "name": "Alice",
  "nickname": "&cAlly",
  "first-join": 1791068259803,
  "last-seen": 1791070000000,
  "session-start": 0,
  "last-ip": "203.0.113.7",
  "homes": {
    "base": { "world": "minecraft:overworld", "x": 120.5, "y": 64.0, "z": -33.5, "yaw": 90.0, "pitch": 0.0 }
  },
  "last-location": { "world": "minecraft:the_nether", "x": 10.5, "y": 70.0, "z": 4.5, "yaw": 0.0, "pitch": 0.0 },
  "logout-location": { "world": "minecraft:overworld", "x": 0.5, "y": 100.0, "z": 0.5, "yaw": 0.0, "pitch": 0.0 },
  "balance": 130.0,
  "mail": [
    { "sender": "Bob", "sender-id": "9f9d51bc-70ef-31ca-9c14-f307980a29d8", "time": 1791069000000, "message": "hi", "read": false }
  ],
  "ignored": [],
  "kit-uses": { "starter": 1791068259832 },
  "muted-until": 0,
  "mute-reason": "",
  "jail": {
    "jail": "jail", "remaining": 300000, "reason": "griefing",
    "previous": { "world": "minecraft:overworld", "x": 1.5, "y": 64.0, "z": 1.5, "yaw": 0.0, "pitch": 0.0 },
    "placed": true
  },
  "god": false,
  "social-spy": false,
  "vanished": false,
  "frozen": false
}
```

| Field | Meaning |
|---|---|
| `name` | Last known username (updated on join; used by `/seen`, `/whois`, offline lookups). |
| `nickname` | Nickname with `&` codes, without the `nick.prefix`. Absent = no nickname. |
| `first-join`, `last-seen`, `session-start` | Epoch milliseconds. `session-start` is 0 while offline. |
| `last-ip` | IP address of the last login. Shown by `/whois` only to holders of `essentials.whois.ip`. |
| `homes` | Home name → location. |
| `last-location` | Where `/back` goes. Absent until the player teleports or dies. |
| `logout-location` | Where the player logged out; shown by `/whois` for offline players and used as the release point when someone is jailed while offline. |
| `balance` | Money. |
| `mail` | Mail received; `read` mail is kept until `/mail clear`. |
| `ignored` | UUIDs this player ignores. |
| `kit-uses` | Kit name → epoch millis of the last claim (cooldowns). |
| `muted-until` | `0` = not muted, `-1` = permanent, otherwise epoch millis when the mute ends. |
| `jail` | Present while jailed. `remaining` is milliseconds (`-1` = until `/unjail`). |
| `god`, `social-spy`, `vanished`, `frozen` | Toggles that survive relogs (subject to `player.persist-god`, `vanish.persist`). |

Fields with no value (`null`) are left out.

## Locations

Warps, jails, spawn, homes and `/back` all use the same shape:

```json
{ "world": "minecraft:overworld", "x": 0.5, "y": 64.0, "z": 0.5, "yaw": 180.0, "pitch": 0.0 }
```

`world` is the dimension id, so custom dimensions from datapacks or mods work. If a location points at a
dimension that no longer exists, teleporting there fails with `teleport.world-missing` instead of crashing.

## Editing files by hand

- **`config.json`, `messages.json`, `kits.json`**: edit any time, then run `/essentials reload`.
- **`warps.json`, `jails.json`, `spawn.json` and `userdata/`**: these are held in memory and written back by
  the server, so edits made while the server runs will be overwritten. **Stop the server first**, edit,
  then start it.
- Keep the JSON valid. If a player file can't be parsed, the error is logged with the file name and that
  file is skipped (the player starts with fresh data, and the broken file is overwritten the next time
  they are saved — keep a backup).

## Backups

Back up the whole `config/essentials/` folder together with your world. Because files are replaced
atomically, copying the folder while the server runs is safe; you get each file either before or after a
write, never halfway.

## Moving from another server

Player files are keyed by UUID, so moving `userdata/` between servers works as long as both use the same
UUIDs (both online-mode, or both offline-mode with the same names).

## Memory use

All player files are kept in memory. Each one is roughly the size of its JSON file (usually well under
10 KB), so even servers with many thousands of known players only need a few megabytes for it.
