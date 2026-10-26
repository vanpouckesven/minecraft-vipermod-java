# ViperMod

QOL-mod voor Monifactory (Minecraft 1.20.1, Forge 47). Zie de README in de root voor features.

- Dependencies (optioneel): JEI 15.x, LaserIO 1.6.8 — versies in `gradle.properties`.
- Integraties staan onder `integration/<mod>/` en worden enkel geladen als die mod aanwezig is.
- `make gradle MOD=ViperMod TASK=runClient` start een dev-client met JEI + LaserIO
  (vraagt een display; in Docker op WSL niet getest).
