package wawa.wayfinder.mixin.client;

import net.minecraft.client.renderer.texture.TextureManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// todo: remove this mixin once a sufficient amount of real stamp designs are present for testing and a billion nonexistent texture references aren't needed
@Mixin(TextureManager.class)
public class QuietIKnowItsAMissingTexture {
    @Redirect(method = "Lnet/minecraft/client/renderer/texture/TextureManager;loadTexture(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/texture/AbstractTexture;)Lnet/minecraft/client/renderer/texture/AbstractTexture;",
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void silence(Logger instance, String string, Object arg1, Object arg2) {
//        Wayfinder.LOGGER.info("Quietened exception for texture {}", arg1);
    }
}
