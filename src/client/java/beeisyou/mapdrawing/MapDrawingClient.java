package beeisyou.mapdrawing;

import beeisyou.mapdrawing.mapmanager.MapRegions;
import beeisyou.mapdrawing.mapmanager.PlayerMovementHistory;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class MapDrawingClient implements ClientModInitializer {
	public static MapRegions regions = new MapRegions();
	public static PlayerMovementHistory movementHistory = new PlayerMovementHistory();
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