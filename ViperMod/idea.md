# Technisch Ontwerp: Direct Resource Simulator (HNN Addon)

## 1. Concept & Doelstelling
De **Direct Resource Simulator** (`vipermod:resource_simulator`) combineert de *Simulation Chamber* en de *Loot Fabricator*
uit **Hostile Neural Networks** (HNN 5.3.3, Placebo 8.6.3) in één blok. Er worden geen tussentijdse mob predictions
gemaakt: een gelukte simulatie levert direct de gekozen fabricator-drop op.

Gebouwd voor Monifactory 0.13.8 (packmode Normal): data models worden daar gecraft op max tier (100% accuracy)
en `Sim Chamber Upgrades Model = 0`. De machine volgt gewoon de HNN-config, dus in andere packs werkt ze ook.

ViperMod moet voor deze feature op **client én server** staan. Het blok wordt enkel geregistreerd als HNN geladen is.

---

## 2. Machine Backend & Tick Logica
Werkt zoals de sim chamber, met de loot fabricator er rechtstreeks achter.

* **Inputs:** FE, 1x data model, prediction matrices (of de input die het model vraagt).
* **Duur:** 300 ticks per cyclus, zoals de sim chamber.
* **Energie:** `sim_cost + Loot Fab Power Cost` FE/t (bv. blaze: 1024 + 256). Evenveel als beide machines die parallel draaien.
  Buffer = `Sim Chamber Power Cap`.
* **Start:** zelfde controles als HNN (input past bij model, plaats in output, genoeg energie). Matrix wordt verbruikt,
  de prediction-uitkomst wordt bij de start bepaald (`accuracy`).
* **Einde:**
  * Altijd: 1x generalized prediction (`base_drop`) in het generalized-slot.
  * Bij gelukte prediction: de gekozen `fabricator_drops`-stack (met aantal, bv. 10 blaze rods) in het loot-slot.
  * Past de loot niet (bv. keuze gewijzigd tijdens de cyclus), dan wacht de machine zonder energie te verbruiken.
  * Model-data en iterations worden bijgewerkt zoals in HNN.
* **Faulty model, verkeerde input, volle output, geen energie:** zelfde foutmeldingen als HNN (`FailureState`).
* **Model zonder fabricator drops:** draait, maar levert enkel generalized predictions.

---

## 3. GUI Layout (hergebruik `hostilenetworks:textures/gui/sim_chamber.png`)
Exact dezelfde texture als de sim chamber. Het model-slot steekt links buiten het paneel uit (18x18 sprite op 0,141);
dezelfde sprite wordt een tweede keer getekend, 20px lager, voor het ghost slot.

```
┌──┐┌───────────────────────────────────────┐
│DM││ Target: Blaze               [mat][gen] │
└──┘│ Tier: Self Aware                [loot] │
┌──┐│ Accuracy: 100%                         │
│GH││▌        terminal-tekst            ▐   │
└──┘└───────────────────────────────────────┘
```

| Slot | Positie (t.o.v. GUI) | Rol |
|---|---|---|
| 0 | -13, 1 | Data model (input) |
| ghost | -13, 21 | Gekozen drop (geen echt slot) |
| 1 | 176, 7 | Prediction matrix (input) |
| 2 | 196, 7 | Generalized prediction (output) |
| 3 | 186, 27 | Loot (output, vroeger de mob prediction) |

* Energiebalk rechts en databalk links zoals in HNN; tooltip toont de gecombineerde kost.
* Terminal-tekst en foutmeldingen overgenomen uit HNN (HNN lang-keys).

---

## 4. Ghost Slot "Cycle" Systeem
* Toont de gekozen drop van het huidige model (standaard de eerste), half doorzichtig, met aantal.
* **Linksklik** = volgende drop, **rechtsklik** = vorige.
* Gaat via de vanilla menu-knop (`clickMenuButton`), geen eigen netwerkpakket nodig.
* De keuze wordt **per model** onthouden op de block entity (zoals de loot fabricator), opgeslagen in NBT.
* De keuze van het huidige model gaat via een data slot naar de client.

---

## 5. Automatisering
* **Alle zijden:** stroom in, data model en matrices in, beide outputs eruit (zoals HNN).
* **Onderkant:** de machine pusht actief **enkel de loot** (slot 3) naar de inventory onder het blok.
  Generalized predictions worden nooit gepusht. Lukt de push niet, dan blijft de loot in het slot.

---

## 6. Overige
* **Recept:** shapeless, sim chamber + loot fabricator (enkel als HNN geladen is).
* **Blok:** idealiter een nieuw model: bovenste helft uit het sim chamber-model, onderste helft uit het
  loot fabricator-model (zelfde HNN-textures). Fallback: het sim chamber-model.
* **Drop:** het blok zelf; inventory valt eruit bij afbreken.
* **EMI:** eigen categorie "Direct Resource Simulator": data model + input → fabricator drop + generalized prediction.

---

## 7. Fases
1. **Backend:** registratie (blok, item, block entity, menu), tick-logica, push naar onder, recept, loot table.
2. **Cycle-systeem:** keuze per model, menu-knop, data slot.
3. **GUI + blokmodel:** screen op de sim chamber-texture met ghost slot; gesplitst blokmodel.
4. **EMI-categorie.**
