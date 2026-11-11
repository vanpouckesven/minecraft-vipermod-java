package be.viper.vipermod;

import be.viper.vipermod.integration.ae2.TransferAmount;
import net.minecraftforge.common.ForgeConfigSpec;

/** Client-config: config/vipermod-client.toml */
public final class ViperConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec.IntValue LASERIO_FILTER_MULTIPLIER;
    public static final ForgeConfigSpec.EnumValue<TransferAmount> AE2_TRANSFER_AMOUNT;
    public static final ForgeConfigSpec.IntValue GTCEU_ME_ITEM_AMOUNT;
    public static final ForgeConfigSpec.IntValue GTCEU_ME_FLUID_BUCKETS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("laserio");
        LASERIO_FILTER_MULTIPLIER = builder
                .comment("Factor op de receptaantallen bij '+' in een counting filter (buffer).",
                        "Shift+klik zet altijd het exacte aantal (x1).")
                .defineInRange("filterMultiplier", 4, 1, 64);
        builder.pop();
        builder.push("ae2");
        AE2_TRANSFER_AMOUNT = builder
                .comment("Hoeveelheid die '+' (esotericae2) in ME-interfaces en precise/threshold bussen zet.",
                        "RECIPE = exact receptaantal, HALF_STACK / FULL_STACK = halve / volle stack (nooit minder dan het recept).",
                        "Stack = max stackgrootte van het item; voor fluids de AE2-slotcapaciteit (standaard 4 buckets).")
                .defineEnum("transferAmount", TransferAmount.FULL_STACK);
        builder.pop();
        builder.push("gtceu");
        GTCEU_ME_ITEM_AMOUNT = builder
                .comment("Minimum aantal items dat '+' in een GTCEu ME Input Bus zet (meer als het recept meer vraagt).")
                .defineInRange("meItemAmount", 1024, 1, Integer.MAX_VALUE);
        GTCEU_ME_FLUID_BUCKETS = builder
                .comment("Minimum aantal buckets dat '+' in een GTCEu ME Input Hatch zet (meer als het recept meer vraagt).")
                .defineInRange("meFluidBuckets", 1000, 1, 2_000_000);
        builder.pop();
        CLIENT_SPEC = builder.build();
    }

    private ViperConfig() {}
}
