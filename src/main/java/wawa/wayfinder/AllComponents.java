package wawa.wayfinder;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import wawa.wayfinder.stampitem.StampComponent;

public class AllComponents {
    public static final DataComponentType<StampComponent> STAMP = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Wayfinder.id("stamp"),
            new DataComponentType.Builder<StampComponent>().persistent(StampComponent.CODEC).build()
    );

    public static void init() {}
}
