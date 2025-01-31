package wawa.wayfinder.data;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.pages.AbstractPage;
import wawa.wayfinder.data.pages.LoadedPage;
import wawa.wayfinder.data.pages.UnloadedPage;

import java.util.*;

/**
 * Handles loading, getting, modifying, and saving {@link AbstractPage} instances
 */
public class PageManager {
    public PageIO pageIO;
    private final Map<Vector2i, AbstractPage> pages = new HashMap<>();

    private int emptyCount = 0;
    private int loadedCount = 0;

    private int cleanupTimer = 0;

    @Nullable
    public LoadedPage getOrCreateRegion(final int rx, final int ry) {
        final AbstractPage page = this.pages.get(new Vector2i(rx, ry));

        LoadedPage loaded = null;
        if (page == null) {
            loaded = new UnloadedPage(rx, ry, this).attemptToLoad();
        } else if (page instanceof final UnloadedPage up) {
            loaded = up.attemptToLoad();
        } else {
            return (LoadedPage) page;
        }

        if (loaded.failedToLoad) {
            WayfinderClient.LOGGER.error("Unable to load WayFinder Page for {} {}", rx, ry);
            return null;
        }

        this.pages.put(new Vector2i(loaded.rx, loaded.ry), loaded);
        return loaded;
    }

    private void deltaCount(final AbstractPage page, final int delta) {
/*        if (page instanceof EmptyPage) {
            emptyCount += delta;
        } else if (page instanceof LoadedPage) {
            loadedCount += delta;
        }*/
    }

    public String getDebugCount() {
        return (this.emptyCount + this.loadedCount) + " (" + this.emptyCount + " / " + this.loadedCount + ")";
    }

    /**
     * Absolute world coordinates
     */
    public void putPixel(final int x, final int y, final int RGBA) {
        final int rx = Math.floorDiv(x, 512);
        final int ry = Math.floorDiv(y, 512);

        final LoadedPage newPage = this.getOrCreateRegion(rx, ry);
        if (newPage != null) {
            newPage.setPixel(x - rx * 512, y - ry * 512, RGBA);
            this.replacePage(rx, ry, newPage);
        }
    }

    public int getPixel(final int x, final int y) {
        final int rx = Math.floorDiv(x, 512);
        final int ry = Math.floorDiv(y, 512);

        final LoadedPage page = this.getOrCreateRegion(rx, ry);
        if (page != null) {
            return page.getPixel(x - rx * 512, y - ry * 512);
        } else {
            return 0;
        }
    }

    public void putSquare(final int x, final int y, final int RGBA, final int r) {
        for (int i = -r + x; i <= r + x; i++) {
            for (int j = -r + y; j <= r + y; j++) {
                this.putPixel(i, j, RGBA);
            }
        }
    }

    public void replacePage(final int rx, final int ry, final AbstractPage replacement) {
        this.deltaCount(this.pages.get(new Vector2i(rx, ry)), -1);
        this.pages.put(new Vector2i(rx, ry), replacement);
        this.deltaCount(replacement, 1);
    }

    public void reloadPageIO(final Level level, final Minecraft client) {
        this.saveAndClear();
        this.pageIO = new PageIO(level, client);
    }

    public void tick() {
        final long rendertime = Util.getMillis();

        if (--this.cleanupTimer < 0) {
            this.cleanupTimer = 20 * 10;

            final Map<Vector2i, AbstractPage> newPageMap = new HashMap<>();

            final Iterator<AbstractPage> iter = this.pages.values().iterator();
            while (iter.hasNext()) {
                final AbstractPage page = iter.next();
                if (page instanceof final LoadedPage lp && rendertime - lp.lastRenderedTime > 10 * 20) {
                    final UnloadedPage newPage = lp.unloadPage(false);
                    newPageMap.put(new Vector2i(newPage.rx, newPage.ry), newPage);

                    iter.remove();
                }
            }

            this.pages.putAll(newPageMap);
        }
    }

    public void unloadAllPages(final boolean close) {
        for (final AbstractPage page : this.pages.values()) {
            if (page instanceof final LoadedPage lp) {
                lp.unloadPage(close);
            }
        }
    }

    public void saveAndClear() {
        this.unloadAllPages(true);
        this.pages.clear();
        this.emptyCount = 0;
        this.loadedCount = 0;
    }
}
