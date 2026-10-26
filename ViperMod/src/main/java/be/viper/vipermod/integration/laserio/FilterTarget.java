package be.viper.vipermod.integration.laserio;

import com.direwolf20.laserio.common.containers.CardFluidContainer;
import com.direwolf20.laserio.common.containers.CardItemContainer;
import com.direwolf20.laserio.common.containers.FilterBasicContainer;
import com.direwolf20.laserio.common.containers.FilterCountContainer;
import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
import com.direwolf20.laserio.common.items.filters.FilterBasic;
import com.direwolf20.laserio.common.items.filters.FilterCount;
import com.direwolf20.laserio.common.network.PacketHandler;
import com.direwolf20.laserio.common.network.packets.PacketGhostSlot;
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
    /** Maximum mB per slot, zoals in CardFluidScreen */
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

    /** Overschrijft alle filterslots; slots voorbij de lijst worden leeggemaakt. */
    void apply(List<Entry> entries) {
        for (int i = 0; i < SLOTS; i++) {
            Entry entry = i < entries.size() ? entries.get(i) : Entry.EMPTY;
            ItemStack stack = entry.stack().copy();
            int mbAmount = -1;
            if (countHandler == null) {
                if (!stack.isEmpty()) stack.setCount(1);
            } else if (entry.mbAmount() > 0) {
                mbAmount = Math.min(entry.mbAmount(), MAX_MB);
                stack.setCount(Math.max(1, mbAmount / 1000));
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
