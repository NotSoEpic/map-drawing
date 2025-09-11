package wawa.wayfinder.neoforge.platform;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.WayfinderConfig;
import wawa.wayfinder.platform.services.IConfig;

public class NeoForgeConfig implements IConfig {
    private static final Pair<Config, ModConfigSpec> CLIENT = new ModConfigSpec.Builder().configure(Config::new);

    @Override
    public WayfinderConfig config() {
        return CLIENT.getLeft();
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT.getRight());
    }

    public static class Config implements WayfinderConfig {
        private final ModConfigSpec.BooleanValue hideCoordinates;

        public Config(ModConfigSpec.Builder builder) {
            builder.push(WayfinderClient.MOD_ID);

            this.hideCoordinates = builder
                    .comment("Whether to hide coordinates from the debug screen")
                    .define("hideCoordinates", false);

            builder.pop();
        }

        @Override
        public boolean hideCoordinates() {
            return hideCoordinates.get();
        }
    }
}
