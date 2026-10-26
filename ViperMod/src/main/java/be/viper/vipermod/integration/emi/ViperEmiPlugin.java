package be.viper.vipermod.integration.emi;

import be.viper.vipermod.integration.laserio.LaserIOEmi;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.minecraftforge.fml.ModList;

@EmiEntrypoint
public class ViperEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        // LaserIO-klassen pas laden als de mod er is
        if (ModList.get().isLoaded("laserio")) {
            LaserIOEmi.register(registry);
        }
    }
}
