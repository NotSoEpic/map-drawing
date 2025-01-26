package wawa.wayfinder.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.input.InputListener;
import wawa.wayfinder.input.KeyMappings;

public final class WayfinderFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WayfinderClient.init();

        KeyMappings.toRegister.forEach(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(InputListener::tick);
    }
}
