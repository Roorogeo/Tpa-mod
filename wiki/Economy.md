# Economy

Every player has a balance stored in their data file. New players start with
`economy.starting-balance` (100).

| Command | Permission | Description |
|---|---|---|
| `/balance [player]` (`/bal`, `/money`) | `essentials.balance` / `.others` | Show a balance |
| `/pay <player> <amount>` | `essentials.pay` | Pay someone, online or offline (`economy.pay-offline`) |
| `/baltop [page]` | `essentials.baltop` | Richest players and the server total |
| `/eco give <player> <amount>` | `essentials.eco` + `.give` | Add money |
| `/eco take <player> <amount>` | `essentials.eco` + `.take` | Remove money (fails if they have less) |
| `/eco set <player> <amount>` | `essentials.eco` + `.set` | Set a balance (`0` allowed) |
| `/eco reset <player>` | `essentials.eco` + `.reset` | Back to the starting balance |

## Rules

- Amounts are rounded to `economy.decimal-places` (2). Negative and zero payments are refused, and
  payments below `economy.min-payment` (0.01).
- Balances never go below 0 or above `economy.max-balance`.
- `/eco` and `/pay` work on offline players; online receivers of `/pay` get `pay.received`.
- `economy.enabled: false` turns every economy command off (`economy.disabled`).

## Formatting

```json
"economy": {
  "currency-symbol": "$",
  "currency-name-singular": "dollar",
  "currency-name-plural": "dollars",
  "format": "{symbol}{amount}",
  "decimal-places": 2,
  "group-thousands": true
}
```

| `format` | Result for 1234.5 |
|---|---|
| `{symbol}{amount}` | `$1,234.50` |
| `{amount} {name}` | `1,234.50 dollars` |
| `{amount}{symbol}` with symbol `€` and `decimal-places: 0` | `1,235€` |

## Other mods

Balances are plain numbers in `userdata/<uuid>.json` (`"balance": 100.0`), so other tools can read
them. Edit files only while the server is stopped (see [Data and Storage](Data-and-Storage.md)).
