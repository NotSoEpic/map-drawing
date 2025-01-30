package wawa.wayfinder.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector2d;
import wawa.wayfinder.map.MapScreen;

public class InputListener {
    public static void tick(Minecraft minecraft) {
        while (minecraft.level != null && KeyMappings.NormalMappings.OPEN_MAP.mapping.
                consumeClick()) {
            minecraft.setScreen(new MapScreen(
                    new Vector2d((int) minecraft.player.getX(), (int) minecraft.player.getZ()),
                    getEndingPosition(minecraft.player)));
        }
    }

    private static Vector2d getEndingPosition(LocalPlayer player) {
        int distance = Minecraft.getInstance().options.getEffectiveRenderDistance();

        if (player.getMainHandItem().is(Items.SPYGLASS) || player.getOffhandItem().is(Items.SPYGLASS)) {
            BlockHitResult result = player.level().clip(new ClipContext(
                    player.getEyePosition(),
                    player.getEyePosition().add(player.getLookAngle().scale(distance * 16)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
            if (result.getType() != HitResult.Type.MISS) {
                return new Vector2d((int) result.getLocation().x, (int) result.getLocation().z);
            }
        }
        return new Vector2d((int) player.getX(), (int) player.getZ());
    }
}
