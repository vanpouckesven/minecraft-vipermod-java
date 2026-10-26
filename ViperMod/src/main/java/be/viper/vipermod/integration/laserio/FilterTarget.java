package be.viper.vipermod.integration.laserio;

import be.viper.vipermod.ViperConfig;
import com.direwolf20.laserio.common.containers.CardFluidContainer;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketGhostSlot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

import java.util.List;

/**
 * De 15 filterslots in een open LaserIO-menu.
 *
 * @param firstSlot    menu-index van de eerste filterslot
 * @param countHandler gezet bij een counting filter, null bij een basic filter
 * @param fluids       filter zit in een fluid card: fluid-inputs i.p.v. item-inputs
 */
record FilterTarget(AbstractContainerMenu menu, int firstSlot, @Nullable FilterCountHandler countHandler,
                    boolean fluids) {
    static final int SLOTS = 15;
    /** Maximum per slot, zoals in FilterCountScreen en CardFluidScreen */
    static final int MAX_COUNT = 4096;
    static final int MAX_MB = 4_096_000;

    /**
     * Eén filterslot.
     *
     * @param mbAmount hoeveelheid fluid in mB, of -1 voor een gewoon item
     */
    record Entry(ItemStack stack, int mbAmount) {
        static final Entry EMPTY = new Entry(ItemStack.EMPTY, -1);
    }

    /** Filter in het open menu: losse filter-GUI of filter in een item-/fluid card. Null als er geen is. */
    @Nullable
    static FilterTarget of(AbstractContainerMenu menu) {
        if (menu instanceof FilterBasicContainer) {
            return new FilterTarget(menu, 0, null, false);
        }
        if (menu instanceof FilterCountContainer filterMenu) {
            return new FilterTarget(menu, 0, filterMenu.handler, false);
        }
        if (menu instanceof CardItemContainer card) { // ook CardFluidContainer
            boolean fluids = card instanceof CardFluidContainer;
            ItemStack filter = card.slots.get(0).getItem();
            if (filter.getItem() instanceof FilterCount && card.filterHandler instanceof FilterCountHandler countHandler) {
                return new FilterTarget(menu, CardItemContainer.SLOTS, countHandler, fluids);
            }
            if (filter.getItem() instanceof FilterBasic) {
                return new FilterTarget(menu, CardItemContainer.SLOTS, null, fluids);
            }
        }
        return null;
    }

    /** Factor voor counting filters: uit de config, of x1 bij shift+klik. */
    static int multiplier() {
        return Screen.hasShiftDown() ? 1 : ViperConfig.LASERIO_FILTER_MULTIPLIER.get();
    }

    boolean counting() {
        return countHandler != null;
    }

    /**
     * Overschrijft alle filterslots; slots voorbij de lijst worden leeggemaakt.
     * Bij een counting filter worden de aantallen vermenigvuldigd met {@code multiplier}, afgetopt op LaserIO's maximum.
     */
    void apply(List<Entry> entries, int multiplier) {
        for (int i = 0; i < SLOTS; i++) {
            Entry entry = i < entries.size() ? entries.get(i) : Entry.EMPTY;
            ItemStack stack = entry.stack().copy();
            int mbAmount = -1;
            if (countHandler == null) {
                if (!stack.isEmpty()) stack.setCount(1);
            } else if (entry.mbAmount() > 0) {
                mbAmount = (int) Math.min((long) entry.mbAmount() * multiplier, MAX_MB);
                stack.setCount(Math.max(1, mbAmount / 1000));
            } else if (!stack.isEmpty()) {
                stack.setCount((int) Math.min((long) stack.getCount() * multiplier, MAX_COUNT));
            }
            int slotIndex = firstSlot + i;

            // Client-side meteen tonen, zoals LaserIO's eigen JEI ghost handlers
            menu.slots.get(slotIndex).set(stack.copy());
            if (countHandler != null) {
                countHandler.setStackInSlotSave(i, stack.copy());
                if (mbAmount > 0) {
                    FilterCount.setSlotAmount(countHandler.stack, i, mbAmount);
                }
            }
            PacketHandler.sendToServer(new PacketGhostSlot(slotIndex, stack, stack.getCount(), mbAmount));
        }
    }
}
