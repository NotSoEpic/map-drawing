package wawa.mapwright.mixin.coordinates_config;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wawa.mapwright.config.MapwrightClientConfig;
import wawa.mapwright.config.ReducedDebugLevel;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

	@WrapOperation(method = "handleDebugKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isReducedDebugInfo()Z"))
	private boolean mapwright$isReducedDebugInfo(final LocalPlayer instance, final Operation<Boolean> original) {
		if(MapwrightClientConfig.REDUCED_DEBUG_LEVEL.get() != ReducedDebugLevel.NONE) {
			return !MapwrightClientConfig.REDUCED_DEBUG_LEVEL.get().allowsCoordinates();
		}
		return original.call(instance);
	}
}
