package wawa.wayfinder.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles loading, getting, modifying, and saving {@link AbstractPage} instances
 */
public class PageManager {
    public PageIO pageIO;
    private Map<Vector2i, AbstractPage> pages = new HashMap<>();

    public AbstractPage getOrCreateRegion(int rx, int ry) {
        return pages.computeIfAbsent(new Vector2i(rx, ry), v -> new UnloadedPage(v.x, v.y, this));
    }

    /**
     * Absolute world coordinates
     */
    public void putPixel(int x, int y, int RGBA) {
        int rx = Math.floorDiv(x, 512);
        int ry = Math.floorDiv(y, 512);
        AbstractPage newPage = getOrCreateRegion(rx, ry).putPixel(x - rx * 512, y - ry * 512, RGBA);
        if (newPage != null) {
            replacePage(rx, ry, newPage);
        }
    }

    public void replacePage(int rx, int ry, AbstractPage replacement) {
        pages.put(new Vector2i(rx, ry), replacement);
    }

    public void reloadPageIO(ClientLevel level, Minecraft client) {
        pageIO = new PageIO(level, client);
    }

    public void save() {
        for (AbstractPage page : pages.values()) {
            page.save(pageIO);
        }
    }

    public void saveAndClear() {
        save();
        pages.clear();
    }
}
