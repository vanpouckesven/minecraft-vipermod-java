# Bouwen

Vereist: Docker met Compose op WSL. Verder niets.

## Commando's
| Commando | Wat |
|---|---|
| `make list` | toon alle mods |
| `make build MOD=GregTech-Modern` | build, jars naar `dist/GregTech-Modern/` |
| `make build-all` | build alle mods |
| `make gradle MOD=X TASK=spotlessApply` | willekeurige Gradle-taak |
| `make shell MOD=X` | bash in de container, in de map van de mod |
| `make clean MOD=X` | `gradle clean` + `dist/X` leegmaken |
| `make image` | build-image (opnieuw) bouwen |

## Output
- Naar `dist/<Mod>/` gaan alle jars uit `build/libs`, behalve `-dev`, `-sources` en `-javadoc`.
- Voor GregTech-Modern is `gtceu-1.20.1-<versie>.jar` de jar om te spelen.

## Resources
- Container-limiet: 8 GB RAM (GregTech gebruikt `-Xmx6G`).
- Zorg dat WSL genoeg geheugen heeft (`.wslconfig`).

## Problemen
- **Cache corrupt:** `rm -rf .cache/gradle`
- **Image na UID-wijziging:** `make image`
