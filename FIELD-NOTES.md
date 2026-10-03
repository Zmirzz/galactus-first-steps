---
kind: game
title: "Galactus invasion boss for Minecraft Fabric 1.21.1"
game: Minecraft Java Edition
game_version: 1.21.1
platform: Windows
engine: Java
route: Fabric loader API
tools: [Fabric Loom 1.10.5, Fabric API 0.116.6+1.21.1, Fabric Loader 0.16.14, JDK 21, Gradle 8.14.3, universal-modder]
status: working
---

Built a persistent herald/preparation/invasion/occupation event, a colossal custom mob, four shield anchors, original native geometry/textures, recipes and three survival endings. Verified native client interactions and a remapped production server installation.

1. Minecraft max-health attribute caps at 1024; use 1000 HP rather than asking for 1200 and assuming the unclamped value.
2. Load the target chunk before reading a surface height for a new-region spawn. A bad height can put a giant below the floor; use no gravity and explicit surface positioning for a scripted colossal entity.
3. Deferred entity visibility makes UUID lookup null briefly. Allow warmup on startup/return/new origin and associate event entities with persistent UUIDs; discard obsolete bound entities to prevent duplicates.
4. Environmental damage immunity must allow generic-kill for administrative cleanup. Tests otherwise select stale mobs rather than the tracked event boss.
5. Use singular 1.21 data paths: recipe, advancement, loot_table, tags/block. Written-book pages need RawFilteredPair<Text>.
6. Dev server and client should use separate run folders. Use a development-only file harness plus RCON for repeatable interaction and screenshots without moving the user's keyboard/mouse.
7. Test the remapped JAR in a normal Fabric server, not only Loom runServer. Keep its world and launcher separate from the user's game.
8. um win capture by HWND worked for video, but did not resolve a PID for audio; the handoff demo is silent.

Evidence: MODLOG.md, release/VALIDATION.md, evidence/live-tests.txt, production and dev lab logs. No game files or runtime caches in release. No shared knowledge-base PR submitted.
