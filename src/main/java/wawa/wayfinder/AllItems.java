package wawa.wayfinder;

import wawa.wayfinder.stampitem.StampItem;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class AllItems {
    public static Item STAMP = register(Wayfinder.id("stamp"), new StampItem(new Item.Properties()));

    private static  <T extends Item> T register(ResourceLocation id, T item) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void init() {}
}
