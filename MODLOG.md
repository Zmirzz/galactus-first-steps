# Galactus: The Coming Hunger

Target: Minecraft Java 1.21.1, Fabric, Java 21. User authorizes selecting the version for a future CurseForge modpack. Work stays in this isolated project; existing Minecraft worlds and launcher settings are not changed.

Route: Fabric loader API, native custom entities and renderers, persistent world invasion controller, recipes, configurable destruction. Film-inspired original Minecraft geometry and textures; no extracted film/game assets in release.

Design: silver herald warning; three-day preparation; cinematic arrival; four cosmic anchors feed Galactus; escalating hunger, raids and bounded terrain consumption. Victory through Ultimate Nullifier, dimensional banishment, or herald redemption. Loss leaves an occupied world with a recoverable final fight. Repeatable admin commands and separate development world for verification.

Recon: installed vanilla versions 1.21.1 and 26.2, no active Java client or existing saves found. System Java is JRE 25 (no compiler). Isolated terminal setup fails with helper_unknown_error; host execution works. No matching task memories. Read universal-modder mod-any-game and Minecraft playbook; relevant KB entry is passthrough, not boss implementation.

## Implementation and verification

- First vertical slice: Fabric dev server and native client loaded the custom Galactus; GPU screenshot inspected (`evidence/galactus-slice.png`).
- Full invasion: native entities, original 64x64/16x16 textures, five recipes, journal, world PersistentState and configuration. Boss health is 1000 (Minecraft clamps max-health attributes at 1024); anchors have 80 health.
- Live gates: four anchors sustain shield; player-attributed attacks break all four and drop shards; actual held item use in survival channels Nullifier for 5 seconds and consumes the device. Confirmed NULLIFIER victory.
- Live redemption: actual client entity interaction establishes allegiance; player-attributed damage below the health threshold triggers HERALD victory.
- Live portal: native block interaction consumes all 32 Ender Pearls; valid beacon/obsidian/gold frame charges for 90 seconds; leaving the defense radius pauses charge. Confirmed PORTAL victory.
- Hunger reaches OCCUPIED; saving, server restart and reconnection retained occupation and four anchors. Empty Overworld (player moved to Nether) pauses counters. Return resumes without duplicate entities.
- A new-region test exposed below-ground spawning and deferred entity loads. Fixed synchronous surface chunk reads, gravity-free ground positioning, five-second warmup on warning/server start/return, and event UUID ownership cleanup. New-region test now reports exactly 1 boss, 1 herald, 4 anchors and stable Y=-60 over repeated checks.
- Administrative generic-kill now works; reset cleans loaded cosmic entities and raid mobs. A stale first-slice boss caused early herald test selectors to hit the wrong entity; final test uses player-attributed damage and entity count assertions.
- JUnit: 3 tests, 0 failures, for full NBT roundtrip, redemption gates and occupied/saved semantics.
- Production test: separate `run-prod` installed official Fabric server launcher, Fabric API and final remapped JAR. Loaded 1295 recipes and 1400 advancements without mod errors. Client connected, invasion spawned at surface, all four anchors present, development-only test command correctly absent. Native screenshot inspected/retained.
- Captured a 34-second 1280x720/30fps real gameplay video via universal-modder GPU capture. No audio stream was captured; the delivered demo is deliberately silent. Original take and native fallback frames retained outside release.

## Lab and backups

Existing `.minecraft` saves, mods and profiles were not modified. All game testing uses `run`, `run-client`, and `run-prod` below this project.

Pristine lab backup: `C:\Users\razva\.universal-modder\backups\galactus-pristine-lab\20261003-153646.zip` (um backup; initial lab settings). Later experimental worlds retained as development artifacts, never included in release/source package.

RCON binds 127.0.0.1 on lab-only ports 25578/25579. Development commands and client file harness only activate in Fabric development environment. Future dev lab launch: `build.ps1 runServer`, `build.ps1 runClient -PgalactusLab`. Installed production mod has no automation listener.

## Deliverables

Release JAR, source ZIP, Romanian README, default config example, preview and silent MP4. Package excludes Minecraft files, libraries, JDK, Gradle caches, test worlds, logs and auth state. Code uses MIT; Marvel characters remain Marvel's. No publication or shared-KB PR authorized or performed.

## Revision 1.1.0 — Silver Surfer and presentation

User asked for a less crude result and specifically Silver Surfer. Replaced the primitive character art with original articulated models and individually packed painted UV islands in 512px atlases. Silver Surfer has anatomical chest/waist detail, elbow/knee joints, a wide surfing stance, tapered board rails, bright silver shading and a cosmetic wake. Galactus has layered armor, helmet channels and swept vanes, cheek/nose/mouth geometry, gauntlets and knees. Emissive masks light eyes and energy accents.

Changed herald movement from a small orbit to a smooth player approach/hover. It holds position inside interaction reach. Registry ID stays `galactus:herald`; loaded events refresh the display name to Silver Surfer. Added timed dialogue and localized Romanian/English stage titles, circular rift particles and two original synthesized OGG stingers.

Live visual review corrected overly dark silver, board foreshortening and fractional-UV holes in tiny eyes/rails. Safe-distance rift review exposed Minecraft's default 32-block particle delivery limit; sky particles now use forced per-player delivery for nearby viewers within 256 blocks. Captures use real native rendering.

Build/JUnit passed after the final changes. Native sigil interaction allied the herald, entity/recipe/NBT/shield assertions passed, and player-attributed damage plus broken anchors produced `victory=HERALD`. Remapped JAR 1.1.0 loaded in the separate normal Fabric server and a newly generated natural world; development command absent as intended. Existing lab save retained across restart.

Backups: `galactus-before-visual-revision` snapshot 20261003-174243 (run); `galactus-production-before-v11` snapshot 20261003-175658 (run-prod). Natural world remains under run-prod/galactus-v11-natural. A production restart must wait for process completion: natural-world shutdown spent about 50 seconds saving chunks, and an early relaunch was rejected by the world lock. No forced termination or save deletion.

New deliverables go into `release-1.1.0`; original release directory remains as the earlier version. No public upload.
