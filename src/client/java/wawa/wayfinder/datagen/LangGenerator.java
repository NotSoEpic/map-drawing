package wawa.wayfinder.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import wawa.wayfinder.AllItems;
import wawa.wayfinder.MapBindings;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.stampitem.BuiltInStamps;

import java.util.concurrent.CompletableFuture;

public class LangGenerator extends FabricLanguageProvider {
    protected LangGenerator(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(AllItems.STAMP, "Stamp");

        stamp(translationBuilder, Wayfinder.id("missingstamp"), "Unusual Pattern");
        stamp(translationBuilder, BuiltInStamps.X, "X Marks the Spot");
        stamp(translationBuilder, BuiltInStamps.STAMPTONG, "The Legally Distinct Stamptong");

        translationBuilder.add(MapBindings.openMap.getCategory(), "Wayfinder Map Controls");
        translationBuilder.add(MapBindings.openMap.getName(), "Open Map");
        translationBuilder.add(MapBindings.undo.getName(), "Undo (w/ ctrl)");
        translationBuilder.add(MapBindings.swap_tool.getName(), "Swap to Previous Tool");
        translationBuilder.add(MapBindings.pencil.getName(), "Pencil Tool");
        translationBuilder.add(MapBindings.brush.getName(), "Brush Tool");
        translationBuilder.add(MapBindings.eraser.getName(), "Eraser Tool");
        translationBuilder.add(MapBindings.ruler.getName(), "Ruler Tool");
    }

    private void stamp(TranslationBuilder translationBuilder, ResourceLocation stamp, String value) {
        translationBuilder.add(stamp.toLanguageKey("stamp"), value);
    }
}
