package be.viper.vipermod.integration.jei;

import be.viper.vipermod.ViperMod;
import be.viper.vipermod.integration.laserio.LaserIOTransferHandlers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

@JeiPlugin
public class ViperJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(ViperMod.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // LaserIO-klassen pas laden als de mod er is
        if (ModList.get().isLoaded("laserio")) {
            LaserIOTransferHandlers.register(registration);
        }
    }
}
