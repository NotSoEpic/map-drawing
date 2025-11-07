package wawa.mapwright;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2d;
import org.slf4j.Logger;
import wawa.mapwright.data.PageManager;
import wawa.mapwright.map.stamp_bag.StampBagHandler;
import wawa.mapwright.map.tool.PanTool;
import wawa.mapwright.map.tool.ToolManager;
import wawa.mapwright.platform.MapWrightServices;

public final class MapwrightClient {
    public static final String MOD_ID = "mapwright";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static PageManager PAGE_MANAGER = new PageManager();
    public static ToolManager TOOL_MANAGER = new ToolManager(PanTool.INSTANCE);
    private static boolean DH_PRESENT = false;
	private static Boolean SABLE_PRESENT = null;

	public static Vector2d targetPanningPosition = new Vector2d();

    public static final StampBagHandler STAMP_HANDLER = new StampBagHandler();

	public static final int CHUNK_SIZE = 512;

    @ApiStatus.Internal
    public static final Gson MAPWRIGHT_GSON = new GsonBuilder().setLenient()
            .create();

    public static void init() {
        LOGGER.info("Hello from {}!", MapWrightServices.PLATFORM.getPlatformName());

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

	public static boolean isSablePresent() {
		if (SABLE_PRESENT == null) {
			SABLE_PRESENT = MapWrightServices.PLATFORM.isModLoaded("sable");
		}

		return SABLE_PRESENT;
	}

    public static ResourceLocation id(final String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
