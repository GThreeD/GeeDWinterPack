# GeeDWinterPack (Fabric) – Dynamic Winter / Snow Piles / Tracks

**Ziel:** Eine Welt, die sich *wie Winter anfühlt*: überall Schnee (visuell + gameplay), dynamische Schnee-Akkumulation
und “Footprints / Pathing” wie bei echtem Schnee – inklusive Verdichtung, schrittweisem Abtragen und Schmelzen bei
Wärmequellen.

**Mod-ID:** `geedwinterpack`  
**Name:** GeeDWinterPack  
**Minecraft:** `~1.21.11`  
**Java:** `>=21`  
**Fabric Loader:** `>=0.17.3`  
**Fabric API:** erforderlich

> Die Versionen/Dependencies stehen in [`fabric.mod.json`](src/main/resources/fabric.mod.json).

---

## Features (aktueller Stand)

### Immer-Winter Look & Feel

- Custom Snow-Render + Partikel statt Vanilla-Wetter
- Regen/Thunder/Weather-Sounds werden unterdrückt
- Server-Wettercycle wird überschrieben, damit es “Winter-Atmosphäre” bleibt

Code:

- Mixins: [`geedwinterpack.mixins.json`](src/main/resources/geedwinterpack.mixins.json)
- Server Weather + Snow Tick: [
  `ServerLevelMixin.java`](src/main/java/net/gthreed/geedwinterpack/mixin/ServerLevelMixin.java)
- Weather Renderer: [
  `WeatherEffectRendererMixin.java`](src/main/java/net/gthreed/geedwinterpack/mixin/WeatherEffectRendererMixin.java)
- Rain sounds block: [`SoundManagerMixin.java`](src/main/java/net/gthreed/geedwinterpack/mixin/SoundManagerMixin.java)

---

## Schnee-Systeme

### 1) SnowPile (Tile-Schnee)

Ein eigener Block, der intern ein 4×4-Grid (16 Zellen) speichert. Jede Zelle hat eine eigene Höhe (
Minecraft-Pixel-Layer).

- Grid: `4×4`
- Layer: `1..13` (1 Layer = 1/16 Blockhöhe)
- Verdichtung: mehrere Schritte stampfen, bevor wirklich “weggetragen” wird

Code:

- Logik: [`SnowPileBlockEntity.java`](src/main/java/net/gthreed/geedwinterpack/block/snowpile/SnowPileBlockEntity.java)
- Rendering (BER): [
  `SnowPileBlockEntityRenderer.java`](src/main/java/net/gthreed/geedwinterpack/block/snowpile/SnowPileBlockEntityRenderer.java)

Assets:

- Blockstates/Models/Item:
    - [`snow_pile.json`](src/main/resources/assets/geedwinterpack/blockstates/snow_pile.json)
    - [`snow_pile_cell.json`](src/main/resources/assets/geedwinterpack/blockstates/snow_pile_cell.json)
    - [`snow_pile_base.json`](src/main/resources/assets/geedwinterpack/models/block/snow_pile_base.json)
    - [`snow_pile_cell.json`](src/main/resources/assets/geedwinterpack/models/block/snow_pile_cell.json)
    - [`snow_pile.json` (item)](src/main/resources/assets/geedwinterpack/models/item/snow_pile.json)

### 2) Vanilla Snow (Normal-Schnee)

Optionaler Modus, bei dem statt Tile-Zellen der Vanilla-Block `Blocks.SNOW` genutzt wird (Layer 1..8).
Die “Weltlogik” (wo/wann Schnee wächst & schmilzt) bleibt gleich – nur die Darstellung/Storage ist einfacher und
serverfreundlicher.

> Der Vanilla-Modus ist gedacht als Performance-Alternative (z.B. für große Server).

---

## Footprints / Tracks

Entities (Player & Mobs) erzeugen Spuren:

- AABB → betroffene Zellen im Block werden berechnet
- “Verdichtung zuerst”: erst nach mehreren Tritten wird wirklich abgetragen

(Hook über Mixin auf Entity tick)

- [`LivingEntityMixin.java`](src/main/java/net/gthreed/geedwinterpack/mixin/LivingEntityMixin.java)

---

## Schnee-Akkumulation & Schmelzen

Server tickt Schnee-Wachstum rund um Spieler:

- pro Tick werden mehrere Zufallspunkte um jeden Spieler geprüft
- wächst schrittweise (Tile: einzelne Zellen; Vanilla: Layer)
- Wärmequellen (Torch, Campfire, Fire, Lava, Magma, …) lassen Schnee schmelzen

Code:

- [`SnowAccumulation.java`](src/main/java/net/gthreed/geedwinterpack/CustomRendering/SnowAccumulation.java)

---

## Installation (Client / Server)

1. Fabric Loader installieren (>= 0.17.3)
2. Fabric API installieren
3. Mod-JAR nach `mods/` legen
    - Client: `.minecraft/mods/`
    - Server: `./mods/`
4. Starten mit Minecraft `1.21.11`

---

## Build (aus Source)

### Voraussetzungen

- JDK 21
- Git
- Gradle Wrapper (im Repo)

### Build

```bash
git clone <dein-repo-url>
cd <repo-ordner>
./gradlew build
