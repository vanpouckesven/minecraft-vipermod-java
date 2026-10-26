package be.viper.vipermod.integration.laserio;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Zet een fluid om naar een item dat LaserIO als fluid-filter herkent:
 * elk item met een fluid-capability die die fluid bevat.
 */
final class FluidContainers {
    /** Fallback voor fluids zonder bucket (bv. GT-gassen), in volgorde van voorkeur */
    private static final List<ResourceLocation> FALLBACK_CONTAINERS = List.of(
            new ResourceLocation("gtceu", "fluid_cell"));

    private FluidContainers() {}

    /** Gevulde container met 1 bucket van de fluid, of EMPTY als er geen past. */
    static ItemStack filled(FluidStack fluid) {
        FluidStack bucket = new FluidStack(fluid, 1000);

        ItemStack filledBucket = FluidUtil.getFilledBucket(bucket);
        if (!filledBucket.isEmpty()) return filledBucket;

        for (ResourceLocation id : FALLBACK_CONTAINERS) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item == null) continue;
            ItemStack container = FluidUtil.getFluidHandler(new ItemStack(item)).map(handler -> {
                if (handler.fill(bucket, IFluidHandler.FluidAction.EXECUTE) <= 0) return ItemStack.EMPTY;
                return handler.getContainer();
            }).orElse(ItemStack.EMPTY);
            if (!container.isEmpty()) return container;
        }
        return ItemStack.EMPTY;
    }
}
