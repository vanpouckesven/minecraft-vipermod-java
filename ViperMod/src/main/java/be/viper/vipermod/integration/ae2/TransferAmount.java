package be.viper.vipermod.integration.ae2;

/** Hoeveelheid die esotericae2's "+" in de config-slots van ME-interfaces zet. Geen AE2-klassen: ViperConfig laadt dit altijd. */
public enum TransferAmount {
    /** Exact het receptaantal (gedrag van esotericae2). */
    RECIPE,
    /** Halve stack, of meer als het recept meer vraagt. */
    HALF_STACK,
    /** Volle stack, of meer als het recept meer vraagt. */
    FULL_STACK
}
