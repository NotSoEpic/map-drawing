package wawa.wayfinder.stampitem;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import wawa.wayfinder.AllComponents;
import wawa.wayfinder.AllItems;
import wawa.wayfinder.Wayfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BuiltInStamps {
    private static final HashMap<ResourceKey<LootTable>, List<StampData>> injections = new HashMap<>();
    private static ResourceLocation registerAndInject(String stampId, ResourceKey<LootTable> lootTable, int weight) {
        ResourceLocation resource = Wayfinder.id(stampId);
        injections.computeIfAbsent(ResourceKey.create(Registries.LOOT_TABLE, lootTable.location()),
                (awa) -> new ArrayList<>()).add(new StampData(resource, weight));
        return resource;
    }

    // todo: placeholder injections
    public static ResourceLocation X = registerAndInject("x_marks_the_spot", BuiltInLootTables.SIMPLE_DUNGEON, 2);
    public static ResourceLocation STAMPTONG = registerAndInject("the_stamptong", BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY, 2);

    public static void init() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            List<StampData> stamps = injections.get(key);
            if (source.isBuiltin() && stamps != null) {
                tableBuilder.modifyPools(builder -> {
                    for (StampData data : stamps) {
                        builder.add(LootItem.lootTableItem(AllItems.STAMP).apply(
                                SetComponentsFunction.setComponent(AllComponents.STAMP, new StampComponent(data.texture))
                        ).setWeight(data.weight));
                    }
                });
            }
        });
    }

    private record StampData(ResourceLocation texture, int weight) {}
}
