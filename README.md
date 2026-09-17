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
