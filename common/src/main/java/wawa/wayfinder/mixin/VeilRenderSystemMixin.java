package wawa.wayfinder.mixin;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.impl.client.imgui.VeilImGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VeilRenderSystem.class)
public class VeilRenderSystemMixin {
    // veil's imgui implementation is stupid and forcefully overrides whether the cursor is hidden or not every frame
    // this is the only way to get it to stop doing that
    @Redirect(method = "Lfoundry/veil/api/client/render/VeilRenderSystem;beginFrame()V", at = @At(value = "INVOKE", target = "Lfoundry/veil/impl/client/imgui/VeilImGui;beginFrame()V"), remap = false)
    private static void continueBeatingVeilWithALeadPipe(final VeilImGui instance) {
        // noop
    }
}
