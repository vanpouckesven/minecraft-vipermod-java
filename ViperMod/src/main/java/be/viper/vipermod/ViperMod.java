package be.viper.vipermod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(ViperMod.MOD_ID)
public class ViperMod {
    public static final String MOD_ID = "vipermod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ViperMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ViperConfig.CLIENT_SPEC);
        LOGGER.info("ViperMod geladen");
    }
}
