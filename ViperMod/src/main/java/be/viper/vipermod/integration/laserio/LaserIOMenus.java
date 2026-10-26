package be.viper.vipermod.integration.laserio;

import com.direwolf20.laserio.common.containers.CardFluidContainer;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * LaserIO-menu's met filterslots. Opgezocht via registry-naam i.p.v. LaserIO's Registration-velden,
 * want die heten anders in de originele LaserIO en in de UEL-fork (Monifactory).
 */
final class LaserIOMenus {
    record Menu<C extends AbstractContainerMenu>(Class<C> containerClass, MenuType<C> type) {}

    private LaserIOMenus() {}

    static List<Menu<?>> all() {
        List<Menu<?>> menus = new ArrayList<>();
        add(menus, FilterBasicContainer.class, "filterbasic");
        add(menus, FilterCountContainer.class, "filtercount");
        add(menus, CardItemContainer.class, "carditem");
        add(menus, CardFluidContainer.class, "cardfluid");
        return menus;
    }

    @SuppressWarnings("unchecked")
    private static <C extends AbstractContainerMenu> void add(List<Menu<?>> menus, Class<C> containerClass, String name) {
        MenuType<?> type = ForgeRegistries.MENU_TYPES.getValue(new ResourceLocation("laserio", name));
        if (type != null) {
            menus.add(new Menu<>(containerClass, (MenuType<C>) type));
        }
    }
}
