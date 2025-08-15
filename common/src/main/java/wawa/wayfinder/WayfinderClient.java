package wawa.wayfinder;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.tool.PanTool;
import wawa.wayfinder.map.tool.ToolManager;
import wawa.wayfinder.platform.Services;

public final class WayfinderClient {
    public static final String MOD_ID = "wayfinder";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static PageManager PAGE_MANAGER = new PageManager();
    public static ToolManager TOOL_MANAGER = new ToolManager(PanTool.INSTANCE);

    public static void init() {
        LOGGER.info("Hello from {}!", Services.PLATFORM.getPlatformName());
    }

    public static ResourceLocation id(final String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
