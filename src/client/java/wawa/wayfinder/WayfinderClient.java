package wawa.wayfinder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import wawa.wayfinder.color.ColorPalette;
import wawa.wayfinder.color.ColorPaletteManager;
import wawa.wayfinder.mapmanager.MapRegions;
import wawa.wayfinder.mapmanager.PlayerMovementHistory;
import wawa.wayfinder.mixin.client.BiomeAccessAccessor;
import wawa.wayfinder.stampitem.StampComponent;
import wawa.wayfinder.stampitem.StampGroups;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class WayfinderClient implements ClientModInitializer {
	public static MapRegions regions = new MapRegions();
	public static PlayerMovementHistory movementHistory = new PlayerMovementHistory();

	public static ColorPalette palette;
	@Nullable
	public static Vector2i lastDrawnPos = null;

	private static WeakReference<ClientLevel> currentWorld = new WeakReference<>(null); //weak reference so we don't keep the last world loaded in memory if we leave the server lol

	@Override
	public void onInitializeClient() {
		Wayfinder.LOGGER.info("wawa");
		MapBindings.init();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			MapBindings.inputEvents(client);
			movementHistory.tick(client.player);
			regions.tick();
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			boolean connectedTolocal = client.isLocalServer();
			Wayfinder.LOGGER.debug("Connecting to {}", client.level);

			long seed = ((BiomeAccessAccessor) client.level.getBiomeManager()).getBiomeZoomSeed();

			String name;
			if (connectedTolocal) {
				name = client.getSingleplayerServer().getWorldData().getLevelName();
			} else {
				name = client.getCurrentServer().name;
			}

			UUID uuid = Mth.createInsecureUUID(RandomSource.create(seed));
			Wayfinder.LOGGER.debug("Generating UUID from {}; {}_{}", seed, uuid, name);

			WayfinderClient.regions.setRegionPathGeneral(uuid, name, connectedTolocal);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			Wayfinder.LOGGER.debug("Disconnecting from {}", client.level);

			WayfinderClient.regions.save(true);
			WayfinderClient.regions.unsafeClear();
			WayfinderClient.regions.clearRegionPath();
			WayfinderClient.movementHistory.clear();
		});

		TooltipComponentCallback.EVENT.register((data -> {
			if (data instanceof StampComponent component) {
				return new ClientStampTooltipComponent(component);
			}
			return null;
		}));

		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ColorPaletteManager());
		ClientPlayNetworking.registerGlobalReceiver(StampGroups.Payload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				StampGroups.handlePayload(payload);
			});
		});
	}
}