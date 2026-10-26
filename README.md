# Minecraft Mods

Deze folder bestaat uit verschillende mods die kunnen aangepast worden en gebouwd worden om in minecraft te spelen. 
De mods zijn speciaal ontworpen voor Monifactory van Omicron Industries. Momenteel is versie 0.13.8 beschikbaar. 

De mods worden gebouwd met behulp van java. Om onze WSL omgeving zo veilig mogelijk te houden werken we enkel met docker containers voor het bouwen van onze modpacks. 

## Bouwen

```bash
make list                        # alle mods
make build MOD=GregTech-Modern   # jars naar dist/GregTech-Modern/
```

Meer info in [doc/](doc/):
- [architecture.md](doc/architecture.md): opzet en keuzes
- [building.md](doc/building.md): alle build-commando's
- [new-mod.md](doc/new-mod.md): nieuwe mod toevoegen

# GregTech-Modern

Eerste clone van de officiele gtceu (Gregtech Modern). 
We willen een aantal aanpassingen doorvoeren aan deze modpack waardoor we hem geforked hebben. 

Je mag hier aanpassingen in doen en je mag hier committen aangezien deze repo naar mijn eigen github gaat. 

## Wijzigingen

### Standaard output van machines
Geldt voor elektrische single-block machines (`SimpleTieredMachine`: macerator, furnace, centrifuge, …).
Alleen voor nieuw geplaatste machines; bestaande machines houden hun instellingen.

| Instelling | Origineel | Nu |
|---|---|---|
| Output-zijde items | achterkant | onderkant |
| Output-zijde fluids | achterkant | onderkant |
| Auto output items/fluids | uit | aan |
| Input via output-zijde (items/fluids) | uit | aan |

- Wijst de voorkant naar beneden, dan blijft de output aan de achterkant.
- Code: `api/machine/SimpleTieredMachine.java` (constructor).

# ViperMod

Eigen mod (`be.viper.vipermod`) met kleine QOL-features voor Monifactory die nergens anders te vinden zijn.
Build: `make build MOD=ViperMod` → `dist/ViperMod/vipermod-1.20.1-<versie>.jar`.

## Features

### JEI "+" voor LaserIO-filters
Klik in JEI op `+` bij een recept terwijl een LaserIO **basic** of **counting filter** open staat
(los, of in een item card). De item-inputs van het recept komen in de filter.

- Gelijke items worden samengeteld; bij de counting filter wordt dat de count.
- Alle 15 slots worden overschreven (niet-gebruikte slots worden leeg).
- Meer dan 15 verschillende items, of geen filter in de card: de `+` is rood met uitleg.
- Fluids worden genegeerd.
- Werkt via LaserIO's eigen `PacketGhostSlot`, dus ViperMod hoeft enkel client-side geïnstalleerd te zijn voor deze feature.
- Code: `integration/laserio/`.
