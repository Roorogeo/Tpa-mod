# Homes

## Commands

| Command | Permission |
|---|---|
| `/home [name]` | `essentials.home` |
| `/home <player>:<name>` | `essentials.home.others` |
| `/sethome [name]` | `essentials.sethome` |
| `/sethome <player>:<name>` | `essentials.sethome.others` |
| `/delhome <name>` / `/delhome <player>:<name>` | `essentials.delhome` / `.others` |
| `/homes [player]` | `essentials.homes` / `.others` |

Names are lower-cased and must match `homes.name-pattern` (letters, numbers, `_`, `-`, up to 32).
Without a name, `/sethome` uses `homes.default-home-name` (`home`).

`/home` without a name goes to the home called `home`; if there is no such home but exactly one home,
to that one; otherwise it shows the list (`homes.list-when-ambiguous`).

`/homes` prints clickable names (click = `/home <name>`, hover = coordinates) and the count as
**used/max**, e.g. `Homes (2/3): base, farm`.

Other players' homes work for offline players too: `/home Steve:base`, `/homes Steve`.

Homes are stored in the player's data file with their dimension, so homes in the Nether or End work.
`homes.allowed-dimensions` can restrict where homes may be set.

## Limits

The limit is calculated every time it is needed, so permission changes apply immediately:

1. If the player has `essentials.sethome.unlimited`: the limit is `homes.absolute-max-homes`
   (or truly unlimited when that is `-1`).
2. Otherwise: the **highest** `essentials.sethome.multiple.<number>` the player has, or
   `homes.default-max-homes` (3) if they have none.
3. That number is clamped to `homes.absolute-max-homes` (10). Nobody can go over it, whatever their
   nodes say. Set it to `-1` to remove the cap.

| Player's nodes | default 3, absolute 10 | default 3, absolute -1 |
|---|---|---|
| none | 3 | 3 |
| `multiple.5` | 5 | 5 |
| `multiple.5`, `multiple.20` | 10 | 20 |
| `unlimited` | 10 | unlimited |

Nodes are checked from `homes.multiple-scan-limit` (100) downwards; raise it if you grant numbers above 100.

### What happens at the limit

- **Overwriting** an existing home (`/sethome base` when `base` exists) always works and never counts
  against the limit.
- A **new** home when the player already has `max` homes is refused:
  `You have 3/3 homes. Delete one with /delhome.` (`home.limit-reached`).
- If the limit **drops** below the number of homes a player has (e.g. a rank expired), all existing
  homes keep working, but new ones are refused (`home.over-limit`) until they delete enough homes to be
  under the limit.

### LuckPerms examples

```
/lp group default permission set essentials.sethome.multiple.2 true
/lp group vip permission set essentials.sethome.multiple.5 true
/lp group mvp permission set essentials.sethome.multiple.10 true
/lp group admin permission set essentials.sethome.unlimited true
```

Groups inherit, so an MVP who also inherits VIP's `multiple.5` gets 10: the highest number wins.

## Importing homes from `homewarps.json`

If the server used a home mod that stored everything in `homewarps.json`, Essentials takes its homes
and warps over automatically, so players keep every home when you switch:

1. Stop the server.
2. Remove the old home mod's jar from `mods/` and add Essentials. **Leave `homewarps.json` where it is.**
3. Start the server. While loading, Essentials reads the file, adds every home to its owner's
   Essentials data and every warp to `warps.json`, then renames the file to `homewarps.json.imported`.
   The log says what happened:

   ```
   homewarps.json: imported 158 homes of 51 players (0 already existed in Essentials, 0 invalid)
   homewarps.json: imported 1 warps (0 already existed in Essentials, 0 invalid)
   Finished importing /srv/mc/config/homewarps.json; the file was renamed to homewarps.json.imported
   ```

Details:

- The file is looked for in `config/`, in sub-folders of `config/` (e.g. `config/<mod>/homewarps.json`),
  in the server folder, in the world folder and in the world's `data/` folder.
- Every home is imported, **including homes above a player's limit** and homes of players who haven't
  joined since Essentials was installed. Players over their limit keep using all their homes; they just
  can't add new ones until they are under the limit (see [What happens at the limit](#what-happens-at-the-limit)).
- Names keep their spelling in lower case (`Base` → `base`). Characters that can't be typed in a command
  are replaced with `_`. If two names end up the same (`Base` and `base`), the second gets a number
  (`base2`). Every renamed home is listed in the log.
- Dimensions are kept, so Nether and End homes work as before.
- If a player already has an Essentials home with the same name, the Essentials home is kept.
- Warps (`"warps"` in the file) are imported into `warps.json` with the same naming rules. A warp that
  already exists in Essentials is kept. Imported warps are usable by everyone by default, like any warp
  (`essentials.warp.<name>`, see [Warps and Spawn](Warps-and-Spawn.md)).
- The import runs once. To run it again, rename `homewarps.json.imported` back to `homewarps.json` and
  restart; homes that already exist are skipped, so nothing is duplicated.
- Set `homes.import-homewarps` to `false` in `config.json` to turn the import (homes and warps) off.
- If the player files themselves fail to load, the import is skipped (and logged), so existing
  Essentials data is never overwritten.
