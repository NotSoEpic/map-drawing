package beeisyou.mapdrawing;

import beeisyou.mapdrawing.stampitem.StampItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class AllItems {
    public static Item STAMP = register(MapDrawing.id("stamp"), StampItem::new, new Item.Settings());

    private static  <T extends Item> T register(Identifier id, Function<Item.Settings, T> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, key, factory.apply(settings.registryKey(key)));
    }

    public static void init() {}
}
