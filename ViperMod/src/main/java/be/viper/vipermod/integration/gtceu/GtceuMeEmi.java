package be.viper.vipermod.integration.gtceu;

import be.viper.vipermod.ViperConfig;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.ConfigWidget;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import appeng.api.stacks.AEKey;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

/**
 * EMI "+" in de GUI van een GTCEu ME Input Bus/Hatch: zet de receptinputs in de config-slots.
 * GT's eigen handler voor ModularUIContainer wijkt hier (zie GTEmiRecipeHandlerMixin).
 */
public final class GtceuMeEmi implements EmiRecipeHandler<ModularUIContainer> {
    private GtceuMeEmi() {}

    public static void register(EmiRegistry registry) {
        registry.addRecipeHandler(ModularUIContainer.MENUTYPE, new GtceuMeEmi());
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<ModularUIContainer> screen) {
        // Config-slots gebruiken geen echte items
        return new EmiPlayerInventory(List.of());
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        // EMI kiest per menutype de eerste handler die "ja" zegt; enkel ja in een ME bus/hatch-scherm
        return !recipe.getInputs().isEmpty() && MeConfigFiller.isMeConfigScreen();
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ModularUIContainer> context) {
        return !MeConfigFiller.inputs(recipe, MeConfigFiller.configWidgets()).isEmpty();
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<ModularUIContainer> context) {
        List<ConfigWidget> widgets = MeConfigFiller.configWidgets();
        Map<AEKey, Long> inputs = MeConfigFiller.inputs(recipe, widgets);
        if (inputs.isEmpty()) return false;
        MeConfigFiller.fill(inputs, widgets);
        return true;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<ModularUIContainer> context) {
        Component text = canCraft(recipe, context)
                ? Component.translatable("vipermod.gtceu.me_amount",
                        ViperConfig.GTCEU_ME_ITEM_AMOUNT.get(), ViperConfig.GTCEU_ME_FLUID_BUCKETS.get())
                        .withStyle(ChatFormatting.GRAY)
                : Component.translatable("vipermod.gtceu.me_nothing").withStyle(ChatFormatting.RED);
        return List.of(ClientTooltipComponent.create(text.getVisualOrderText()));
    }
}
