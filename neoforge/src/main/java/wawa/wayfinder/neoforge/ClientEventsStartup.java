package wawa.wayfinder.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.neoforge.input.NeoKeyMappings;

@EventBusSubscriber(modid = WayfinderClient.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientEventsStartup {
    @SubscribeEvent
    public static void registerBindings(final RegisterKeyMappingsEvent event) {
        event.register(NeoKeyMappings.OPEN_MAP.get());
        event.register(NeoKeyMappings.UNDO.get());
        event.register(NeoKeyMappings.PENCIL.get());
        event.register(NeoKeyMappings.BRUSH.get());
        event.register(NeoKeyMappings.SWAP.get());
    }
}
