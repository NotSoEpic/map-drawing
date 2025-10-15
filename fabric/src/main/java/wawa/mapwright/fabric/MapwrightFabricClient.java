package wawa.mapwright.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.neoforged.fml.config.ModConfig;
import wawa.mapwright.ClientEvents;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.config.MapwrightClientConfig;
import wawa.mapwright.fabric.input.FabricKeyMappings;

public final class MapwrightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MapwrightClient.init();

        FabricKeyMappings.init();
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::tick);
//        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientEvents.join(client.level, client));
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, level) -> ClientEvents.loadLevel(level, client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientEvents.leaveServer());
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> ClientEvents.postWorldRender(ctx.consumers(), ctx.matrixStack(), ctx.tickCounter().getRealtimeDeltaTicks()));
        NeoForgeConfigRegistry.INSTANCE.register(MapwrightClient.MOD_ID, ModConfig.Type.CLIENT, MapwrightClientConfig.CONFIG_SPEC);
    }
}
