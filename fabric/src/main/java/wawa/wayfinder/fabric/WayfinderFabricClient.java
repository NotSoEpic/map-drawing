package wawa.wayfinder.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import wawa.wayfinder.ClientEvents;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.input.InputListener;
import wawa.wayfinder.input.KeyMappings;
import wawa.wayfinder.map.tool.DrawTool;
import wawa.wayfinder.map.tool.Tool;

public final class WayfinderFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WayfinderClient.init();

        KeyMappings.toRegister.forEach(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::tick);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientEvents.join(client.level, client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientEvents.leave());
    }
}
