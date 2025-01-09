package beeisyou.mapdrawing;

import beeisyou.mapdrawing.mapmanager.MapRegions;
import beeisyou.mapdrawing.mapmanager.PlayerMovementHistory;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.MapColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

public class MapDrawingClient implements ClientModInitializer {
	public static MapRegions regions = new MapRegions();
	public static PlayerMovementHistory movementHistory = new PlayerMovementHistory();

	public static int penColor = MapColor.WHITE.color | 0xFF000000;
	public static int penSize = 3;
	public static boolean highlight = false;
	@Nullable
	public static Vector2d lastDrawnPos = null;
	@Override
	public void onInitializeClient() {
		MapBindings.init();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			MapBindings.inputEvents(client);
			movementHistory.tick(client.player);
			regions.tick();
		});
	}
}