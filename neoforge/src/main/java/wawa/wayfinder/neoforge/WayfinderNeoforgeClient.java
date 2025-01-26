package wawa.wayfinder.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

import wawa.wayfinder.WayfinderClient;

@Mod(value = WayfinderClient.MOD_ID, dist = Dist.CLIENT)
public final class WayfinderNeoforgeClient {
    public WayfinderNeoforgeClient() {
        WayfinderClient.init();
    }
}
