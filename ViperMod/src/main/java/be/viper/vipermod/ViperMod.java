package be.viper.vipermod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ViperMod.MOD_ID)
public class ViperMod {
    public static final String MOD_ID = "vipermod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ViperMod() {
        LOGGER.info("ViperMod geladen");
    }
}
