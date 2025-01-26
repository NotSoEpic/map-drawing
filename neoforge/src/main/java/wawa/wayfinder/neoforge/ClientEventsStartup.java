package wawa.wayfinder.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.input.KeyMappings;

@EventBusSubscriber(modid = WayfinderClient.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientEventsStartup {
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        KeyMappings.toRegister.forEach(event::register);
    }
}
