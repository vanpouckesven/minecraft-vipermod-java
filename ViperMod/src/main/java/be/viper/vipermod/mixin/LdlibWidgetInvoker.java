package be.viper.vipermod.mixin;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Consumer;

/** LDLib's writeClientAction is protected; nodig om de hoeveelheid van een GTCEu ME config-slot te zetten. */
@Mixin(value = Widget.class, remap = false)
public interface LdlibWidgetInvoker {
    @Invoker("writeClientAction")
    void vipermod$writeClientAction(int id, Consumer<FriendlyByteBuf> data);
}
