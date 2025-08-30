package wawa.wayfinder;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.tool.CopyTool;
import wawa.wayfinder.map.tool.ToolManager;
import wawa.wayfinder.platform.Services;

public final class WayfinderClient {
    public static final String MOD_ID = "wayfinder";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static PageManager PAGE_MANAGER = new PageManager();
    public static ToolManager TOOL_MANAGER = new ToolManager(CopyTool.INSTANCE); // todo dont commit this
    private static boolean DH_PRESENT = false;

    public static void init() {
        LOGGER.info("Hello from {}!", Services.PLATFORM.getPlatformName());

        // https://gitlab.com/distant-horizons-team/distant-horizons-api-example/-/blob/main/Fabric-ApiDemo/src/main/java/com/example/ExampleMod.java
        try {
            final Class<?> dhApiClass = Class.forName("com.seibel.distanthorizons.api.DhApi");
            LOGGER.info("Found Distant Horizons API!");
            DH_PRESENT = true;
        } catch (final ClassNotFoundException ignored) {
        }
    }

    public static boolean isDHPresent() {
        return DH_PRESENT;
    }

    public static ResourceLocation id(final String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
