package wawa.wayfinder;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import wawa.wayfinder.input.InputListener;
import wawa.wayfinder.map.MapScreen;

public class ClientEvents {
    public static void tick(final Minecraft client) {
        InputListener.tick(client);
        WayfinderClient.PAGE_MANAGER.tick();
        WayfinderClient.STAMP_HANDLER.tick();
        WayfinderClient.TOOL_MANAGER.get().tick(client.screen instanceof MapScreen);
    }

    public static void loadLevel(final Level level, final Minecraft client) {
        WayfinderClient.PAGE_MANAGER.saveAndClear();
        WayfinderClient.PAGE_MANAGER.reloadPageIO(level, client);
    }

    public static void leaveServer() {
        WayfinderClient.PAGE_MANAGER.saveAndClear();
        DistantRaycast.clearCache();
    }

    public static void postWorldRender(final MultiBufferSource bufferSource, final PoseStack poseStack, final float partialTick) {
        if (Helper.isUsingSpyglass(Minecraft.getInstance().player)) {
            WayfinderClient.PAGE_MANAGER.getSpyglassPins().render(bufferSource, poseStack, partialTick);
        }
    }
}
