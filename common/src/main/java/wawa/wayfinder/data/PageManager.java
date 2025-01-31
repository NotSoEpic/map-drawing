package wawa.wayfinder.data;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.joml.Vector2i;
import org.lwjgl.system.linux.Stat;
import wawa.wayfinder.data.history.OperationHistory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Handles loading, getting, modifying, and saving {@link AbstractPage} instances
 */
public class PageManager {
    public PageIO pageIO;
    private Map<Vector2i, AbstractPage> pages = new HashMap<>();

    private int emptyCount = 0;
    private int loadedCount = 0;

    private SnapshotState state = SnapshotState.IDLE;
    private Stack<OperationHistory> pastHistories = new Stack<>();
    private OperationHistory currentHistory;

    public enum SnapshotState {
        IDLE, SNAPSHOTTING
    }

    /**
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

    public void startSnapshot() {
        if (state != SnapshotState.SNAPSHOTTING) {
            state = SnapshotState.SNAPSHOTTING;
            currentHistory = new OperationHistory(new HashMap<>());
        }
    }

    public void endSnapshot() {
        if (state != SnapshotState.IDLE && currentHistory != null) {
            state = SnapshotState.IDLE;
            pastHistories.push(currentHistory);
        }
    }

    public void undoChanges() {
        if (!pastHistories.empty() && state == SnapshotState.IDLE) { //make sure we can't undo while we are currently modifying pages) {
            OperationHistory recentHistory = pastHistories.pop();

            for (Map.Entry<Vector2i, NativeImage> entry : recentHistory.pagesModified().entrySet()) {
                AbstractPage page = getOrCreatePage(entry.getKey().x, entry.getKey().y);
                page.unboChanges(entry.getValue());
            }

            recentHistory.pagesModified().clear();
        }
    }

    /**
     * Absolute world coordinates
     */
    public void putPixel(int x, int y, int RGBA) {
        int rx = Math.floorDiv(x, 512);
        int ry = Math.floorDiv(y, 512);


        AbstractPage newPage = getOrCreatePage(rx, ry);
        if (newPage instanceof EmptyPage ep && ep.isLoading()) { //don't try to take snapshots of pages that are still loading
            return;
        }

        if (state == SnapshotState.SNAPSHOTTING) {
            Map<Vector2i, NativeImage> history = currentHistory.pagesModified();

            Vector2i key = new Vector2i(rx, ry);
            if (history.get(key) == null) {
                NativeImage image = new NativeImage(512, 512, false);
                NativeImage pageImg = newPage.getImage();
                if (pageImg != null) {
                    image.copyFrom(pageImg);
                }

                history.put(key, image);
            }
        }

        newPage.setPixel(x - rx * 512, y - ry * 512, RGBA);
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

                    continue;
                }

                if (page instanceof EmptyPage ep && ep.attemptedUndo && !ep.isLoading()) {
                    ep.unboChanges(ep.undoImage);
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

        for (OperationHistory history : pastHistories) {
            for (NativeImage value : history.pagesModified().values()) {
                value.close();
            }

            history.pagesModified().clear();
        }

        pastHistories.clear();
    }
}
