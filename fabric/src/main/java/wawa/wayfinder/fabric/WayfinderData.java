package wawa.wayfinder.fabric;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import wawa.wayfinder.WayfinderClient;

public class WayfinderData implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        WayfinderClient.LOGGER.info("Running Wayfinder datagen");

        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
    }
}
