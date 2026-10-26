package be.viper.vipermod.mixin;

import com.mojang.logging.LogUtils;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Past een mixin enkel toe als de doelklasse er is én de wrapInItemStack-call staat waar die mixin hem verwacht.
 * Zo werkt ViperMod met esotericae2 1.0.x, 1.1.x en zonder esotericae2, zonder crash bij een onbekende versie.
 */
public class ViperMixinPlugin implements IMixinConfigPlugin {
    static final String TARGET = "com.quantumgarbage.esotericae2.FakeSlotTransferHelper";
    static final String WRAP = "Lappeng/api/stacks/GenericStack;wrapInItemStack(Lappeng/api/stacks/GenericStack;)Lnet/minecraft/world/item/ItemStack;";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PKG = "be.viper.vipermod.mixin.";
    private static final Map<String, Set<String>> REQUIRED_METHODS = Map.of(
            PKG + "FakeSlotTransferHelperLegacyMixin", Set.of("transfer"),
            PKG + "FakeSlotTransferHelperMixin", Set.of("overwriteFilters", "addFilters"));

    private Set<String> wrappingMethods;

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        Set<String> required = REQUIRED_METHODS.get(mixinClassName);
        if (required == null) return true;
        boolean apply = wrappingMethods(targetClassName).containsAll(required);
        if (apply) LOGGER.info("ViperMod: {} toegepast op {}", mixinClassName, targetClassName);
        return apply;
    }

    /** Methodes in de doelklasse die GenericStack.wrapInItemStack aanroepen (leeg als esotericae2 ontbreekt). */
    private Set<String> wrappingMethods(String targetClassName) {
        if (wrappingMethods != null) return wrappingMethods;
        wrappingMethods = new HashSet<>();
        ClassNode node;
        try {
            node = MixinService.getService().getBytecodeProvider().getClassNode(targetClassName);
        } catch (Exception e) {
            return wrappingMethods;
        }
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof MethodInsnNode call
                        && call.owner.equals("appeng/api/stacks/GenericStack")
                        && call.name.equals("wrapInItemStack")) {
                    wrappingMethods.add(method.name);
                }
            }
        }
        return wrappingMethods;
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
