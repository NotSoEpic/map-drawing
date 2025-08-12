package wawa.wayfinder.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.Pin;
import wawa.wayfinder.map.MapScreen;
import wawa.wayfinder.platform.Services;
import wawa.wayfinder.platform.services.IKeyMappings;

public class InputListener {
    public static void tick(final Minecraft minecraft) {
        while (minecraft.level != null && Services.KEY_MAPPINGS.consume(IKeyMappings.Normal.OPEN_MAP)) {
            final Vector2d target = getEndingPosition(minecraft.player);
            final Vector2d playerPosition = new Vector2d((int) minecraft.player.getX(), (int) minecraft.player.getZ());
            if (target != null) {
                WayfinderClient.PAGE_MANAGER.addEphemeralPin(new Pin(Pin.SPYGLASS_EPHEMERAL, target));
                minecraft.setScreen(new MapScreen(playerPosition, target));
            } else {
                minecraft.setScreen(new MapScreen(playerPosition, playerPosition));
            }
        }
    }

    @Nullable
    public static Vector2d getEndingPosition(final LocalPlayer player) {
        final int distance = Minecraft.getInstance().options.getEffectiveRenderDistance();

        if (player.isUsingItem() && ((player.getMainHandItem().is(Items.SPYGLASS) && player.getUsedItemHand().equals(InteractionHand.MAIN_HAND))
                || (player.getOffhandItem().is(Items.SPYGLASS) && player.getUsedItemHand().equals(InteractionHand.OFF_HAND)))) {
            final BlockHitResult result = player.level().clip(new ClipContext(
                    player.getEyePosition(),
                    player.getEyePosition().add(player.getLookAngle().scale(distance * 16)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
            if (result.getType() != HitResult.Type.MISS) {
                return new Vector2d((int) result.getLocation().x, (int) result.getLocation().z);
            }
        }
        return null;
    }
}
