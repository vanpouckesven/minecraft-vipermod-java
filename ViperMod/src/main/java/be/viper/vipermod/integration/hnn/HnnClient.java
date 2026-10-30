package be.viper.vipermod.integration.hnn;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class HnnClient {
    public static void register(IEventBus modBus) {
        modBus.addListener((FMLClientSetupEvent e) ->
                e.enqueueWork(() -> MenuScreens.register(HnnContent.RESOURCE_SIMULATOR_MENU.get(), ResourceSimulatorScreen::new)));
    }

    private HnnClient() {}
}
