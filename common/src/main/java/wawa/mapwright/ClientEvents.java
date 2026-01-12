package wawa.mapwright;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import wawa.mapwright.input.InputListener;
import wawa.mapwright.map.MapScreen;

public class ClientEvents {
    public static void tick(final Minecraft client) {
        InputListener.tick(client);
        MapwrightClient.PAGE_MANAGER.tick();
        MapwrightClient.STAMP_HANDLER.tick();
        MapwrightClient.TOOL_MANAGER.get().tick(client.screen instanceof MapScreen);
    }

    public static void loadLevel(final Level level, final Minecraft client) {
        MapwrightClient.PAGE_MANAGER.saveAndClear();
        MapwrightClient.PAGE_MANAGER.reloadPageIO(level, client);
    }

    public static void leaveServer() {
        MapwrightClient.PAGE_MANAGER.saveAndClear();
        DistantRaycast.clearCache();
    }

    public static void postWorldRender(final MultiBufferSource bufferSource, final PoseStack poseStack, final float partialTick) {
        if (Minecraft.getInstance().player.isScoping()) {
            MapwrightClient.PAGE_MANAGER.getSpyglassPins().render(bufferSource, poseStack, partialTick);
        }
    }
}
