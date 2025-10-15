package wawa.mapwright.mixin.coordinates_config;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import wawa.mapwright.config.ReducedDebugLevel;
import wawa.mapwright.config.MapwrightClientConfig;

@Mixin(Player.class)
public class PlayerMixin {
	@WrapMethod(method = "isReducedDebugInfo")
	private boolean mapwright$isReducedDebugInfo(Operation<Boolean> original) {
		if(MapwrightClientConfig.REDUCED_DEBUG_LEVEL.get() == ReducedDebugLevel.ALL) {
			return true;
		}
		return original.call();
	}
}
