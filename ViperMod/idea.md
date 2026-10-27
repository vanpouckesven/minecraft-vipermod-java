# Technisch Ontwerp: Direct Resource Simulator (HNN Addon)

## 1. Concept & Doelstelling
De **Direct Resource Simulator** combineert de functionaliteiten van de *Simulation Chamber* en de *Loot Fabricator* uit de mod **Hostile Neural Networks (HNN)** in één efficiënt single-block apparaat. 

Het doel is het elimineren van tussentijdse *Mob Prediction items*, complexe Side-IO restricties en GUI-spaghetti, om zo de ultieme, TPS-vriendelijke end-game automatisering te faciliteren (perfect aansluitend op drawers en LaserIO).

---

## 2. Machine Backend & Tick Logica
De machine werkt lineair en verwerkt simulaties direct tot eindproducten. 

*   **Inputs:** Energie (FE), 1x HNN Data Model, 1x Prediction Matrix.
*   **Verwerking:** De machine hanteert de standaard simulatietijd van HNN (bijv. 300 ticks). 
*   **In-Memory Processing:** Er worden *geen* fysieke Mob Prediction items gegenereerd. Bij een succesvolle tick-cyclus spreekt de machine direct de drop-table van het geplaatste Data Model aan.
*   **Auto-Output (Forced):** Aan het einde van de cyclus pusht de machine de geproduceerde loot automatisch via een `Capability` check naar de inventory direct *onder* het blok (bijv. een Functional Storage Drawer).

---

## 3. GUI Layout & Slot Architectuur (KISS)
De interface wordt teruggebracht naar een compact, lineair fabrieksscherm (Inputs links -> Voortgang -> Outputs rechts). Het bevat in totaal slechts 5 functionele slots:

1.  **Slot 0 (Input):** HNN Data Model.
2.  **Slot 1 (Input):** Prediction Matrices (Polymer Clay).
3.  **Slot 2 (Ghost Slot):** Target Output Selector (zie sectie 4).
4.  **Slot 3 (Output):** Het daadwerkelijke geproduceerde materiaal (bijv. Iron Ingots).
5.  **Slot 4 (Output):** Generalized Predictions (standaard HNN bijproduct).

---

## 4. Het Ghost Slot "Cycle" Systeem
In plaats van het gigantische selectiescherm uit de Loot Fabricator, introduceert deze machine een klikbare **Ghost Slot**:
*   Wanneer een Data Model in Slot 0 wordt geplaatst, leest de code de beschikbare drops uit de HNN-registry voor dat specifieke model.
*   De Ghost Slot toont visueel de eerste beschikbare drop (zonder dat het item fysiek aanwezig is).
*   **Interactie:** Als de speler op de Ghost Slot klikt, *cycled* (bladert) de interface naar de volgende beschikbare drop in de lijst.
*   De geselecteerde drop wordt op de server opgeslagen (`ResourceLocation chosenDrop`) en bepaalt wat er in Slot 3 spawnt aan het einde van een cyclus.

---

## 5. Automatiseringsprofiel (Side IO)
De machine stelt de volgende capabilities open voor externe mods (zoals LaserIO of GTCEU):
*   **Zijkanten (Direction.NORTH/SOUTH/EAST/WEST):** Accepteert stroom en staat *invoer* van Prediction Matrices (Slot 1) toe, evenals *uitvoer* van de Generalized Predictions (Slot 4) indien nodig.
*   **Bovenkant (Direction.UP):** Vrij te gebruiken (geen strikte blokkade meer nodig).
*   **Onderkant (Direction.DOWN):** Pusht actief de hoofd-loot (Slot 3) naar beneden.

---

## 6. Prompting Stappenplan voor Claude Code
Wanneer je dit met Claude Code gaat bouwen, hanteer dan de volgende volgorde:

1.  **Fase 1 (De Backend):** Voed Claude de `SimulationChamberBlockEntity.java` en de HNN Data Model registers. Laat Claude de gecombineerde tick-logica schrijven die stroom verbruikt, matrices sloopt en direct loot genereert op basis van een hardcoded test-drop.
2.  **Fase 2 (Het Cycle Systeem):** Laat Claude de code schrijven om de drops dynamisch uit het Data Model te trekken en de `chosenDrop` variabele te beheren via een simpel C2S netwerkpakketje.
3.  **Fase 3 (De GUI/Screen):** Voed Claude een gecropte of nieuwe `.png` texture en laat Claude de slots en de Ghost Slot renderen met de juiste pixel-offsets.
