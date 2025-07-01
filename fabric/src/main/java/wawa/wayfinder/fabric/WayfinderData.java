package wawa.wayfinder.fabric;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import wawa.wayfinder.WayfinderClient;

public class WayfinderData implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(final FabricDataGenerator fabricDataGenerator) {
        WayfinderClient.LOGGER.info("Running Wayfinder datagen");

        final FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
    }
}
