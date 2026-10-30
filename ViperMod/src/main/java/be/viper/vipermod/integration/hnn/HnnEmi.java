package be.viper.vipermod.integration.hnn;

import be.viper.vipermod.ViperMod;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.HostileConfig;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.data.ModelTier;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** EMI-categorie: data model + input -> gekozen fabricator-drop + generalized prediction. */
public final class HnnEmi {
    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            new ResourceLocation(ViperMod.MOD_ID, "resource_simulator"), EmiStack.of(HnnContent.RESOURCE_SIMULATOR_ITEM.get()));

    public static void register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(HnnContent.RESOURCE_SIMULATOR_ITEM.get()));
        // Data models en mob predictions per model onderscheiden (HNN doet dit enkel voor JEI)
        Comparison byModel = Comparison.compareData(s -> DataModelItem.getStoredModel(s.getItemStack()).getId());
        registry.setDefaultComparison(Hostile.Items.DATA_MODEL.get(), byModel);
        registry.setDefaultComparison(Hostile.Items.PREDICTION.get(), byModel);

        for (DataModel model : DataModelRegistry.INSTANCE.getValues()) {
            ResourceLocation id = DataModelRegistry.INSTANCE.getKey(model);
            for (int i = 0; i < model.fabDrops().size(); i++) {
                registry.addRecipe(new SimulatorRecipe(model, id, i));
            }
        }
    }

    private static class SimulatorRecipe extends BasicEmiRecipe {
        private final int powerCost;

        SimulatorRecipe(DataModel model, ResourceLocation modelId, int drop) {
            super(CATEGORY, new ResourceLocation(ViperMod.MOD_ID, "/resource_simulator/" + modelId.getNamespace() + "/" + modelId.getPath() + "/" + drop), 118, 38);
            ItemStack modelStack = new ItemStack(Hostile.Items.DATA_MODEL.get());
            DataModelItem.setStoredModel(modelStack, model);
            DataModelItem.setData(modelStack, model.getTierData(ModelTier.SELF_AWARE));
            this.catalysts = List.of(EmiStack.of(modelStack));
            this.inputs = List.of(EmiStack.of(model.input()));
            this.outputs = List.of(EmiStack.of(model.fabDrops().get(drop)), EmiStack.of(model.baseDrop()));
            this.powerCost = model.simCost() + HostileConfig.fabPowerCost;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addSlot(this.catalysts.get(0), 0, 0).catalyst(true);
            widgets.addSlot(this.inputs.get(0), 18, 0);
            widgets.addFillingArrow(41, 1, ResourceSimulatorBlockEntity.RUNTIME * 50);
            widgets.addSlot(this.outputs.get(0), 72, 0).recipeContext(this);
            widgets.addSlot(this.outputs.get(1), 92, 0).recipeContext(this);
            widgets.addText(Component.translatable("vipermod.hnn.emi.cost", ResourceSimulatorBlockEntity.RUNTIME, this.powerCost), 0, 26, 0xFF404040, false);
        }
    }

    private HnnEmi() {}
}
