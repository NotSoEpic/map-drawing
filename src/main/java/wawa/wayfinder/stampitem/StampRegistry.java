package wawa.wayfinder.stampitem;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import wawa.wayfinder.AllComponents;
import wawa.wayfinder.AllItems;
import wawa.wayfinder.Wayfinder;

public class StampRegistry {
    public static final ResourceKey<Registry<StampTextureTooltipData>> REGISTRY_KEY = ResourceKey.createRegistryKey(Wayfinder.id("stamp"));
    public static void init() {
        DynamicRegistries.registerSynced(REGISTRY_KEY, StampTextureTooltipData.CODEC);
    }

    public static void generatePresetPaintings(FabricItemGroupEntries content) {
        content.getContext().holders().lookup(REGISTRY_KEY).ifPresent(registryLookup -> {
            registryLookup.listElements().forEach(reference -> {
                Wayfinder.LOGGER.info(reference.value().texture().toString());
                ItemStack itemStack = new ItemStack(AllItems.STAMP);
                itemStack.set(AllComponents.STAMP, new StampComponent(reference.value().texture()));
                content.accept(itemStack);
            });
        });
    }
}
