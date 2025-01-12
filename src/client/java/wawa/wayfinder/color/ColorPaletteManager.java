package wawa.wayfinder.color;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import wawa.wayfinder.Wayfinder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ColorPaletteManager implements SimpleSynchronousResourceReloadListener {
	private static final Map<ResourceLocation, ColorPalette> COLOR_PALETTES = new HashMap<>();

	public static ColorPalette get(ResourceLocation id) {
		return COLOR_PALETTES.get(id);
	}

	@Override
	public ResourceLocation getFabricId() {
		return Wayfinder.id("color_palettes");
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		COLOR_PALETTES.clear();

		Map<ResourceLocation, Resource> resources = manager.listResources("color_palettes", path -> path.getPath().endsWith(".json"));
		for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
			ResourceLocation key = entry.getKey();
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getPath().split("/")[1].replace(".json", ""));

			try(InputStream stream = entry.getValue().open()) {
				InputStreamReader reader = new InputStreamReader(stream);
				JsonElement json = JsonParser.parseReader(reader);

				DataResult<ColorPalette> result = ColorPalette.CODEC.parse(JsonOps.INSTANCE, json);

				ColorPalette colorPalette = result.getOrThrow();

				COLOR_PALETTES.put(id, colorPalette);
			} catch (Exception e) {
				Wayfinder.LOGGER.error(e.getMessage());
			}
		}
	}
}
