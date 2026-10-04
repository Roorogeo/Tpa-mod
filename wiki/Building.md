# Building from Source

## Toolchain

| Tool | Version | Where it is set |
|---|---|---|
| Minecraft | 26.2 | `gradle.properties` → `minecraft_version` |
| Fabric Loader | 0.19.5 | `gradle.properties` → `loader_version` |
| Fabric Loom | 1.18 (`1.18-SNAPSHOT`, plugin id `net.fabricmc.fabric-loom`) | `gradle.properties` → `loom_version` |
| Fabric API | 0.161.0+26.2 | `gradle.properties` → `fabric_api_version` |
| fabric-permissions-api | 0.7.0 (Maven Central, bundled with `include`) | `gradle.properties` → `permissions_api_version` |
| Gradle | 9.7.1 (wrapper) | `gradle/wrapper/gradle-wrapper.properties` |
| Java | 25 | `build.gradle` → `options.release = 25` |

These are the versions of Fabric's official 26.2 example mod at the time of writing.

Minecraft 26.x is **not obfuscated**, so the project uses the non-remapping Loom plugin
(`net.fabricmc.fabric-loom`) and Mojang's official names everywhere (`ServerPlayer`,
`CommandSourceStack`, `Identifier`, ...). Dependencies use plain `implementation` instead of
`modImplementation`.

## Building

```sh
./gradlew build
```

The jar is written to `build/libs/essentials-<version>.jar` (a `-sources.jar` is produced too). It
already contains fabric-permissions-api.

## Regenerating PERMISSIONS.md

`PermissionNodes.java` has no Minecraft dependencies and a `main` method, so Java's source launcher
can run it directly:

```sh
java src/main/java/com/roorogeo/essentials/perm/PermissionNodes.java PERMISSIONS.md
```

CI fails if `PERMISSIONS.md` doesn't match the code.

## Project layout

```
src/main/java/com/roorogeo/essentials/
├── Essentials.java              entrypoint: loads config, registers events and commands
├── combat/CombatService         combat tags and combat-log handling
├── command/
│   ├── EssentialsCommand        shared base: permissions, cooldowns, combat/mute/jail/freeze blocking, messages
│   ├── CommandRegistry          creates every command, applies enable toggles and aliases
│   ├── admin/ chat/ economy/ kit/ moderation/ player/ teleport/ world/   one class per command
├── config/                      EssentialsConfig (config.json), DefaultMessages, ConfigManager
├── data/                        PlayerData, PlayerDataStore, NamedLocations (warps/jails), SpawnStore, KitStore, DataLoader
├── event/                       connection, damage and interaction event handlers
├── menu/                        chest views for /invsee and /enderchest, anywhere crafting/anvil
├── mixin/                       four small mixins, see Technical Notes
├── perm/                        PermissionNodes (registry), Perms (checks + configurable defaults)
├── service/                     AFK, chat, economy, freeze, homes, jail, kits, tab list, vanish
├── storage/AsyncFileWriter      background writer with coalescing and atomic replace
├── teleport/                    TeleportService, TpaService, SafeLocations, ChunkLoading, RandomTeleport
├── text/                        TextFormatter (& codes), Messages, DisplayNames
└── util/                        Durations, Names, PlayerLookup
```
