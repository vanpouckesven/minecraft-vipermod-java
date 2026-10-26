package be.viper.vipermod;

import be.viper.vipermod.integration.ae2.TransferAmount;
import net.minecraftforge.common.ForgeConfigSpec;

/** Client-config: config/vipermod-client.toml */
public final class ViperConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec.IntValue LASERIO_FILTER_MULTIPLIER;
    public static final ForgeConfigSpec.EnumValue<TransferAmount> AE2_TRANSFER_AMOUNT;

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
        CLIENT_SPEC = builder.build();
    }

    private ViperConfig() {}
}
