# Architectuur

## Doel
Losse mods ontwikkelen voor Monifactory (Omicron Industries, versie 0.13.8: Minecraft 1.20.1, Forge).
We maken geen modpack. Gebouwde jars gaan naar een bestaande Monifactory-instance.

## Opbouw
```
minecraft/
├── Dockerfile          # generiek Java build-image
├── docker-compose.yml  # build-service, mount van de repo + Gradle-cache
├── Makefile            # ingang voor alle builds
├── doc/                # deze documentatie
├── <Mod>/              # elke mod een eigen map in de root (bv. GregTech-Modern)
├── dist/<Mod>/         # gebouwde jars (niet in git)
└── .cache/gradle/      # gedeelde Gradle-cache (niet in git)
```

## Keuzes
- **Alles bouwt in Docker.** Op WSL zelf komt geen JDK of Gradle.
- **Eén generiek image** (`mc-modbuild`) met JDK 17 en 21. Gradle-toolchains kiezen zelf de juiste versie.
- **Non-root.** De container draait met jouw UID/GID, dus bestanden in de repo blijven van jou.
- **Gedeelde Gradle-cache in `.cache/gradle/`.** Minecraft, Forge en dependencies worden één keer gedownload en door alle mods hergebruikt.
- **De Makefile is generiek.** Een mod is elke map met een `gradlew`. Voor een nieuwe mod hoeft de Makefile niet te veranderen.
- **Afwijkingen per mod** staan in `<Mod>/.modbuild`, zie [new-mod.md](new-mod.md).
- **Mods zijn eigen git-repo's** (forks naar de eigen GitHub).
