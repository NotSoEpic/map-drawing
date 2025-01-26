package wawa.wayfinder.fabric;

import net.fabricmc.api.ClientModInitializer;
import wawa.wayfinder.WayfinderClient;

public final class WayfinderFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WayfinderClient.init();
    }
}
