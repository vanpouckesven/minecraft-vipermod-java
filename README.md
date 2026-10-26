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

### EMI/JEI "+" voor LaserIO-filters
Klik in EMI (of JEI) op `+` bij een recept terwijl een LaserIO **basic** of **counting filter** open staat
(los, of in een item- of fluid card). De inputs van het recept komen in de filter.

- Gelijke items worden samengeteld; bij de counting filter wordt dat de count.
- Counting filter: **klik** = receptaantal × factor (buffer, standaard ×4), **shift+klik** = exact receptaantal.
  Factor in `config/vipermod-client.toml` (`[laserio] filterMultiplier`, 1–64). Afgetopt op 4096 items / 4.096.000 mB per slot.
- Alle 15 slots worden overschreven (niet-gebruikte slots worden leeg).
- Meer dan 15 verschillende inputs, of geen filter in de card: de `+` is rood met uitleg.
- In een **fluid card** komen de fluid-inputs in de filter, als gevulde bucket of (zonder bucket) GT fluid cell. In een counting filter wordt de hoeveelheid in mB gezet.
- In een item card of de losse filter-GUI komen enkel de item-inputs.
- Werkt via LaserIO's eigen `PacketGhostSlot`, dus ViperMod hoeft enkel client-side geïnstalleerd te zijn voor deze feature.
- Monifactory gebruikt **LaserIO UEL** (fork, modId `laserio`, versie 1.5.x) en **EMI** zonder JEI. ViperMod compileert daartegen; de JEI-variant werkt enkel in packs met JEI.
- Code: `integration/laserio/` (`LaserIOEmi`, `LaserIOJei` delen `FilterTarget` en `RecipeInputs`).

### EMI/JEI "+" naar ME-interfaces: hoeveelheid instelbaar
esotericae2 laat `+` een recept in de config-slots van ME-interfaces zetten (AE2 Interface, ExtendedAE (Oversize) Interface,
Precise/Threshold Export Bus, Precise Storage Bus), altijd met exact het receptaantal. ViperMod past die hoeveelheid aan via een client-mixin.

- Instelling in `config/vipermod-client.toml`: `[ae2] transferAmount` = `RECIPE` | `HALF_STACK` | `FULL_STACK` (standaard `FULL_STACK`).
- Stack = max stackgrootte van het item (64, 16, 1, …); voor fluids AE2's slotcapaciteit (standaard 4000 mB).
- Nooit minder dan het receptaantal. De server klemt daarna nog af op wat het slot toelaat.
- Werkt voor EMI en JEI tegelijk (beide gebruiken `FakeSlotTransferHelper`).
- Compatibel met esotericae2 1.0.x (Monifactory 0.13.8) en 1.1.x (`extra_ae2_recipe_transfers`): `ViperMixinPlugin` kijkt in de bytecode
  welke methodes `GenericStack.wrapInItemStack` aanroepen en past alleen de passende mixin toe. Zonder esotericae2 gebeurt er niets.
- Code: `mixin/` en `integration/ae2/`.
