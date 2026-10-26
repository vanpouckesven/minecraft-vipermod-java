package be.viper.vipermod.integration.laserio;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/** EMI "+"-knop voor LaserIO-filters (Monifactory gebruikt EMI, geen JEI). */
public final class LaserIOEmi implements EmiRecipeHandler<AbstractContainerMenu> {
    private LaserIOEmi() {}

    @SuppressWarnings("unchecked")
    public static void register(EmiRegistry registry) {
        LaserIOEmi handler = new LaserIOEmi();
        for (LaserIOMenus.Menu<?> menu : LaserIOMenus.all()) {
            registry.addRecipeHandler((MenuType<AbstractContainerMenu>) menu.type(), handler);
        }
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<AbstractContainerMenu> screen) {
        // Een filter gebruikt geen echte items; leeg houdt EMI's "craftables" erbuiten
        return new EmiPlayerInventory(List.of());
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return !recipe.getInputs().isEmpty();
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<AbstractContainerMenu> context) {
        return resolve(recipe, context).error() == null;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<AbstractContainerMenu> context) {
        RecipeInputs.Result result = resolve(recipe, context);
        if (result.error() != null) return false;
        FilterTarget.of(context.getScreenHandler()).apply(result.entries());
        return true;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<AbstractContainerMenu> context) {
        RecipeInputs.Result result = resolve(recipe, context);
        if (result.error() == null) return List.of();
        return List.of(ClientTooltipComponent.create(result.error().getVisualOrderText()));
    }

    /** Eerste stack per ingredient (bij tags), met de hoeveelheid van het ingredient. */
    private static RecipeInputs.Result resolve(EmiRecipe recipe, EmiCraftContext<AbstractContainerMenu> context) {
        RecipeInputs inputs = new RecipeInputs();
        for (EmiIngredient ingredient : recipe.getInputs()) {
            if (ingredient.isEmpty()) continue;
            EmiStack stack = ingredient.getEmiStacks().get(0);
            int amount = (int) Math.min(ingredient.getAmount(), Integer.MAX_VALUE);
            if (stack.getKey() instanceof Item) {
                ItemStack itemStack = stack.getItemStack();
                itemStack.setCount(amount);
                inputs.addItem(itemStack);
            } else if (stack.getKey() instanceof Fluid fluid) {
                inputs.addFluid(new FluidStack(fluid, amount, stack.getNbt()));
            }
        }
        return inputs.resolve(FilterTarget.of(context.getScreenHandler()));
    }
}
