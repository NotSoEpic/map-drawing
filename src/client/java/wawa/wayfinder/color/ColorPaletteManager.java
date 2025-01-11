package wawa.wayfinder.color;

import wawa.wayfinder.Wayfinder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ColorPaletteManager implements SimpleSynchronousResourceReloadListener {
	private static final Map<Identifier, ColorPalette> COLOR_PALETTES = new HashMap<>();

	public static ColorPalette get(Identifier id) {
		return COLOR_PALETTES.get(id);
	}

	@Override
	public Identifier getFabricId() {
		return Wayfinder.id("color_palettes");
	}

	@Override
	public void reload(ResourceManager manager) {
		COLOR_PALETTES.clear();

		Map<Identifier, Resource> resources = manager.findResources("color_palettes", path -> path.getPath().endsWith(".json"));
		for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
			Identifier key = entry.getKey();
			Identifier id = Identifier.of(key.getNamespace(), key.getPath().split("/")[1].replace(".json", ""));

			try(InputStream stream = entry.getValue().getInputStream()) {
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
