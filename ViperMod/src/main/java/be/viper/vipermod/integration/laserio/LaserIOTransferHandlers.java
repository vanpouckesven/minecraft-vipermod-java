package be.viper.vipermod.integration.laserio;

import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.setup.Registration;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.world.item.ItemStack;

/**
 * JEI "+"-knop voor LaserIO-filters: zet de inputs van een recept in de basic- of counting filter.
 * Werkt zowel in de losse filter-GUI als in een item card met filter erin.
 */
public final class LaserIOTransferHandlers {
    private LaserIOTransferHandlers() {}

    public static void register(IRecipeTransferRegistration registration) {
        IRecipeTransferHandlerHelper helper = registration.getTransferHelper();

        registration.addUniversalRecipeTransferHandler(new FilterTransferHandler<>(
                FilterBasicContainer.class, Registration.FilterBasic_Container.get(), helper,
                menu -> new FilterTarget(menu, 0, null)));

        registration.addUniversalRecipeTransferHandler(new FilterTransferHandler<>(
                FilterCountContainer.class, Registration.FilterCount_Container.get(), helper,
                menu -> new FilterTarget(menu, 0, menu.handler)));

        registration.addUniversalRecipeTransferHandler(new FilterTransferHandler<>(
                CardItemContainer.class, Registration.CardItem_Container.get(), helper,
                LaserIOTransferHandlers::cardTarget));
    }

    private static FilterTarget cardTarget(CardItemContainer menu) {
        ItemStack filter = menu.slots.get(0).getItem();
        if (filter.getItem() instanceof FilterCount && menu.filterHandler instanceof FilterCountHandler countHandler) {
            return new FilterTarget(menu, CardItemContainer.SLOTS, countHandler);
        }
        if (filter.getItem() instanceof FilterBasic) {
            return new FilterTarget(menu, CardItemContainer.SLOTS, null);
        }
        return null;
    }
}
