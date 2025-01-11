package beeisyou.mapdrawing.datagen;

import beeisyou.mapdrawing.AllItems;
import beeisyou.mapdrawing.MapDrawing;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class LangGenerator extends FabricLanguageProvider {
    protected LangGenerator(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(AllItems.STAMP, "Stamp");

        stamp(translationBuilder, Identifier.ofVanilla("missingno"), "Unusual Pattern");
        stamp(translationBuilder, MapDrawing.id("x_marks_the_spot"), "X Marks the Spot");
    }

    private void stamp(TranslationBuilder translationBuilder, Identifier stamp, String value) {
        translationBuilder.add(stamp.toTranslationKey("stamp"), value);
    }
}
