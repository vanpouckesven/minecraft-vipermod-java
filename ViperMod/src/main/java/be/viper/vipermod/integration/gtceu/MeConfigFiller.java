package be.viper.vipermod.integration.gtceu;

import be.viper.vipermod.ViperConfig;
import be.viper.vipermod.mixin.LdlibWidgetInvoker;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEFluidConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEItemConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.ConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEConfigSlotWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEFluidConfigSlotWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEItemConfigSlotWidget;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Zet receptinputs in de config-slots van GTCEu's ME Input Bus/Hatch (en stocking-varianten).
 * Items: minstens {@code meItemAmount}, fluids: minstens {@code meFluidBuckets} buckets, nooit minder dan het recept.
 * Zelfde regels als {@link be.viper.vipermod.integration.ae2.ConfigSlotFiller}: bestaande slots blijven staan.
 */
public final class MeConfigFiller {
    /** AEConfigSlotWidget.AMOUNT_CHANGE_ID (protected in GTCEu). */
    private static final int AMOUNT_CHANGE_ID = 1002;

    private MeConfigFiller() {}

    /** Config-widgets van het GT-scherm onder EMI (ook als EMI's receptscherm ervoor staat). */
    public static List<ConfigWidget> configWidgets() {
        AbstractContainerScreen<?> screen = EmiApi.getHandledScreen();
        if (screen == null || !(screen.getMenu() instanceof ModularUIContainer container)) return List.of();
        List<ConfigWidget> result = new ArrayList<>();
        for (Widget widget : container.getModularUI().getFlatWidgetCollection()) {
            if (widget instanceof ConfigWidget config && !config.isAutoPull()) result.add(config);
        }
        return result;
    }

    public static boolean isMeConfigScreen() {
        return !configWidgets().isEmpty();
    }

    /** Inputs die in dit scherm passen (items in een bus, fluids in een hatch), samengeteld per key. */
    public static Map<AEKey, Long> inputs(EmiRecipe recipe, List<ConfigWidget> widgets) {
        boolean items = widgets.stream().anyMatch(w -> w instanceof AEItemConfigWidget);
        boolean fluids = widgets.stream().anyMatch(w -> w instanceof AEFluidConfigWidget);
        Map<AEKey, Long> merged = new LinkedHashMap<>();
        for (EmiIngredient ingredient : recipe.getInputs()) {
            if (ingredient.isEmpty() || ingredient.getAmount() <= 0) continue;
            // Bij tags: eerste alternatief
            EmiStack stack = ingredient.getEmiStacks().get(0);
            AEKey key = null;
            if (items && stack.getKey() instanceof Item) key = AEItemKey.of(stack.getItemStack());
            else if (fluids && stack.getKey() instanceof Fluid fluid) key = AEFluidKey.of(fluid, stack.getNbt());
            if (key != null) merged.merge(key, ingredient.getAmount(), Long::sum);
        }
        return merged;
    }

    public static void fill(Map<AEKey, Long> inputs, List<ConfigWidget> widgets) {
        Set<AEConfigSlotWidget> claimed = new HashSet<>();
        for (Map.Entry<AEKey, Long> entry : inputs.entrySet()) {
            AEKey key = entry.getKey();
            long amount = scale(key, entry.getValue());
            for (ConfigWidget widget : widgets) {
                if (key instanceof AEItemKey item && widget instanceof AEItemConfigWidget) {
                    AEItemConfigSlotWidget slot = (AEItemConfigSlotWidget) findSlot(widget, claimed, key);
                    if (slot == null) continue;
                    claimed.add(slot);
                    // UPDATE stuurt een ItemStack (count als byte), dus de hoeveelheid apart zetten
                    slot.acceptItem(item.toStack());
                    if (!widget.isStocking()) {
                        ((LdlibWidgetInvoker) slot).vipermod$writeClientAction(AMOUNT_CHANGE_ID,
                                buf -> buf.writeVarLong(amount));
                    }
                    break;
                }
                if (key instanceof AEFluidKey fluid && widget instanceof AEFluidConfigWidget) {
                    AEFluidConfigSlotWidget slot = (AEFluidConfigSlotWidget) findSlot(widget, claimed, key);
                    if (slot == null) continue;
                    claimed.add(slot);
                    slot.acceptFluid(fluid.toStack((int) Math.min(amount, Integer.MAX_VALUE)));
                    break;
                }
            }
        }
    }

    private static long scale(AEKey key, long recipeAmount) {
        long minimum = key instanceof AEFluidKey
                ? ViperConfig.GTCEU_ME_FLUID_BUCKETS.get() * 1000L
                : ViperConfig.GTCEU_ME_ITEM_AMOUNT.get();
        return Math.max(recipeAmount, minimum);
    }

    /**
     * Slot met dezelfde key, anders het eerste lege slot dat nog niet in deze transfer gebruikt is.
     * De client ziet de nieuwe config pas na de server-sync, daarom houden we zelf bij wat geclaimd is.
     */
    @Nullable
    private static AEConfigSlotWidget findSlot(ConfigWidget widget, Set<AEConfigSlotWidget> claimed, AEKey key) {
        AEConfigSlotWidget empty = null;
        int index = 0; // GTCEu voegt de slot-widgets toe in volgorde van hun index
        for (Widget child : widget.widgets) {
            if (!(child instanceof AEConfigSlotWidget slot)) continue;
            GenericStack current = widget.getDisplay(index++).getConfig();
            if (claimed.contains(slot)) continue;
            if (current == null) {
                if (empty == null) empty = slot;
            } else if (Objects.equals(current.what(), key)) {
                return slot;
            }
        }
        return empty;
    }
}
