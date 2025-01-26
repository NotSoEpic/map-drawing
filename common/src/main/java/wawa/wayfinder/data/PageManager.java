package wawa.wayfinder.data;

import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles loading, getting, modifying, and saving {@link AbstractPage} instances
 */
public class PageManager {
    private Map<Vector2i, AbstractPage> pages = new HashMap<>();

    public AbstractPage getOrLoadRegion(int rx, int ry) {
        return pages.computeIfAbsent(new Vector2i(rx, ry), v -> new LoadedPage(v.x, v.y));
    }

    /**
     * Absolute world coordinates
     */
    public void putPixel(int x, int y, int RGBA) {
        int rx = Math.floorDiv(x, 512);
        int ry = Math.floorDiv(y, 512);
        getOrLoadRegion(rx, ry).putPixel(x - rx * 512, y - ry * 512, RGBA);
    }
}
