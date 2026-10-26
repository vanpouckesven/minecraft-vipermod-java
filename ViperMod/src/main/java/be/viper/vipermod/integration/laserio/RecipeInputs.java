package be.viper.vipermod.integration.laserio;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Inputs van één recept, los van JEI/EMI; gelijke items en fluids worden samengeteld. */
final class RecipeInputs {
    private final List<ItemStack> items = new ArrayList<>();
    private final List<FluidStack> fluids = new ArrayList<>();

    /**
     * @param entries filterslots, leeg bij een fout
     * @param error   reden waarom het niet kan, of null
     */
    record Result(List<FilterTarget.Entry> entries, @Nullable Component error) {
        static Result error(String key, Object... args) {
            return new Result(List.of(), Component.translatable(key, args));
        }
    }

    void addItem(ItemStack stack) {
        if (stack.isEmpty()) return;
        for (ItemStack existing : items) {
            if (ItemStack.isSameItemSameTags(existing, stack)) {
                existing.grow(stack.getCount());
                return;
            }
        }
        items.add(stack.copy());
    }

    void addFluid(FluidStack fluid) {
        if (fluid.isEmpty()) return;
        for (FluidStack existing : fluids) {
            if (existing.isFluidEqual(fluid)) {
                existing.grow(fluid.getAmount());
                return;
            }
        }
        fluids.add(fluid.copy());
    }

    /** Fluid card: de fluids als gevulde container; anders de items. */
    Result resolve(@Nullable FilterTarget target) {
        if (target == null) {
            return Result.error("vipermod.laserio.no_filter");
        }

        List<FilterTarget.Entry> entries = new ArrayList<>();
        if (target.fluids()) {
            for (FluidStack fluid : fluids) {
                ItemStack filled = FluidContainers.filled(fluid);
                if (filled.isEmpty()) {
                    return Result.error("vipermod.laserio.no_container", fluid.getDisplayName());
                }
                entries.add(new FilterTarget.Entry(filled, fluid.getAmount()));
            }
        } else {
            for (ItemStack stack : items) {
                entries.add(new FilterTarget.Entry(stack, -1));
            }
        }

        if (entries.isEmpty()) {
            return Result.error(target.fluids() ? "vipermod.laserio.no_fluids" : "vipermod.laserio.no_items");
        }
        if (entries.size() > FilterTarget.SLOTS) {
            return Result.error("vipermod.laserio.too_many", entries.size(), FilterTarget.SLOTS);
        }
        return new Result(entries, null);
    }
}
