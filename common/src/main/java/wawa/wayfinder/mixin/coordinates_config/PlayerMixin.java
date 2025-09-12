package wawa.wayfinder.mixin.coordinates_config;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import wawa.wayfinder.config.ReducedDebugLevel;
import wawa.wayfinder.config.WayfinderClientConfig;

@Mixin(Player.class)
public class PlayerMixin {
	@WrapMethod(method = "isReducedDebugInfo")
	private boolean wayfinder$isReducedDebugInfo(Operation<Boolean> original) {
		if(WayfinderClientConfig.REDUCED_DEBUG_LEVEL.get() == ReducedDebugLevel.ALL) {
			return true;
		}
		return original.call();
	}
}
