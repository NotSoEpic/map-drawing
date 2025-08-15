package wawa.wayfinder.neoforge;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.neoforge.data.LangGen;
import wawa.wayfinder.neoforge.input.NeoKeyMappings;

@EventBusSubscriber(modid = WayfinderClient.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientEventsStartup {
    @SubscribeEvent
    public static void registerBindings(final RegisterKeyMappingsEvent event) {
        event.register(NeoKeyMappings.OPEN_MAP.get());
        event.register(NeoKeyMappings.UNDO.get());
        event.register(NeoKeyMappings.REDO.get());
        event.register(NeoKeyMappings.HAND.get());
        event.register(NeoKeyMappings.PENCIL.get());
        event.register(NeoKeyMappings.BRUSH.get());
        event.register(NeoKeyMappings.ERASER.get());
        event.register(NeoKeyMappings.SWAP.get());
    }

    @SubscribeEvent
    public static void gatherData(final GatherDataEvent event) {
        WayfinderClient.LOGGER.info("Generating data!!! :3");

        final DataGenerator generator = event.getGenerator();
        final PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new LangGen(output));
    }
}
