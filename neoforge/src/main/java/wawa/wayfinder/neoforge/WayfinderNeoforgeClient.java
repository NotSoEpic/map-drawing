package wawa.wayfinder.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.neoforge.platform.NeoForgeConfig;

@Mod(value = WayfinderClient.MOD_ID, dist = Dist.CLIENT)
public final class WayfinderNeoforgeClient {
    public WayfinderNeoforgeClient(ModContainer container, final IEventBus modBus) {
        WayfinderClient.init();
        modBus.register(ClientEventsStartup.class);
        NeoForge.EVENT_BUS.register(ClientEventsRuntime.class);
        NeoForgeConfig.register(container);
    }
}
