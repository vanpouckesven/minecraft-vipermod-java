# ViperMod

QOL-mod voor Monifactory (Minecraft 1.20.1, Forge 47). Zie de README in de root voor features.

- Dependencies (optioneel): EMI 1.1.x, JEI 15.x, LaserIO UEL 1.5.0 (of originele LaserIO) — versies in `gradle.properties`.
- Integraties staan onder `integration/<mod>/` en worden enkel geladen als die mod aanwezig is.
- `make gradle MOD=ViperMod TASK=runClient` start een dev-client met EMI + LaserIO UEL
  (vraagt een display; in Docker op WSL niet getest).
