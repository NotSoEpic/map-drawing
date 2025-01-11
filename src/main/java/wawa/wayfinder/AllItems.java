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
    public static Item STAMP = register(Wayfinder.id("stamp"), StampItem::new, new Item.Properties());

    private static  <T extends Item> T register(ResourceLocation id, Function<Item.Properties, T> factory, Item.Properties settings) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(settings.setId(key)));
    }

    public static void init() {}
}
