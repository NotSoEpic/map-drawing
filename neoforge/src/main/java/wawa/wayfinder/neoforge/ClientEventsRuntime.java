package wawa.wayfinder.neoforge;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.input.InputListener;

@EventBusSubscriber(modid = WayfinderClient.MOD_ID, value = Dist.CLIENT)
public class ClientEventsRuntime {
    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        InputListener.tick(Minecraft.getInstance());
    }
}
