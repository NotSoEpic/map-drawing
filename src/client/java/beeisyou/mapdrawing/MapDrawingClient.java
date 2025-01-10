package beeisyou.mapdrawing;

import beeisyou.mapdrawing.color.ColorPalette;
import beeisyou.mapdrawing.color.ColorPaletteManager;
import beeisyou.mapdrawing.mapmanager.MapRegions;
import beeisyou.mapdrawing.mapmanager.PlayerMovementHistory;
import beeisyou.mapdrawing.mixin.client.BiomeAccessAccessor;
import beeisyou.mapdrawing.stampitem.StampTextureTooltipData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.block.MapColor;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class MapDrawingClient implements ClientModInitializer {
	public static MapRegions regions = new MapRegions();
	public static PlayerMovementHistory movementHistory = new PlayerMovementHistory();

	public static ColorPalette palette;
	public static int penColorIndex = 0;
	public static int penSize = 3;
	public static boolean highlight = false;
	@Nullable
	public static Vector2d lastDrawnPos = null;

	private static WeakReference<ClientWorld> currentWorld = new WeakReference<>(null); //weak reference so we don't keep the last world loaded in memory if we leave the server lol

	@Override
	public void onInitializeClient() {
		MapBindings.init();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			MapBindings.inputEvents(client);
			movementHistory.tick(client.player);
			regions.tick();
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			boolean connectedTolocal = client.isConnectedToLocalServer();
			MapDrawing.LOGGER.debug("Connecting to {}", client.world);

			long seed = ((BiomeAccessAccessor) client.world.getBiomeAccess()).getSeed();

			String name;
			if (connectedTolocal) {
				name = client.getServer().getSaveProperties().getLevelName();
			} else {
				name = client.getCurrentServerEntry().name;
			}

			UUID uuid = MathHelper.randomUuid(Random.create(seed));
			MapDrawing.LOGGER.debug("Generating UUID from {}; {}_{}", seed, uuid, name);

			MapDrawingClient.regions.setRegionPathGeneral(uuid, name, connectedTolocal);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			MapDrawing.LOGGER.debug("Disconnecting from {}", client.world);

			MapDrawingClient.regions.save();
			MapDrawingClient.regions.clear();
			MapDrawingClient.regions.clearRegionPath();
			MapDrawingClient.movementHistory.clear();
		});

		TooltipComponentCallback.EVENT.register((data -> {
			if (data instanceof StampTextureTooltipData stampTextureComponent) {
				return new StampTooltipComponent(stampTextureComponent.texture());
			}
			return null;
		}));

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ColorPaletteManager());
	}
}