package be.viper.vipermod;

import net.minecraftforge.common.ForgeConfigSpec;

/** Client-config: config/vipermod-client.toml */
public final class ViperConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec.IntValue LASERIO_FILTER_MULTIPLIER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("laserio");
        LASERIO_FILTER_MULTIPLIER = builder
                .comment("Factor op de receptaantallen bij '+' in een counting filter (buffer).",
                        "Shift+klik zet altijd het exacte aantal (x1).")
                .defineInRange("filterMultiplier", 4, 1, 64);
        builder.pop();
        CLIENT_SPEC = builder.build();
    }

    private ViperConfig() {}
}
