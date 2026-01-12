package wawa.mapwright.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector3d;
import wawa.mapwright.DistantRaycast;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.compat.multithread_testing.DHBridge;
import wawa.mapwright.data.SpyglassPins;
import wawa.mapwright.map.MapScreen;
import wawa.mapwright.platform.MapWrightServices;
import wawa.mapwright.platform.services.IKeyMappings;

public class InputListener {
    public static void tick(final Minecraft minecraft) {
        while (minecraft.level != null && MapWrightServices.KEY_MAPPINGS.consume(IKeyMappings.Normal.OPEN_MAP)) {
            final Vector2d playerPosition = new Vector2d((int) minecraft.player.getX(), (int) minecraft.player.getZ());

            minecraft.level.playLocalSound(Minecraft.getInstance().player, SoundEvents.BOOK_PAGE_TURN, SoundSource.MASTER, 0.5f, 1.0f);

            if (MapwrightClient.PAGE_MANAGER.pageIO.getPagePath().endsWith("_0")) {
                MapwrightClient.PAGE_MANAGER.reloadPageIO(minecraft.level, minecraft);
            }

            if (minecraft.player.isScoping()) {
                Vector3d target = getEndingPosition(minecraft.player);
                if (target != null) {
                    MapwrightClient.PAGE_MANAGER.getSpyglassPins().add(target);
                } else {
					target = new Vector3d(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ());
	                MapwrightClient.PAGE_MANAGER.getSpyglassPins().addDelayedRequest(DHBridge.createRequest(minecraft.player));
                }

				MapwrightClient.targetPanningPosition.set(target.x, target.z);
	            minecraft.setScreen(new MapScreen(playerPosition));

	            return;
            } else {
                final int numPins = MapwrightClient.PAGE_MANAGER.getSpyglassPins().getPins().size();
                if (numPins >= 1) {
                    // If pins exist, move camera to average position
                    double avgX = 0;
                    double avgZ = 0;
                    for (final SpyglassPins.PinData pin : MapwrightClient.PAGE_MANAGER.getSpyglassPins().getPins()) {
                        avgX += pin.position().x() / numPins;
                        avgZ += pin.position().z() / numPins;
                    }

					MapwrightClient.targetPanningPosition.set(avgX, avgZ);
                    minecraft.setScreen(new MapScreen(playerPosition));
                    return;
                }
            }

	        MapwrightClient.targetPanningPosition.set(playerPosition);
            minecraft.setScreen(new MapScreen(playerPosition));
        }
    }

    @Nullable
    public static Vector3d getEndingPosition(final LocalPlayer player) {
        return DistantRaycast.clip(
                player,
                player.getEyePosition(),
                player.getLookAngle(),
                Minecraft.getInstance().options.getEffectiveRenderDistance() * 16,
		        DHBridge.getViewDistance()
        );
    }
}
