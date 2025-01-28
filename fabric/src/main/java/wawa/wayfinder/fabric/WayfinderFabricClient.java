package wawa.wayfinder.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.input.InputListener;
import wawa.wayfinder.input.KeyMappings;

public final class WayfinderFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WayfinderClient.init();

        KeyMappings.toRegister.forEach(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(InputListener::tick);
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> WayfinderClient.PAGE_MANAGER.reloadPageIO(client.level, client)));
        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> WayfinderClient.PAGE_MANAGER.saveAndClear()));
    }
}
