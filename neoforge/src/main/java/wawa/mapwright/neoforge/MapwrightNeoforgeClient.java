package wawa.mapwright.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.config.MapwrightClientConfig;

@Mod(value = MapwrightClient.MOD_ID, dist = Dist.CLIENT)
public final class MapwrightNeoforgeClient {
    public MapwrightNeoforgeClient(ModContainer container, final IEventBus modBus) {
        MapwrightClient.init();
        modBus.register(ClientEventsStartup.class);
        NeoForge.EVENT_BUS.register(ClientEventsRuntime.class);
        container.registerConfig(ModConfig.Type.CLIENT, MapwrightClientConfig.CONFIG_SPEC);
    }
}
