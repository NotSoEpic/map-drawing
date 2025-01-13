package wawa.wayfinder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wawa.wayfinder.stampitem.BuiltInStamps;
import wawa.wayfinder.stampitem.StampRegistry;

public class Wayfinder implements ModInitializer {
	public static final String MOD_ID = "wayfinder";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		AllItems.init();
		AllComponents.init();
		StampRegistry.init();
		BuiltInStamps.init();

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(StampRegistry::generatePresetPaintings);
	}
}