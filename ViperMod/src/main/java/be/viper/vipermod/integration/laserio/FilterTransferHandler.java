package be.viper.vipermod.integration.laserio;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Geldt voor elk recepttype: verzamelt de item- of fluid-inputs en zet ze in de filter. */
class FilterTransferHandler<C extends AbstractContainerMenu> implements IUniversalRecipeTransferHandler<C> {
    private final Class<C> containerClass;
    private final MenuType<C> menuType;
    private final IRecipeTransferHandlerHelper helper;
    private final Function<C, FilterTarget> targetFactory;

    FilterTransferHandler(Class<C> containerClass, MenuType<C> menuType, IRecipeTransferHandlerHelper helper,
                          Function<C, FilterTarget> targetFactory) {
        this.containerClass = containerClass;
        this.menuType = menuType;
        this.helper = helper;
        this.targetFactory = targetFactory;
    }

    @Override
    public Class<? extends C> getContainerClass() {
        return containerClass;
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        return Optional.of(menuType);
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(C container, Object recipe, IRecipeSlotsView recipeSlots,
                                         Player player, boolean maxTransfer, boolean doTransfer) {
        FilterTarget target = targetFactory.apply(container);
        if (target == null) {
            return helper.createUserErrorWithTooltip(Component.translatable("vipermod.laserio.no_filter"));
        }

        List<FilterTarget.Entry> entries = new ArrayList<>();
        if (target.fluids()) {
            for (FluidStack fluid : collectFluids(recipeSlots)) {
                ItemStack filled = FluidContainers.filled(fluid);
                if (filled.isEmpty()) {
                    return helper.createUserErrorWithTooltip(
                            Component.translatable("vipermod.laserio.no_container", fluid.getDisplayName()));
                }
                entries.add(new FilterTarget.Entry(filled, fluid.getAmount()));
            }
        } else {
            for (ItemStack stack : collectItems(recipeSlots)) {
                entries.add(new FilterTarget.Entry(stack, -1));
            }
        }

        if (entries.isEmpty()) {
            return helper.createUserErrorWithTooltip(Component.translatable(
                    target.fluids() ? "vipermod.laserio.no_fluids" : "vipermod.laserio.no_items"));
        }
        if (entries.size() > FilterTarget.SLOTS) {
            return helper.createUserErrorWithTooltip(
                    Component.translatable("vipermod.laserio.too_many", entries.size(), FilterTarget.SLOTS));
        }

        if (doTransfer) {
            target.apply(entries);
        }
        return null;
    }

    /** Getoonde item per input-slot; gelijke items worden samengeteld. */
    private static List<ItemStack> collectItems(IRecipeSlotsView recipeSlots) {
        List<ItemStack> result = new ArrayList<>();
        for (IRecipeSlotView slot : recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)) {
            slot.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresent(stack -> mergeItem(result, stack));
        }
        return result;
    }

    /** Getoonde fluid per input-slot; gelijke fluids worden samengeteld. */
    private static List<FluidStack> collectFluids(IRecipeSlotsView recipeSlots) {
        List<FluidStack> result = new ArrayList<>();
        for (IRecipeSlotView slot : recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)) {
            slot.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fluid -> mergeFluid(result, fluid));
        }
        return result;
    }

    private static void mergeItem(List<ItemStack> result, ItemStack stack) {
        if (stack.isEmpty()) return;
        for (ItemStack existing : result) {
            if (ItemStack.isSameItemSameTags(existing, stack)) {
                existing.grow(stack.getCount());
                return;
            }
        }
        result.add(stack.copy());
    }

    private static void mergeFluid(List<FluidStack> result, FluidStack fluid) {
        if (fluid.isEmpty()) return;
        for (FluidStack existing : result) {
            if (existing.isFluidEqual(fluid)) {
                existing.grow(fluid.getAmount());
                return;
            }
        }
        result.add(fluid.copy());
    }
}
