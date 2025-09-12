package wawa.wayfinder.mixin.coordinates_config;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wawa.wayfinder.config.WayfinderClientConfig;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
	@Unique private static final HitResult wayfinder$EMPTY = BlockHitResult.miss(new Vec3(0.0f, 0.0f, 0.0f), Direction.NORTH, new BlockPos(0, 0, 0));

	@Shadow private HitResult block;
	@Shadow private HitResult liquid;

	@WrapOperation(
			method = "getGameInformation",
			at = {
					@At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 3),
					@At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 4),
					@At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 5),
			}
	)
	private <E> boolean wayfinder$getGameInformation1(List<E> instance, E e, Operation<Boolean> original) {
		if(WayfinderClientConfig.REDUCED_DEBUG_LEVEL.get().allowsCoordinates()) {
			return original.call(instance, e);
		}
		return false;
	}

	@WrapOperation(method = "getGameInformation", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 6))
	private <E> boolean wayfinder$getGameInformation2(List<E> instance, E e, Operation<Boolean> original) {
		if(WayfinderClientConfig.REDUCED_DEBUG_LEVEL.get().allowsRotation()) {
			return original.call(instance, e);
		}
		return false;
	}

	@Inject(method = "getSystemInformation", at = @At("HEAD"))
	private void wayfinder$getSystemInformation(CallbackInfoReturnable<List<String>> cir) {
		if(!WayfinderClientConfig.REDUCED_DEBUG_LEVEL.get().allowsCoordinates()) {
			this.block = wayfinder$EMPTY;
			this.liquid = wayfinder$EMPTY;
		}
	}

}