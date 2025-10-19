package wawa.mapwright.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import wawa.mapwright.ClientEvents;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.mixin.LevelRendererAccessor;

@EventBusSubscriber(modid = MapwrightClient.MOD_ID, value = Dist.CLIENT)
public class ClientEventsRuntime {
    @SubscribeEvent
    public static void clientTick(final ClientTickEvent.Post event) {
        ClientEvents.tick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void levelLoad(final LevelEvent.Load event) {
        if (event.getLevel() instanceof final ClientLevel level) {
            ClientEvents.loadLevel(level, Minecraft.getInstance());
        }
    }

    @SubscribeEvent
    public static void levelUnload(final LevelEvent.Unload event) {
        MapwrightClient.PAGE_MANAGER.saveAndClear();
    }

    @SubscribeEvent
    public static void postWorldRender(final RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            final MultiBufferSource bufferSource = ((LevelRendererAccessor)event.getLevelRenderer()).getRenderBuffers().bufferSource();
            ClientEvents.postWorldRender(bufferSource, event.getPoseStack(), event.getPartialTick().getRealtimeDeltaTicks());
        }
    }
}
