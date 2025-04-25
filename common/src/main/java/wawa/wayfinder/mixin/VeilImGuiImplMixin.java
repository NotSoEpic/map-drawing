package wawa.wayfinder.mixin;

import foundry.veil.impl.client.imgui.VeilImGuiImpl;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VeilImGuiImpl.class)
public class VeilImGuiImplMixin {
    @Redirect(method = "Lfoundry/veil/impl/client/imgui/VeilImGuiImpl;endFrame()V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;)V"), remap = false)
    private void noComplainingAllowed(Logger instance, String message) {
        // noop
    }
}
