# Galactus: The Coming Hunger

Un mod survival pentru **Minecraft Java 1.21.1 + Fabric**. Silver Surfer anunță venirea lui Galactus; ai timp să te pregătești înainte ca uriașul să înceapă să consume lumea. Interpretare Minecraft originală, inspirată de *The Fantastic Four: First Steps*, cu armură violet, cască cu extensii laterale și Silver Surfer pe o placă argintie.

## Instalare în modpackul CurseForge

1. Creează un profil Minecraft **1.21.1**, cu loader **Fabric 0.16.14 sau mai nou** și Java 21.
2. Adaugă **Fabric API** pentru 1.21.1. Versiunea verificată este **0.116.6+1.21.1**.
3. În folderul `mods` al profilului pune **galactus-first-steps-1.1.0.jar**.
4. Pornește o lume. Modul se instalează atât pe client, cât și pe server pentru o lume multiplayer privată.

Nu funcționează pe Forge, NeoForge, Bedrock sau alte versiuni Minecraft. Nu necesită GeckoLib sau un resource pack separat. Modelele și texturile sunt incluse în JAR. Pentru exportul unui modpack CurseForge personal, JAR-ul local poate fi inclus în folderul de overrides/mods; pentru listarea publică respectă [cerințele CurseForge pentru dependențe și override mods](https://support.curseforge.com/support/solutions/articles/9000197908-exporting-a-modpack-for-curseforge-project-submission).

## Cum se joacă

Heraldul apare după **două zile active** în Overworld (aproximativ 40 de minute). Poți chema avertismentul mai devreme: construiește un **Cosmic Receiver** și folosește-l. Primești jurnalul **The Coming Hunger**, cu instrucțiuni, și receptorul care îți arată etapa și coordonatele.

După avertisment ai **trei zile Minecraft de pregătire** (60 de minute). Cronometrul folosește timpul activ de joc: somnul nu sare peste el. Urmează 45 de secunde de sosire, apoi invazia. Evenimentul se salvează cu lumea și se oprește când Overworld-ul este gol.

Galactus are **1.000 HP**. Patru ancore cosmice îi susțin scutul. Atacă ancorele cu arme sau proiectile; fiecare are 80 HP și lasă un **Cosmic Shard**. Distruge-le pe toate pentru a expune bossul. Raidurile apără zona, iar loviturile cosmice marchează un cerc cu patru secunde înainte de impact. Ieși din cerc. Foamea ajunge la maxim după 20 de minute de invazie; atunci lumea intră în ocupație, cu raiduri și corupție și în jurul jucătorilor aflați departe.

### Trei soluții

| Soluție | Pregătire | Execuție |
|---|---|---|
| Ultimate Nullifier | Patru fragmente din ancore, Nether Star, două diamante, două Echo Shards | Cu toate ancorele distruse, ține click dreapta 5 secunde la maximum 64 de blocuri de Galactus. Dispozitivul se consumă. |
| Portal dimensional | Dimensional Core, beacon, patru obsidian, patru blocuri de aur, Phase Conductor și 32 Ender Pearls | După distrugerea ancorelor, activează nucleul și apără-l în raza de 48 de blocuri timp de 90 de secunde. |
| Sprijinul heraldului | Redemption Sigil | Click dreapta pe Silver Surfer cu sigiliul; distruge ancorele și redu viața lui Galactus la 35% sau mai puțin. Heraldul îl alungă. |

Poți învinge și forma expusă în luptă directă. Toate soluțiile rămân disponibile după ocupare. Victoria oprește invazia și oferă un **World Savior**. Corupția de teren deja produsă rămâne în lume.

### Portalul

Nucleul trebuie să fie la maximum **128 de blocuri de punctul inițial al invaziei**. Blocul beacon stă direct sub nucleu; nu trebuie să fie pornit. La nivelul nucleului, construiește:

```text
Aur       Obsidian  Aur
Obsidian  Nucleu    Obsidian
Aur       Obsidian  Aur
```

Click dreapta pe nucleu cu Phase Conductor, având **32 Ender Pearls** în inventar. Perlele sunt consumate. Încărcarea se oprește când nimeni nu îl apără și se pierde dacă strici cadrul. Reparația necesită o nouă activare și combustibil. Nucleul și cadrul sunt protejate de consumul de teren al invaziei; jucătorii le pot sparge.

### Rețete

Obține Cosmic Receiver pentru a debloca în recipe book toate cele cinci rețete. Poți vedea obiectele și în categoria creativă **Galactus: The Coming Hunger**, respectiv în vizualizatoare de rețete compatibile cu Fabric.

```text
Cosmic Receiver:      Ultimate Nullifier:    Redemption Sigil:
   I                    S D S                 B E B
 A C A                  E N E                 G N G
   I                    S D S                 B D E
I=iron, A=amethyst      S=cosmic shard         B=book, E=emerald
C=compass               D=diamond, E=echo      G=gold, N=nether star
                        N=nether star         D=diamond block

Phase Conductor:      Dimensional Core:
 A P A                  O E O
 P N P                  E D E
 A P A                  O E O
A=amethyst, P=pearl     O=obsidian, E=ender eye
N=nether star          D=diamond block
```

## Configurare pentru modpack

Fișierul **config/galactus.json** se generează la prima pornire. Configurația serverului controlează mecanicile; clientul nu trebuie configurat separat.

| Setare | Implicit | Efect |
|---|---:|---|
| automaticInvasion | true | Avertisment natural. Receiver poate porni evenimentul și cu această opțiune oprită. |
| heraldAfterDays | 2 | Zile active până la avertisment. |
| preparationDays | 3 | Zile active pentru pregătire. |
| arrivalSeconds | 45 | Secunde de sosire. |
| hungerSeconds | 1200 | Secunde până la ocupație. |
| terrainDestruction | true | Consum și corupție de teren. Respectă și gamerule mobGriefing. |
| maxConsumedBlocksPerPulse | 24 | Bugetul de blocuri la o pulsare la fiecare 6 secunde, în chunkuri încărcate. |
| maxInvasionRadius | 192 | Raza corupției. În ocupație centrul pulsurilor poate fi lângă jucătorii din Overworld. |
| raidIntervalSeconds | 45 | Intervalul raidurilor. |
| maxRaidMobs | 24 | Numărul maxim de creaturi ale raidului încărcate. |
| portalChargeSeconds | 90 | Timpul de apărare al portalului. |

Consumarea terenului înlocuiește blocuri de suprafață cu crying obsidian sau le îndepărtează. Blocurile cu inventar/block entity, bedrock, obsidian, cadrul și zona apropiată portalului sunt exceptate. Testează întâi într-o copie dacă adaugi modul într-o lume importantă; deteriorarea terenului este permanentă. Pentru un modpack cu baze protejate poți seta `terrainDestruction: false`.

## Comenzi

Orice jucător: `/galactus status`, `/galactus journal`.

Operator / cheats: `/galactus start`, `/galactus arrive`, `/galactus invade`, `/galactus kit`, `/galactus reloadconfig`, `/galactus reset`. Reset oprește evenimentul și elimină entitățile cosmice încărcate; nu repară terenul.

## Compatibilitate și limitări

Verificat cu Fabric API, client și server dedicate locale. Interacțiunile survival pentru Nullifier și portal au fost probate live; alianța, pragul de sănătate și toate cele trei finaluri au fost probate în joc cu scenarii accelerate. Nu este încă testat într-un modpack mare sau pe un server cu mulți jucători. Modelele folosesc geometrie Minecraft articulată, texturi pictate pe fiecare suprafață și măști luminoase. Titlurile cinematice, avertismentul și dialogul Silver Surfer sunt localizate în română și engleză. Jurnalul și unele mesaje de luptă sunt în engleză.

În versiunea **1.1.0**, Silver Surfer se apropie pe placă, plutește în apropierea jucătorului și își ține poziția când te apropii pentru interacțiune. Placa are vârfuri îngustate, margini luminoase și urmă cosmică. Galactus are armură stratificată, mănuși și genunchiere separate, extensii de cască înclinate, trăsături ale feței și ochi luminoși. Sosirea deschide o falie circulară în cer. Evenimentele au două sunete cosmice originale.

Nu scoate modul dintr-o lume în care vrei să păstrezi obiectele/blocurile lui. Pentru depanare, păstrează `logs/latest.log` și specifică versiunile Minecraft, Fabric Loader și Fabric API.

## Surse, credite și licență

Creat cu **Codex (GPT-6)** folosind workflow-ul **universal-modder**. Cod, modele, texturi și cele două semnale sonore originale. Celelalte sunete sunt evenimente vanilla referite prin API. Texturile metalice folosesc reflexe pictate. Geometria și atlasele se regenerează cu `generate_character_art.py` (Python + Pillow); sunetele cu `generate_audio.py` (Python + FFmpeg). Sursele și asseturile generate sunt incluse în proiectul editabil.

Instrumente: [Fabric](https://fabricmc.net/), [Fabric API](https://github.com/FabricMC/fabric), [universal-modder](https://github.com/rehan-remade/universal-modder). Galactus și Silver Surfer aparțin Marvel; acesta este un proiect fan neoficial, fără afiliere. Licența MIT se aplică implementării originale și nu acordă drepturi asupra personajelor Marvel.

Pentru recompilare: Gradle wrapper inclus, JDK 21; rulează `gradlew.bat build`. Scriptul Windows `bootstrap.ps1` pregătește un JDK izolat, iar `build.ps1 build` folosește cache-ul local al proiectului. Fișierele de test sunt surse separate de pachetul instalabil.
