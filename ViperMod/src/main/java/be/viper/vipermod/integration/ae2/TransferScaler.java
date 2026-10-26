package be.viper.vipermod.integration.ae2;

import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import be.viper.vipermod.ViperConfig;

/** Past het receptaantal aan volgens {@link TransferAmount}; aangeroepen vanuit de esotericae2-mixins. */
public final class TransferScaler {
    private TransferScaler() {}

    public static GenericStack scale(GenericStack stack) {
        TransferAmount mode = ViperConfig.AE2_TRANSFER_AMOUNT.get();
        if (mode == TransferAmount.RECIPE) return stack;

        long target = stackSize(stack.what());
        if (mode == TransferAmount.HALF_STACK) target = Math.max(1, target / 2);
        // De server klemt nog af op de slotcapaciteit van de interface/bus
        return target > stack.amount() ? new GenericStack(stack.what(), target) : stack;
    }

    /** Items: max stackgrootte. Fluids e.d.: AE2's slotcapaciteit voor dat type. */
    private static long stackSize(AEKey key) {
        if (key instanceof AEItemKey item) return item.getMaxStackSize();
        Long capacity = GenericSlotCapacities.getMap().get(key.getType());
        return capacity != null ? capacity : key.getAmountPerUnit();
    }
}
