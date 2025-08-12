package wawa.wayfinder.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.Pin;
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
            if (!this.wayfinder$wasPressed && this.player.getItemInHand(this.player.getUsedItemHand()).is(Items.SPYGLASS)) {
                final Vector2d raycast = InputListener.getEndingPosition(this.player);
                if (raycast != null) {
                    WayfinderClient.PAGE_MANAGER.addEphemeralPin(new Pin(Pin.SPYGLASS_EPHEMERAL, new Vector2d((int) raycast.x, (int) raycast.y)));
                }
            }
            this.wayfinder$wasPressed = true;
        } else {
            this.wayfinder$wasPressed = false;
        }
    }
}
