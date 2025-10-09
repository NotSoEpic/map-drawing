package wawa.wayfinder.mixin.coordinates_config;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wawa.wayfinder.config.WayfinderClientConfig;

@Mixin(Gui.class)
public class GuiMixin {
    @WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showDebugScreen()Z"))
            private boolean wayfinder$showDebugScreen(DebugScreenOverlay instance, Operation<Boolean> original) {
        if(!WayfinderClientConfig.REDUCED_DEBUG_LEVEL.get().allowsRotation()) {
            return false;
        }
        return original.call(instance);
    }
}