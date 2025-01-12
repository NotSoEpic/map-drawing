package wawa.wayfinder.datagen;

import wawa.wayfinder.AllItems;
import wawa.wayfinder.Wayfinder;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import java.util.concurrent.CompletableFuture;

public class LangGenerator extends FabricLanguageProvider {
    protected LangGenerator(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(AllItems.STAMP, "Stamp");

        stamp(translationBuilder, ResourceLocation.withDefaultNamespace("missingno"), "Unusual Pattern");
        stamp(translationBuilder, Wayfinder.id("x_marks_the_spot"), "X Marks the Spot");
        stamp(translationBuilder, Wayfinder.id("the_stamptong"), "The Legally Distinct Stamptong");
    }

    private void stamp(TranslationBuilder translationBuilder, ResourceLocation stamp, String value) {
        translationBuilder.add(stamp.toLanguageKey("stamp"), value);
    }
}
