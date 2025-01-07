package beeisyou.mapdrawing;

import beeisyou.mapdrawing.mapmanager.MapManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class MapDrawingClient implements ClientModInitializer {
	public static MapManager mapManager = new MapManager();
	@Override
	public void onInitializeClient() {
		MapBindings.init();
		ClientTickEvents.END_CLIENT_TICK.register(MapBindings::inputEvents);

	}
}