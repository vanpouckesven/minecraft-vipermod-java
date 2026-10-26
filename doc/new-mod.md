# Nieuwe mod toevoegen

1. Zet de mod als map in de root (fork/clone of nieuw project).
2. De mod moet een uitvoerbare `gradlew` hebben (`chmod +x gradlew`).
3. Controleer met `make list`.
4. Build met `make build MOD=<map>`.

## Afwijkingen: `.modbuild`
Optioneel, in de map van de mod. Make-syntax:
```make
TASK=build     # andere standaard Gradle-taak
```
Een waarde op de command line wint altijd van `.modbuild`.

Java: het image bevat JDK 17 en 21. De toolchain in `build.gradle` bepaalt welke gebruikt wordt.
