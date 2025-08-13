package wawa.wayfinder.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import wawa.wayfinder.ClientEvents;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.fabric.input.FabricKeyMappings;

public final class WayfinderFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WayfinderClient.init();

        FabricKeyMappings.init();
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::tick);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientEvents.join(client.level, client));
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, level) -> ClientEvents.join(level, client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientEvents.leave());
        WorldRenderEvents.BEFORE_ENTITIES.register(ctx -> ClientEvents.postWorldRender(ctx.consumers(), ctx.matrixStack(), ctx.tickCounter().getRealtimeDeltaTicks()));
    }
}
