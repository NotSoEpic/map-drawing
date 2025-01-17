package wawa.wayfinder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wawa.wayfinder.stampitem.BuiltInStamps;
import wawa.wayfinder.stampitem.StampGroups;

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
		BuiltInStamps.init();

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(StampGroups::generatePresetStamps);
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new StampGroups());
		PayloadTypeRegistry.playS2C().register(StampGroups.Payload.TYPE, StampGroups.Payload.STREAM_CODEC);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(((player, joined) -> {
			StampGroups.sendToPlayer(player);
		}));
	}
}