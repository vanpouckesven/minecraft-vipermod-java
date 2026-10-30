package be.viper.vipermod;

import be.viper.vipermod.integration.hnn.HnnClient;
import be.viper.vipermod.integration.hnn.HnnContent;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(ViperMod.MOD_ID)
public class ViperMod {
    public static final String MOD_ID = "vipermod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ViperMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ViperConfig.CLIENT_SPEC);
        // HNN-klassen pas laden als de mod er is
        if (ModList.get().isLoaded("hostilenetworks")) {
            HnnContent.register(modBus);
            if (FMLEnvironment.dist == Dist.CLIENT) HnnClient.register(modBus);
        }
        LOGGER.info("ViperMod geladen");
    }
}
