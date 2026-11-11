package be.viper.vipermod.mixin;

import be.viper.vipermod.integration.gtceu.MeConfigFiller;
import dev.emi.emi.api.recipe.EmiRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * GT's EMI-handler geldt voor elk GTCEu-scherm en verplaatst echte items. In een ME bus/hatch-scherm moet
 * {@link be.viper.vipermod.integration.gtceu.GtceuMeEmi} het overnemen, ongeacht de volgorde waarin EMI de handlers kreeg.
 */
@Mixin(targets = "com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipeHandler", remap = false)
public abstract class GTEmiRecipeHandlerMixin {
    @Inject(method = "supportsRecipe", at = @At("HEAD"), cancellable = true)
    private void vipermod$yieldToMeConfig(EmiRecipe recipe, CallbackInfoReturnable<Boolean> cir) {
        if (MeConfigFiller.isMeConfigScreen()) cir.setReturnValue(false);
    }
}
