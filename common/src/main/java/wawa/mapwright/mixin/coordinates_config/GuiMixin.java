package wawa.mapwright.mixin.coordinates_config;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wawa.mapwright.config.MapwrightClientConfig;

@Mixin(Gui.class)
public class GuiMixin {
    @WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showDebugScreen()Z"))
	private boolean mapwright$showDebugScreen(final DebugScreenOverlay instance, final Operation<Boolean> original) {
        if(!MapwrightClientConfig.REDUCED_DEBUG_LEVEL.get().allowsRotation()) {
            return false;
        }
        return original.call(instance);
    }
}