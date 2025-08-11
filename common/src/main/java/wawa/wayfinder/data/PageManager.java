package wawa.wayfinder.data;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.joml.Vector2i;
import wawa.wayfinder.data.history.OperationHistory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;

/**
 * Handles loading, getting, modifying, and saving {@link AbstractPage} instances
 */
public class PageManager {
    public PageIO pageIO;
    private final Map<Vector2i, AbstractPage> pages = new HashMap<>();

    private int emptyCount = 0;
    private int loadedCount = 0;

    private SnapshotState state = SnapshotState.IDLE;
    private final Stack<OperationHistory> pastHistories = new Stack<>();
    private OperationHistory currentHistory;

    public enum SnapshotState {
        IDLE, SNAPSHOTTING
    }

    /**
     * @param rx region x coordinate
     * @param ry region y coordinate
     * @return
     */
    public AbstractPage getOrCreatePage(final int rx, final int ry) {
        return this.pages.computeIfAbsent(new Vector2i(rx, ry), v -> {
            final EmptyPage page = new EmptyPage(v.x, v.y, this, this.pageIO);
            this.deltaCount(page, 1);
            return page;
        });
    }

    private void deltaCount(final AbstractPage page, final int delta) {
        if (page instanceof EmptyPage) {
            this.emptyCount += delta;
        } else if (page instanceof Page) {
            this.loadedCount += delta;
        }
    }

    public String getDebugCount() {
        return (this.emptyCount + this.loadedCount) + " (" + this.emptyCount + " / " + this.loadedCount + ")";
    }

    public void startSnapshot() {
        if (this.state != SnapshotState.SNAPSHOTTING) {
            this.state = SnapshotState.SNAPSHOTTING;
            this.currentHistory = new OperationHistory(new HashMap<>());
        }
    }

    public void endSnapshot() {
        if (this.state != SnapshotState.IDLE && this.currentHistory != null) {
            this.state = SnapshotState.IDLE;
            this.pastHistories.push(this.currentHistory);
        }
    }

    public void undoChanges() {
        if (!this.pastHistories.empty() && this.state == SnapshotState.IDLE) { //make sure we can't undo while we are currently modifying pages) {
            final OperationHistory recentHistory = this.pastHistories.pop();

            for (final Map.Entry<Vector2i, NativeImage> entry : recentHistory.pagesModified().entrySet()) {
                final AbstractPage page = this.getOrCreatePage(entry.getKey().x, entry.getKey().y);
                page.unboChanges(entry.getValue());
            }

            recentHistory.pagesModified().clear();
        }
    }

    /**
     * Absolute world coordinates
     */
    public void putPixel(final int x, final int y, final int RGBA) {
        final int rx = Math.floorDiv(x, 512);
        final int ry = Math.floorDiv(y, 512);


        final AbstractPage newPage = this.getOrCreatePage(rx, ry);
        if (newPage instanceof final EmptyPage ep && ep.isLoading()) { //don't try to take snapshots of pages that are still loading
            return;
        }

        if (this.state == SnapshotState.SNAPSHOTTING) {
            final Map<Vector2i, NativeImage> history = this.currentHistory.pagesModified();

            final Vector2i key = new Vector2i(rx, ry);
            if (history.get(key) == null) {
                final NativeImage image = new NativeImage(512, 512, true);
                final NativeImage pageImg = newPage.getImage();
                if (pageImg != null) {
                    image.copyFrom(pageImg);
                }

                history.put(key, image);
            }
        }

        newPage.setPixel(x - rx * 512, y - ry * 512, RGBA);
    }

    public int getPixel(final int x, final int y) {
        final int rx = Math.floorDiv(x, 512);
        final int ry = Math.floorDiv(y, 512);
        return this.getOrCreatePage(rx, ry).getPixel(x - rx * 512, y - ry * 512);
    }

    public void putSquare(final int x, final int y, final int RGBA, final int r) {
        for (int i = -r + x; i <= r + x; i++) {
            for (int j = -r + y; j <= r + y; j++) {
                this.putPixel(i, j, RGBA);
            }
        }
    }

    public void putConditionalSquare(final int x, final int y, final int RGBA, final int r, final Predicate<Integer> shouldReplace) {
        for (int i = -r + x; i <= r + x; i++) {
            for (int j = -r + y; j <= r + y; j++) {
                if (shouldReplace.test(this.getPixel(i, j))) {
                    this.putPixel(i, j, RGBA);
                }
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

    private int cleanupTimer = 0;

    public void tick() {
        final long rendertime = Util.getMillis();
        if (--this.cleanupTimer < 0) {
            this.cleanupTimer = 20 * 10;
            final Iterator<AbstractPage> it = this.pages.values().iterator();
            while (it.hasNext()) {
                final AbstractPage page = it.next();
                if (rendertime - page.getLastRendertime() > 1000 * 20) {
                    page.save(this.pageIO, true);
                    it.remove();
                    this.deltaCount(page, -1);

                    continue;
                }

                if (page instanceof final EmptyPage ep && ep.attemptedUndo && !ep.isLoading()) {
                    ep.unboChanges(ep.undoImage);
                }
            }
        }
    }

    public void save(final boolean close) {
        for (final AbstractPage page : this.pages.values()) {
            page.save(this.pageIO, close);
        }
    }

    public void saveAndClear() {
        this.save(true);
        this.pages.clear();
        this.emptyCount = 0;
        this.loadedCount = 0;

        for (final OperationHistory history : this.pastHistories) {
            for (final NativeImage value : history.pagesModified().values()) {
                value.close();
            }

            history.pagesModified().clear();
        }

        this.pastHistories.clear();
    }
}
