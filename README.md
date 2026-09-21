# AutoFishT

Fabric mod for Minecraft 1.21 that automates the fishing minigame click-timing bar by reading the title HUD text and pressing left click when the target triangle enters the highlighted zone.

## How it works

Some servers/minigames render a fishing skill-check as a title/subtitle bar, e.g.:

```
TITLE:    ━━━▲━━■■■■━━━━━━━━━
```

AutoFishT mixes into `InGameHud.setTitle` to intercept this text every time the server updates it, parses the character positions of `▲` (moving marker) and `■` (target zone), and simulates a left-click the instant the marker enters the zone.

No screen capture, no pixel scanning — pure text parsing off the actual title packet, so detection is effectively instant (bounded only by the server's own update rate).

## Features

- Reads title text directly (no rendering/screenshot overhead)
- Edge-triggered click: fires once per zone entry, not spammed every tick while inside
- runs synchronously on the network/render thread, no tick-queue wait
- Left-click only

## Requirements

- Minecraft 1.21
- Fabric Loader 0.15+
- Fabric API
- Java 21

## Project structure

```
src/
 └─ client/
     ├─ java/saki/autofisht/
     │   ├─ AutoFishTClient.java       # parsing + click logic
     │   └─ mixin/TitleLoggerMixin.java # hooks InGameHud.setTitle
     └─ resources/
         └─ autofisht.client.mixins.json
```

## Building

```bash
./gradlew build
```

Output `.jar` will be in `build/libs/`.

## Usage

AutoFishT only clicks the skill-check bar. It does not cast, reel in, or recast the rod. Pair it with [XPlus Autofish](https://www.curseforge.com/minecraft/mc-mods/x-autofish), which handles that loop.

1. Install AutoFishT and XPlus Autofish in your `mods` folder. The Fabric build of XPlus Autofish needs [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) and [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config).
2. Join the server, hold a fishing rod, and cast once.
3. XPlus Autofish reels in when a fish bites and recasts after a short delay.
4. When the server shows the skill-check bar, AutoFishT clicks the moment `▲` enters the `■` zone.

Press `V` (default) to open the XPlus Autofish config screen. These options help on servers:

- **Sound-Based Detection**: detects bites by sound instead of hook motion. Try it if bites go unnoticed in multiplayer.
- **Persistent Mode**: recasts every 10 seconds if needed, which covers server lag and plugins that clear hooks.
- **Clearlag Chat Pattern**: recasts when the server announces an entity clear.
- **Recast Delay** and **Random Delay**: set the wait between reel-in and recast.

AutoFishT has no toggle. It stays active while loaded. Remove the jar to turn it off.

## Configuration / tuning

Detection logic lives in `AutoFishTClient.onTitleUpdate(String title)`:

```java
int triangleIdx = title.indexOf('▲');
int zoneStart   = title.indexOf('■');
int zoneEnd     = title.lastIndexOf('■');
boolean overlapping = triangleIdx >= zoneStart && triangleIdx <= zoneEnd;
```

If your server's bar uses different characters or a different title format, update the character checks and `indexOf` targets accordingly.

## Known limitations

- Only works if the minigame is rendered via vanilla title/subtitle (`Text` sent through `InGameHud.setTitle`). If a server/mod instead draws this as a custom HUD overlay, this mixin won't see it — a different hook into that mod's render method would be needed.
- Click timing is bounded by server tick/update rate — this mod adds effectively no client-side delay, but can't click faster than the server sends new bar positions.
- No GUI/config screen — all tuning is via source edit + rebuild.

## Disclaimer

Automating gameplay actions may violate the rules of servers you play on. Use at your own risk and check the relevant server's terms before running this mod there.
