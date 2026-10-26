package be.viper.vipermod.integration.laserio;

import com.direwolf20.laserio.common.containers.customhandler.FilterCountHandler;
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
 */
record FilterTarget(AbstractContainerMenu menu, int firstSlot, @Nullable FilterCountHandler countHandler) {
    static final int SLOTS = 15;

    /** Overschrijft alle filterslots; slots voorbij de lijst worden leeggemaakt. */
    void apply(List<ItemStack> stacks) {
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = i < stacks.size() ? stacks.get(i).copy() : ItemStack.EMPTY;
            if (countHandler == null && !stack.isEmpty()) {
                stack.setCount(1);
            }
            int slotIndex = firstSlot + i;

            // Client-side meteen tonen, zoals LaserIO's eigen JEI ghost handlers
            menu.slots.get(slotIndex).set(stack.copy());
            if (countHandler != null) {
                countHandler.setStackInSlotSave(i, stack.copy());
            }
            PacketHandler.sendToServer(new PacketGhostSlot(slotIndex, stack, stack.getCount()));
        }
    }
}
