package be.viper.vipermod.integration.hnn;

import static be.viper.vipermod.integration.hnn.ResourceSimulatorBlockEntity.*;

import be.viper.vipermod.integration.hnn.ResourceSimulatorBlockEntity.SimulatorItemHandler;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.hostilenetworks.tile.SimChamberTileEntity.FailureState;
import dev.shadowsoffire.placebo.menu.BlockEntityMenu;
import dev.shadowsoffire.placebo.menu.FilteredSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/** Slotposities van HNN's SimChamberContainer; het loot-slot staat waar de mob prediction stond. */
public class ResourceSimulatorMenu extends BlockEntityMenu<ResourceSimulatorBlockEntity> {
    public static final int BUTTON_NEXT = 0, BUTTON_PREVIOUS = 1;

    public ResourceSimulatorMenu(int id, Inventory playerInv, BlockPos pos) {
        super(HnnContent.RESOURCE_SIMULATOR_MENU.get(), id, playerInv, pos);
        SimulatorItemHandler inventory = this.tile.getInventory();
        this.addSlot(new FilteredSlot(inventory, SLOT_MODEL, -13, 1, s -> s.getItem() instanceof DataModelItem));
        this.addSlot(new FilteredSlot(inventory, SLOT_INPUT, 176, 7, s -> DataModelItem.matchesInput(this.getSlot(SLOT_MODEL).getItem(), s)));
        this.addSlot(new FilteredSlot(inventory, SLOT_BASE, 196, 7, s -> false));
        this.addSlot(new FilteredSlot(inventory, SLOT_LOOT, 186, 27, s -> false));
        this.addPlayerSlots(playerInv, 36, 153);
        this.mover.registerRule((stack, slot) -> slot < 4, 4, this.slots.size());
        this.mover.registerRule((stack, slot) -> stack.getItem() instanceof DataModelItem, 0, 1);
        this.mover.registerRule((stack, slot) -> DataModelItem.matchesInput(this.getSlot(SLOT_MODEL).getItem(), stack), 1, 2);
        this.registerInvShuffleRules();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(this.pos).getBlock() == HnnContent.RESOURCE_SIMULATOR.get();
    }

    /** Ghost slot: linksklik = volgende drop, rechtsklik = vorige. */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_NEXT && id != BUTTON_PREVIOUS) return false;
        this.tile.cycleSelection(id == BUTTON_NEXT ? 1 : -1);
        return true;
    }

    public int getEnergyStored() {
        return this.tile.getEnergyStored();
    }

    public int getRuntime() {
        return this.tile.getRuntime();
    }

    public boolean didPredictionSucceed() {
        return this.tile.didPredictionSucceed();
    }

    public FailureState getFailState() {
        return this.tile.getFailState();
    }

    public int getSelectedDrop() {
        return this.tile.getClientSelection();
    }
}
