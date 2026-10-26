package be.viper.vipermod.integration.laserio;

import mezz.jei.api.constants.VanillaTypes;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Geldt voor elk recepttype: verzamelt de item-inputs en zet ze in de filter. */
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

        List<ItemStack> inputs = collectInputs(recipeSlots);
        if (inputs.isEmpty()) {
            return helper.createUserErrorWithTooltip(Component.translatable("vipermod.laserio.no_items"));
        }
        if (inputs.size() > FilterTarget.SLOTS) {
            return helper.createUserErrorWithTooltip(
                    Component.translatable("vipermod.laserio.too_many", inputs.size(), FilterTarget.SLOTS));
        }

        if (doTransfer) {
            target.apply(inputs);
        }
        return null;
    }

    /** Getoonde item per input-slot; gelijke items worden samengeteld. */
    private static List<ItemStack> collectInputs(IRecipeSlotsView recipeSlots) {
        List<ItemStack> result = new ArrayList<>();
        for (IRecipeSlotView slot : recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)) {
            slot.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresent(stack -> merge(result, stack));
        }
        return result;
    }

    private static void merge(List<ItemStack> result, ItemStack stack) {
        if (stack.isEmpty()) return;
        for (ItemStack existing : result) {
            if (ItemStack.isSameItemSameTags(existing, stack)) {
                existing.grow(stack.getCount());
                return;
            }
        }
        result.add(stack.copy());
    }
}
