package be.viper.vipermod.mixin;

import appeng.api.stacks.GenericStack;
import appeng.menu.implementations.UpgradeableMenu;
import be.viper.vipermod.integration.ae2.ConfigSlotFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * esotericae2 1.0.x (Monifactory 0.13.8): transfer overschrijft alle config-slots en wist dus wat er al stond.
 * We vervangen het volledig door {@link ConfigSlotFiller}, die enkel lege of overeenkomende slots invult en schaalt.
 */
@Mixin(targets = ViperMixinPlugin.TARGET, remap = false)
public abstract class FakeSlotTransferHelperLegacyMixin {
    @Inject(method = "transfer", at = @At("HEAD"), cancellable = true)
    private void vipermod$keepExisting(UpgradeableMenu<?> menu, List<List<GenericStack>> inputs, CallbackInfo ci) {
        ConfigSlotFiller.fill(menu, inputs);
        ci.cancel();
    }
}
