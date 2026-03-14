package wawa.mapwright.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.client.imgui.VeilImGui;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wawa.mapwright.map.MapScreen;

@Mixin(VeilRenderSystem.class)
public class VeilRenderSystemMixin {
    // veil's imgui implementation is stupid and forcefully overrides whether the cursor is hidden or not every frame
    // this is the only way to get it to stop doing that
    @WrapOperation(method = "beginFrame()V", at = @At(value = "INVOKE", target = "Lfoundry/veil/impl/client/imgui/VeilImGui;beginFrame()V"), remap = false)
    private static void beginBeatingVeilWithALeadPipe(VeilImGui instance, Operation<Void> original) {
        if(!(Minecraft.getInstance().screen instanceof MapScreen)) original.call(instance);
    }

    @WrapOperation(method = "endFrame()V", at = @At(value = "INVOKE", target = "Lfoundry/veil/impl/client/imgui/VeilImGui;endFrame()V"), remap = false)
    private static void endBeatingVeilWithALeadPipe(VeilImGui instance, Operation<Void> original) {
        if(!(Minecraft.getInstance().screen instanceof MapScreen)) original.call(instance);
    }
}
