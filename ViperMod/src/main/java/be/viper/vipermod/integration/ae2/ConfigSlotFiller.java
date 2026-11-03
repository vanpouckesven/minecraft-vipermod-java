package be.viper.vipermod.integration.ae2;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.slot.FakeSlot;
import com.google.common.math.LongMath;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Vervangt esotericae2 1.0.x's transfer: zet de receptinputs in de config-slots zonder andere slots te wissen.
 * Input al aanwezig: dat slot krijgt de nieuwe hoeveelheid. Anders: eerste lege slot. Geen plaats: overgeslagen.
 */
public final class ConfigSlotFiller {
    private ConfigSlotFiller() {}

    public static void fill(UpgradeableMenu<?> menu, List<List<GenericStack>> inputs) {
        // Inputs samenvoegen per key (eerste alternatief bij tags), zoals esotericae2
        Map<AEKey, Long> merged = new LinkedHashMap<>();
        for (List<GenericStack> options : inputs) {
            if (options.isEmpty()) continue;
            GenericStack stack = options.get(0);
            merged.merge(stack.what(), stack.amount(), LongMath::saturatedAdd);
        }

        List<FakeSlot> slots = configSlots(menu);
        // setFilterTo stuurt enkel een packet; het client-slot blijft leeg tot de server synct.
        // Daarom zelf bijhouden welke slots al gebruikt zijn, anders landt alles in hetzelfde lege slot.
        Set<FakeSlot> claimed = new HashSet<>();
        for (Map.Entry<AEKey, Long> entry : merged.entrySet()) {
            GenericStack stack = TransferScaler.scale(new GenericStack(entry.getKey(), entry.getValue()));
            FakeSlot target = findSlot(slots, claimed, stack.what());
            if (target == null) continue;
            claimed.add(target);
            target.setFilterTo(GenericStack.wrapInItemStack(stack));
        }
    }

    /** Slot met dezelfde key, anders het eerste lege slot dat nog niet in deze transfer gebruikt is. */
    private static FakeSlot findSlot(List<FakeSlot> slots, Set<FakeSlot> claimed, AEKey key) {
        FakeSlot empty = null;
        for (FakeSlot slot : slots) {
            if (claimed.contains(slot)) continue;
            GenericStack current = GenericStack.fromItemStack(slot.getItem());
            if (current == null) {
                if (empty == null) empty = slot;
            } else if (Objects.equals(current.what(), key)) {
                return slot;
            }
        }
        return empty;
    }

    private static List<FakeSlot> configSlots(UpgradeableMenu<?> menu) {
        List<FakeSlot> result = new ArrayList<>();
        for (Slot slot : menu.getSlots(SlotSemantics.CONFIG)) {
            if (slot instanceof FakeSlot fake) result.add(fake);
        }
        if (!result.isEmpty()) return result;
        for (Slot slot : menu.slots) {
            if (slot instanceof FakeSlot fake) result.add(fake);
        }
        return result;
    }
}
