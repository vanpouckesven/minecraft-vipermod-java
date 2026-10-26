package be.viper.vipermod.integration.laserio;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nullable;

import java.util.Optional;

/** JEI "+"-knop voor LaserIO-filters, voor packs met JEI i.p.v. EMI. */
public final class LaserIOJei<C extends AbstractContainerMenu> implements IUniversalRecipeTransferHandler<C> {
    private final LaserIOMenus.Menu<C> menu;
    private final IRecipeTransferHandlerHelper helper;

    private LaserIOJei(LaserIOMenus.Menu<C> menu, IRecipeTransferHandlerHelper helper) {
        this.menu = menu;
        this.helper = helper;
    }

    public static void register(IRecipeTransferRegistration registration) {
        // JEI zoekt op exacte menuklasse, dus elk menu (ook CardFluidContainer) apart
        for (LaserIOMenus.Menu<?> menu : LaserIOMenus.all()) {
            registration.addUniversalRecipeTransferHandler(new LaserIOJei<>(menu, registration.getTransferHelper()));
        }
    }

    @Override
    public Class<? extends C> getContainerClass() {
        return menu.containerClass();
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        return Optional.of(menu.type());
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(C container, Object recipe, IRecipeSlotsView recipeSlots,
                                               Player player, boolean maxTransfer, boolean doTransfer) {
        // Getoonde ingredient per slot (bij tags)
        RecipeInputs inputs = new RecipeInputs();
        for (IRecipeSlotView slot : recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)) {
            slot.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresent(inputs::addItem);
            slot.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(inputs::addFluid);
        }

        FilterTarget target = FilterTarget.of(container);
        RecipeInputs.Result result = inputs.resolve(target);
        if (result.error() != null) {
            return helper.createUserErrorWithTooltip(result.error());
        }
        if (doTransfer) {
            // JEI: maxTransfer = shift+klik
            target.apply(result.entries(), maxTransfer ? 1 : FilterTarget.multiplier());
        }
        return null;
    }
}
