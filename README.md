# ChatFilterMod

A Fabric client-side mod for Minecraft 26.3 that lets you hide chat messages from specific players by entering their nicknames.

## Features

- Hide chat messages from selected players — only on your client
- Add/remove ignored players directly in-game with commands
- Tab-autocompletion for player names (online players for `/addignore`, ignored list for `/removeignore`)
- Persistent ignore list saved to `config/chatfilter-ignored.txt`
- Confirmation messages in chat with `[ChatFilter]` prefix (these are never hidden by the mod itself)
- Works on any server — server owners do not need to install anything

## Commands

| Command | Description |
|---------|-------------|
| `/addignore <nick>` | Hide messages from this player |
| `/removeignore <nick>` | Show messages from this player again |
| `/listignore` | Show currently ignored players |
| `/chatfilter` | Show command help |

## Installation

### From source

1. Install **JDK 21+**
2. Clone this repository
3. Run `./gradlew build` (first run `./gradlew wrapper` if you don't have Gradle installed)
4. Find the compiled `.jar` in `build/libs/`
5. Place it in `.minecraft/mods/` of your Prism Launcher instance (or any Fabric instance)
6. Make sure **Fabric API** is installed

### Ready-made jar

Download `chatfilter-1.0.2.jar` from the Releases section and place it in your instance's `mods` folder.

## Usage

1. Launch Minecraft with your Fabric profile (version 26.3)
2. Open chat (`T`)
3. Type `/addignore PlayerName` — messages from PlayerName will no longer appear in your chat
4. Type `/listignore` — see who is hidden
5. Type `/removeignore PlayerName` — restore their messages

Ignored nicknames are case-insensitive and matched at word boundaries, so `Alex` will not hide messages from `Alexandra`.

## Security / Privacy

This mod:
- Does **not** collect, store, or transmit IP addresses
- Does **not** make any network requests outside the normal Minecraft client
- Does **not** modify server behavior or affect other players
- Stores ignored nicknames locally in `config/chatfilter-ignored.txt`

It is purely client-side chat filtering.

## Requirements

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.165.2+
- Java 21+

## License

MIT License — see [LICENSE](LICENSE).