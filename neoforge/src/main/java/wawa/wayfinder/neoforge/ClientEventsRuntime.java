package wawa.wayfinder.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.input.InputListener;
import wawa.wayfinder.map.tool.DrawTool;
import wawa.wayfinder.map.tool.Tool;

@EventBusSubscriber(modid = WayfinderClient.MOD_ID, value = Dist.CLIENT)
public class ClientEventsRuntime {
    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        InputListener.tick(Minecraft.getInstance());
        WayfinderClient.PAGE_MANAGER.tick();
    }

    @SubscribeEvent
    public static void levelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ClientLevel level && Minecraft.getInstance().isLocalServer()) {
            WayfinderClient.PAGE_MANAGER.reloadPageIO(level, Minecraft.getInstance());
            Tool.set(new DrawTool());
        }
    }

    @SubscribeEvent
    public static void serverJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        WayfinderClient.PAGE_MANAGER.reloadPageIO(event.getPlayer().level(), Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void levelUnload(LevelEvent.Unload event) {
        WayfinderClient.PAGE_MANAGER.saveAndClear();
    }
}
