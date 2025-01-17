package wawa.wayfinder.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import wawa.wayfinder.Wayfinder;

import java.util.concurrent.CompletableFuture;

public class StampGroupGenerator extends StampGroupProvider {
    public StampGroupGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generateGroups(GroupBuilder builder) {
        builder.add(Wayfinder.id("silly"), Wayfinder.MOD_ID, "missingstamp", "x_marks_the_spot");
        builder.add(Wayfinder.id("silly2"), Wayfinder.MOD_ID, "the_stamptong", "x_marks_the_spot");
    }
}
