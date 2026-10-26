package be.viper.vipermod.mixin;

import appeng.api.stacks.GenericStack;
import be.viper.vipermod.integration.ae2.TransferScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** esotericae2 1.0.x (Monifactory 0.13.8): wrapInItemStack zit rechtstreeks in transfer. */
@Mixin(targets = ViperMixinPlugin.TARGET, remap = false)
public abstract class FakeSlotTransferHelperLegacyMixin {
    @ModifyArg(method = "transfer", at = @At(value = "INVOKE", target = ViperMixinPlugin.WRAP))
    private GenericStack vipermod$scaleAmount(GenericStack stack) {
        return TransferScaler.scale(stack);
    }
}
