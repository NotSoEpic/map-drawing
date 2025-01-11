package wawa.wayfinder;

import wawa.wayfinder.stampitem.StampComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class AllComponents {
    public static final ComponentType<StampComponent> STAMP = Registry.register(Registries.DATA_COMPONENT_TYPE, Wayfinder.id("stamp"),
            new ComponentType.Builder<StampComponent>().codec(StampComponent.CODEC).packetCodec(StampComponent.PACKET_CODEC).build()
    );

    public static void init() {}
}
