package wawa.wayfinder.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.input.InputListener;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow @Nullable public LocalPlayer player;

    @Shadow @Final public Options options;

    @Unique
    private boolean wayfinder$wasPressed = false;

    // this.player.isUsingItem() -> this.options.keyAttack.consumeClick()
    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 0))
    private void placeSpyglassPin(final CallbackInfo ci) {
        if (this.options.keyAttack.isDown()) {
            if (!this.wayfinder$wasPressed && Helper.isUsingSpyglass(this.player)) {
                final Vector3d raycast = InputListener.getEndingPosition(this.player);
                if (raycast != null) {
                    WayfinderClient.PAGE_MANAGER.getSpyglassPins().add(raycast);
                }
            }
            this.wayfinder$wasPressed = true;
        } else {
            this.wayfinder$wasPressed = false;
        }
    }
}
