# World

Time and weather commands. They replace vanilla `/time` and `/weather`; disable `commands.time` or
`commands.weather` in `config.json` to keep the vanilla versions instead.

## Commands

| Command | Permission | Default |
|---|---|---|
| `/time [world]` | `essentials.time` | everyone — shows the time |
| `/time set <day\|noon\|night\|midnight\|ticks> [world]` | `essentials.time.set` | op |
| `/time add <time> [world]` | `essentials.time.add` | op |
| `/day [world]` | `essentials.day` | op |
| `/night [world]` | `essentials.night` | op |
| `/weather clear [duration]` | `essentials.weather` + `essentials.weather.clear` | op |
| `/weather rain [duration]` | `essentials.weather` + `essentials.weather.rain` | op |
| `/weather thunder [duration]` | `essentials.weather` + `essentials.weather.thunder` | op |
| `/sun [duration]` | `essentials.sun` | op |
| `/rain [duration]` | `essentials.rain` | op |

`[world]` is a dimension id such as `minecraft:overworld` or `minecraft:the_end`; without it, the
dimension you are in is used (the overworld from the console).

Because every weather type has its own node, you can, for example, let builders clear the weather but not
start storms:

```
/lp group builder permission set essentials.weather true
/lp group builder permission set essentials.weather.clear true
/lp group builder permission set essentials.sun true
```

## Time in 26.x: world clocks

Since Minecraft 26.1 the time of day belongs to **world clocks**. Each dimension type has a default clock;
the overworld and the End have one, the Nether does not. Essentials works with these clocks:

- `/time` shows the clock as `HH:MM` (tick 0 = 06:00), the tick within the day and the day number:
  `The time in minecraft:overworld is 12:00 (6000 ticks, day 3).`
- `/time set day|noon|night|midnight` and `/day` / `/night` **move the clock forward** to the next
  matching time marker, like vanilla `/time set` does in 26.x, so the day counter never goes backwards.
- `/time set <number>` sets the clock's total tick count directly.
- `/time add <time>` accepts vanilla time units: `100` or `100t` ticks, `30s` seconds, `2d` in-game days.
- In a dimension without a clock (the Nether by default) these commands fail with `world-time.no-clock`.

Vanilla `/time` also manages clock rates and timelines; if you need those, disable `commands.time` so the
vanilla command is registered instead.

## Weather

Weather is shared by all dimensions since 26.1, so `/weather` has no world argument.

- The duration uses vanilla time units (`600` ticks, `30s`, `1d` = one in-game day = 20 minutes).
- Without a duration, `world.weather-duration-seconds` is used; when that is `0` (the default) the length
  is random, drawn from the same ranges vanilla uses for natural weather (`ServerLevel.RAIN_DELAY`,
  `RAIN_DURATION`, `THUNDER_DURATION`).
- `/sun` = `/weather clear`, `/rain` = `/weather rain`; they only need their own node.

## Messages

`world-time.query`, `world-time.set`, `world-time.added`, `world-time.no-clock`, `weather.set`,
`weather.name.clear`, `weather.name.rain`, `weather.name.thunder`. Translate `weather.name.*` to change how
weather types are named in `weather.set`.
