package wawa.wayfinder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
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

    @ApiStatus.Internal
    public static final Gson WAYFINDER_GSON = new GsonBuilder().setLenient()
            .create();


    public static void init() {
        LOGGER.info("Hello from {}!", Services.PLATFORM.getPlatformName());
    }

    public static ResourceLocation id(final String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
