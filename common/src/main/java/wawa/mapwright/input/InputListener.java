package wawa.mapwright.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector3d;
import wawa.mapwright.DistantRaycast;
import wawa.mapwright.Helper;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.compat.multithread_testing.MultithreadedDHTerrainAccess;
import wawa.mapwright.data.SpyglassPins;
import wawa.mapwright.map.MapScreen;
import wawa.mapwright.platform.Services;
import wawa.mapwright.platform.services.IKeyMappings;

public class InputListener {
    public static void tick(final Minecraft minecraft) {
        while (minecraft.level != null && Services.KEY_MAPPINGS.consume(IKeyMappings.Normal.OPEN_MAP)) {
            final Vector2d playerPosition = new Vector2d((int) minecraft.player.getX(), (int) minecraft.player.getZ());

            minecraft.level.playLocalSound(minecraft.getInstance().player, SoundEvents.BOOK_PAGE_TURN, SoundSource.MASTER, 0.5f, 1.0f);

            if (Helper.isUsingSpyglass(minecraft.player)) {
                Vector3d target = getEndingPosition(minecraft.player);
                if (target != null) {
                    MapwrightClient.PAGE_MANAGER.getSpyglassPins().add(target);
                } else {
					target = new Vector3d(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ());
	                MapwrightClient.PAGE_MANAGER.getSpyglassPins().addDelayedRequest(MultithreadedDHTerrainAccess.createRequest(minecraft.player));
                }

	            minecraft.setScreen(new MapScreen(playerPosition, new Vector2d(target.x, target.z)));
	            return;
            } else {
                final int numPins = MapwrightClient.PAGE_MANAGER.getSpyglassPins().getPins().size();
                if (numPins >= 1) {
                    // If pins exist, move camera to average position
                    double avgX = 0;
                    double avgZ = 0;
                    for (SpyglassPins.PinData pin : MapwrightClient.PAGE_MANAGER.getSpyglassPins().getPins()) {
                        avgX += pin.position().x() / numPins;
                        avgZ += pin.position().z() / numPins;
                    }
                    minecraft.setScreen(new MapScreen(playerPosition, new Vector2d(avgX, avgZ)));
                    return;
                }
            }

            minecraft.setScreen(new MapScreen(playerPosition, playerPosition));
        }
    }

    @Nullable
    public static Vector3d getEndingPosition(final LocalPlayer player) {
        return DistantRaycast.clip(
                player,
                player.getEyePosition(),
                player.getLookAngle(),
                Minecraft.getInstance().options.getEffectiveRenderDistance() * 16,
		        MultithreadedDHTerrainAccess.INSTANCE.getViewDistance()
        );
    }
}
