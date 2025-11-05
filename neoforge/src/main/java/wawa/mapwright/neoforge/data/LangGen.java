package wawa.mapwright.neoforge.data;

import net.minecraft.client.KeyMapping;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.common.util.Lazy;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.neoforge.input.NeoKeyMappings;

public class LangGen extends LanguageProvider {
    public LangGen(final PackOutput output) {
        super(output, MapwrightClient.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add("key.categories.mapwright", "Mapwright");
        this.add(NeoKeyMappings.OPEN_MAP, "Open Map");
        this.add(NeoKeyMappings.SWAP, "Swap to Previous Tool");
        this.add(NeoKeyMappings.UNDO, "Undo");
        this.add(NeoKeyMappings.REDO, "Redo");
        this.add(NeoKeyMappings.HAND, "Pick Hand");
        this.add(NeoKeyMappings.PEN, "Pick Pencil");
        this.add(NeoKeyMappings.BRUSH, "Pick Brush");
        this.add(NeoKeyMappings.ERASER, "Pick Eraser");

        this.add("mapwright.tool.pan", "Deselect Tool");
        this.add("mapwright.tool.pen", "Pen");
        this.add("mapwright.tool.brush", "Brush");
        this.add("mapwright.tool.eraser", "Eraser");
        this.add("mapwright.tool.pin", "Pin");
        this.add("mapwright.tool.copy", "Copy");
        this.add("mapwright.tool.stamp", "Stamp Bag");

        this.add("item.mapwright.spyglass.use_tooltip", "Attack while scoped in to place a temporary pin");
    }

    private void add(final Lazy<KeyMapping> key, final String value) {
        this.add(key.get().getName(), value);
    }
}
