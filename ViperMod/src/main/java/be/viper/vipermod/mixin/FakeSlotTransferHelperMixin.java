package be.viper.vipermod.mixin;

import appeng.api.stacks.GenericStack;
import be.viper.vipermod.integration.ae2.TransferScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** esotericae2 1.1.x / extra_ae2_recipe_transfers: wrapInItemStack zit in overwriteFilters en addFilters. */
@Mixin(targets = ViperMixinPlugin.TARGET, remap = false)
public abstract class FakeSlotTransferHelperMixin {
    @ModifyArg(method = {"overwriteFilters", "addFilters"}, at = @At(value = "INVOKE", target = ViperMixinPlugin.WRAP))
    private GenericStack vipermod$scaleAmount(GenericStack stack) {
        return TransferScaler.scale(stack);
    }
}
