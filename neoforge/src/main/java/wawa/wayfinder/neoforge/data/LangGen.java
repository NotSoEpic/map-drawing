package wawa.wayfinder.neoforge.data;

import net.minecraft.client.KeyMapping;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.common.util.Lazy;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.neoforge.input.NeoKeyMappings;

public class LangGen extends LanguageProvider {
    public LangGen(final PackOutput output) {
        super(output, WayfinderClient.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add("key.categories.wayfinder", "Wayfinder");
        this.add(NeoKeyMappings.OPEN_MAP, "Open Map");
        this.add(NeoKeyMappings.SWAP, "Swap to Previous Tool");
        this.add(NeoKeyMappings.UNDO, "Undo");
        this.add(NeoKeyMappings.REDO, "Redo");
        this.add(NeoKeyMappings.PENCIL, "Pick Pencil");
        this.add(NeoKeyMappings.BRUSH, "Pick BrushWidget");
    }

    private void add(final Lazy<KeyMapping> key, final String value) {
        this.add(key.get().getName(), value);
    }
}
