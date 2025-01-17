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

        stampGroup(translationBuilder, Wayfinder.id("silly"), "Silly");
        stampGroup(translationBuilder, Wayfinder.id("silly2"), "Silly2");

        stamp(translationBuilder, Wayfinder.id("missingstamp"), "Unusual Pattern");
        stamp(translationBuilder, BuiltInStamps.X, "X Marks the Spot");
        stamp(translationBuilder, BuiltInStamps.STAMPTONG, "The Legally Distinct Stamptong");

        translationBuilder.add(MapBindings.OPEN_MAP.getCategory(), "Wayfinder Map Controls");
        translationBuilder.add(MapBindings.OPEN_MAP.getName(), "Open Map");
        translationBuilder.add(MapBindings.UNDO.getName(), "Undo (w/ ctrl)");
        translationBuilder.add(MapBindings.SWAP_TOOL.getName(), "Swap to Previous Tool");
        translationBuilder.add(MapBindings.PENCIL.getName(), "Pencil Tool");
        translationBuilder.add(MapBindings.BRUSH.getName(), "Brush Tool");
        translationBuilder.add(MapBindings.ERASER.getName(), "Eraser Tool");
        translationBuilder.add(MapBindings.RULER.getName(), "Ruler Tool");
    }

    private void stamp(TranslationBuilder translationBuilder, ResourceLocation stamp, String value) {
        translationBuilder.add(stamp.toLanguageKey("stamp"), value);
    }

    private void stampGroup(TranslationBuilder translationBuilder, ResourceLocation group, String value) {
        translationBuilder.add(group.toLanguageKey("stamp_group"), value);
    }
}
