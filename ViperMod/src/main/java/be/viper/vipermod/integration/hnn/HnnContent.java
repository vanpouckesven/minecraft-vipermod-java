package be.viper.vipermod.integration.hnn;

import be.viper.vipermod.ViperMod;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntityType;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import java.util.Set;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Direct Resource Simulator. Enkel laden als Hostile Neural Networks aanwezig is. */
public final class HnnContent {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ViperMod.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ViperMod.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ViperMod.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ViperMod.MOD_ID);

    // Zelfde eigenschappen als de sim chamber
    public static final RegistryObject<Block> RESOURCE_SIMULATOR = BLOCKS.register("resource_simulator",
            () -> new ResourceSimulatorBlock(Block.Properties.of().lightLevel(s -> 1).strength(4, 3000).noOcclusion()));
    public static final RegistryObject<Item> RESOURCE_SIMULATOR_ITEM = ITEMS.register("resource_simulator",
            () -> new BlockItem(RESOURCE_SIMULATOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<ResourceSimulatorBlockEntity>> RESOURCE_SIMULATOR_BE = BLOCK_ENTITIES.register("resource_simulator",
            () -> new TickingBlockEntityType<>(ResourceSimulatorBlockEntity::new, Set.of(RESOURCE_SIMULATOR.get()), false, true));
    public static final RegistryObject<MenuType<ResourceSimulatorMenu>> RESOURCE_SIMULATOR_MENU = MENUS.register("resource_simulator",
            () -> MenuUtil.posType(ResourceSimulatorMenu::new));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        modBus.addListener(HnnContent::fillTab);
    }

    private static void fillTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == Hostile.Tabs.HNN_TAB_KEY) event.accept(RESOURCE_SIMULATOR_ITEM);
    }

    private HnnContent() {}
}
