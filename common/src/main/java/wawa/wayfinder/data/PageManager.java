package wawa.wayfinder.data;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.history.OperationHistory;
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

    Stack<OperationHistory> pastHistories = new Stack<>();
//    Stack<OperationHistory> futureHistories = new Stack<>();

    OperationHistory currentHistory = null;

    private int emptyCount = 0;
    private int loadedCount = 0;

    private int cleanupTimer = 0;

    private SnapShotState state = SnapShotState.STATIC;

    /**
     * SNAPSHOTTING = saves all operations into an OperationHistory, and pushes that onto an operation stack
     */
    public enum SnapShotState {
        SNAPSHOTTING, STATIC
    }

    @Nullable
    public LoadedPage getOrCreateRegion(final int rx, final int ry) {
        final AbstractPage page = this.pages.get(new Vector2i(rx, ry));

        final LoadedPage loaded;
        if (page == null) {
            loaded = new UnloadedPage(rx, ry, this).attemptToLoad();
        } else if (page instanceof final UnloadedPage up) {
            loaded = up.attemptToLoad();
        } else {
            return (LoadedPage) page;
        }

        if (loaded.isFailedToLoad()) {
            WayfinderClient.LOGGER.error("Unable to load WayFinder Page for {} {}", rx, ry);
            return null;
        }

        this.pages.put(new Vector2i(loaded.rx, loaded.ry), loaded);
        return loaded;
    }

    public String getDebugCount() {
        return (this.emptyCount + this.loadedCount) + " (" + this.emptyCount + " / " + this.loadedCount + ")";
    }

    public void startSnapshot() {
        this.state = SnapShotState.SNAPSHOTTING;

        if (this.currentHistory == null) {
            this.currentHistory = new OperationHistory(new HashMap<>());
        }
    }

    public void endSnapshot() {
        this.state = SnapShotState.STATIC;

        if (this.currentHistory != null) {
            this.pastHistories.push(this.currentHistory);
            this.currentHistory = null;
        }
    }

    /**
     * Absolute world coordinates
     */
    public void putPixel(final int x, final int y, final int RGBA) {
        final int rx = Math.floorDiv(x, 512);
        final int ry = Math.floorDiv(y, 512);

        final LoadedPage newPage = this.getOrCreateRegion(rx, ry);
        if (newPage != null) {
            newPage.createImageIfEmpty();

            if (this.state == SnapShotState.SNAPSHOTTING) {
                final Vector2i pos = new Vector2i(rx, ry);
                final NativeImage image = this.currentHistory.pagesModified().get(pos);
                if (image == null) {
                    final NativeImage copyImage = new NativeImage(512, 512, false);
                    copyImage.copyFrom(newPage.getAssociatedImage().getPixels());

                    this.currentHistory.pagesModified().put(pos, copyImage);
                }
            }

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
        this.pages.put(new Vector2i(rx, ry), replacement);
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
                if (page instanceof final LoadedPage lp && rendertime - lp.getLastRenderedTime() > 10 * 20) {
                    final UnloadedPage newPage = lp.unloadPage(false);
                    newPageMap.put(new Vector2i(newPage.rx, newPage.ry), newPage);

                    iter.remove();
                }
            }

//            this.futureHistories.clear();
            this.pages.putAll(newPageMap);
        }
    }

    public void undoChanges() {
        if (!this.pastHistories.empty()) {
            final OperationHistory history = this.pastHistories.pop();
//            this.futureHistories.push(history);

            for (final Map.Entry<Vector2i, NativeImage> entry : history.pagesModified().entrySet()) {
                final Vector2i pos = entry.getKey();
                final LoadedPage page = this.getOrCreateRegion(pos.x, pos.y);
                if (page == null) {
                    continue;
                }

                final NativeImage image = entry.getValue();
                page.setImageExternally(image);
            }

            history.pagesModified().clear();
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

        this.pastHistories.clear();
    }
}
