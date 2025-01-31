package wawa.wayfinder.data;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Handles loading, getting, modifying, and saving {@link AbstractPage} instances
 */
public class PageManager {
    public PageIO pageIO;
    private Map<Vector2i, AbstractPage> pages = new HashMap<>();

    private int emptyCount = 0;
    private int loadedCount = 0;

    /**
     *
     * @param rx region x coordinate
     * @param ry region y coordinate
     * @return
     */
    public AbstractPage getOrCreatePage(int rx, int ry) {
        return pages.computeIfAbsent(new Vector2i(rx, ry), v -> {
            EmptyPage page = new EmptyPage(v.x, v.y, this, pageIO);
            deltaCount(page, 1);
            return page;
        });
    }

    private void deltaCount(AbstractPage page, int delta) {
        if (page instanceof EmptyPage) {
            emptyCount += delta;
        } else if (page instanceof Page) {
            loadedCount += delta;
        }
    }

    public String getDebugCount() {
        return (emptyCount + loadedCount) + " (" + emptyCount + " / " + loadedCount + ")";
    }

    /**
     * Absolute world coordinates
     */
    public void putPixel(int x, int y, int RGBA) {
        int rx = Math.floorDiv(x, 512);
        int ry = Math.floorDiv(y, 512);
        getOrCreatePage(rx, ry).setPixel(x - rx * 512, y - ry * 512, RGBA);
    }

    public int getPixel(int x, int y) {
        int rx = Math.floorDiv(x, 512);
        int ry = Math.floorDiv(y, 512);
        return getOrCreatePage(rx, ry).getPixel(x - rx * 512, y - ry * 512);
    }

    public void putSquare(int x, int y, int RGBA, int r) {
        for (int i = -r + x; i <= r + x; i++) {
            for (int j = -r + y; j <= r + y; j++) {
                putPixel(i, j, RGBA);
            }
        }
    }

    public void replacePage(int rx, int ry, AbstractPage replacement) {
        deltaCount(pages.get(new Vector2i(rx, ry)), -1);
        pages.put(new Vector2i(rx, ry), replacement);
        deltaCount(replacement, 1);
    }

    public void reloadPageIO(Level level, Minecraft client) {
        saveAndClear();
        pageIO = new PageIO(level, client);
    }

    private int cleanupTimer = 0;
    public void tick() {
        long rendertime = Util.getMillis();
        if (--cleanupTimer < 0) {
            cleanupTimer = 20 * 10;
            Iterator<AbstractPage> it = pages.values().iterator();
            while (it.hasNext()) {
                AbstractPage page = it.next();
                if (rendertime - page.getLastRendertime() > 1000 * 20) {
                    page.save(pageIO, true);
                    it.remove();
                    deltaCount(page, -1);
                }
            }
        }
    }

    public void save(boolean close) {
        for (AbstractPage page : pages.values()) {
            page.save(pageIO, close);
        }
    }

    public void saveAndClear() {
        save(true);
        pages.clear();
        emptyCount = 0;
        loadedCount = 0;
    }
}
