package wawa.wayfinder.fabric.platform;

import wawa.wayfinder.WayfinderConfig;
import wawa.wayfinder.platform.services.IConfig;

public class FabricConfig implements IConfig {
    @Override
    public WayfinderConfig config() {
        return () -> true;
    }
}
