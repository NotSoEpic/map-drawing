package wawa.wayfinder.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector3d;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.Pin;
import wawa.wayfinder.map.MapScreen;
import wawa.wayfinder.platform.Services;
import wawa.wayfinder.platform.services.IKeyMappings;

public class InputListener {
    public static void tick(final Minecraft minecraft) {
        while (minecraft.level != null && Services.KEY_MAPPINGS.consume(IKeyMappings.Normal.OPEN_MAP)) {
            final Vector3d target = getEndingPosition(minecraft.player);
            final Vector2d playerPosition = new Vector2d((int) minecraft.player.getX(), (int) minecraft.player.getZ());

            minecraft.level.playLocalSound(minecraft.getInstance().player, SoundEvents.BOOK_PAGE_TURN, SoundSource.MASTER, 0.5f, 1.0f);

            if (target != null) {
                WayfinderClient.PAGE_MANAGER.getSpyglassPins().add(target);
                minecraft.setScreen(new MapScreen(playerPosition, new Vector2d(target.x, target.z)));
            } else {
                minecraft.setScreen(new MapScreen(playerPosition, playerPosition));
            }
        }
    }

    @Nullable
    public static Vector3d getEndingPosition(final LocalPlayer player) {
        final int distance = Minecraft.getInstance().options.getEffectiveRenderDistance();

        if (Helper.isUsingSpyglass(player)) {
            final BlockHitResult result = player.level().clip(new ClipContext(
                    player.getEyePosition(),
                    player.getEyePosition().add(player.getLookAngle().scale(distance * 16)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
            if (result.getType() != HitResult.Type.MISS) {
                return new Vector3d(result.getLocation().x, result.getLocation().y, result.getLocation().z);
            }
        }
        return null;
    }
}
