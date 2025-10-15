package wawa.mapwright.data;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.joml.Vector2dc;
import org.joml.Vector2i;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.NativeImageTracker;
import wawa.mapwright.data.history.OperationHistory;

import java.util.*;
import java.util.function.Predicate;

/**
 * Handles loading, getting, modifying, and saving {@link AbstractPage} instances
 */
public class PageManager {
    public PageIO pageIO;
    private final Map<Vector2i, AbstractPage> pages = new HashMap<>();
    private final Map<Pin.Type, Pin> pins = new HashMap<>();
    private final SpyglassPins spyglassPins = new SpyglassPins();

    private int emptyCount = 0;
    private int loadedCount = 0;

    private SnapshotState state = SnapshotState.IDLE;
    private static final int MAX_HISTORY = 16;
    private final Stack<OperationHistory> pastHistories = new Stack<>();
    private final Stack<OperationHistory> futureHistories = new Stack<>();
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

            this.futureHistories.forEach(OperationHistory::clear);
            this.futureHistories.clear();
        }
    }

    public void endSnapshot() {
        if (this.state != SnapshotState.IDLE && this.currentHistory != null) {
            this.state = SnapshotState.IDLE;
            this.pastHistories.push(this.currentHistory);
            while (this.pastHistories.size() > MAX_HISTORY) {
                this.pastHistories.removeFirst().clear();
            }
        }
    }

    public void undoChanges() {
        if (!this.pastHistories.empty() && this.state == SnapshotState.IDLE) { //make sure we can't undo while we are currently modifying pages)
            final OperationHistory recentHistory = this.pastHistories.pop();
            final OperationHistory redoHistory = new OperationHistory(new HashMap<>());

            for (final Map.Entry<Vector2i, NativeImage> entry : recentHistory.pagesModified().entrySet()) {
                final AbstractPage page = this.getOrCreatePage(entry.getKey().x, entry.getKey().y);
                 redoHistory.pagesModified().put(entry.getKey(), page.unboChanges(entry.getValue()));
            }

            this.futureHistories.push(redoHistory);

            recentHistory.clear();
        }
    }

    public void redoChanges() {
        if (!this.futureHistories.empty() && this.state == SnapshotState.IDLE) {
            final OperationHistory recentHistory = this.futureHistories.pop();
            final OperationHistory undoHistory = new OperationHistory(new HashMap<>());

            for (final Map.Entry<Vector2i, NativeImage> entry : recentHistory.pagesModified().entrySet()) {
                final AbstractPage page = this.getOrCreatePage(entry.getKey().x, entry.getKey().y);
                undoHistory.pagesModified().put(entry.getKey(), page.unboChanges(entry.getValue()));
            }

            this.pastHistories.push(undoHistory);

            recentHistory.clear();
        }
    }

    public void snapshotPage(AbstractPage page) {
        if (this.state == SnapshotState.SNAPSHOTTING) {
            final Map<Vector2i, NativeImage> history = this.currentHistory.pagesModified();

            final Vector2i key = new Vector2i(page.rx, page.ry);
            if (history.get(key) == null) {
                final NativeImage image = NativeImageTracker.newImage(MapwrightClient.CHUNK_SIZE, MapwrightClient.CHUNK_SIZE, true);
                final NativeImage pageImg = page.getImage();
                if (pageImg != null) {
                    image.copyFrom(pageImg);
                }

                history.put(key, image);
            }
        }
    }

    /**
     * Absolute world coordinates
     */
    public void putPixel(final int x, final int y, final int RGBA) {
        final int rx = Math.floorDiv(x, MapwrightClient.CHUNK_SIZE);
        final int ry = Math.floorDiv(y, MapwrightClient.CHUNK_SIZE);


        final AbstractPage newPage = this.getOrCreatePage(rx, ry);
        if (newPage instanceof final EmptyPage ep && ep.isLoading()) { //don't try to take snapshots of pages that are still loading
            return;
        }

        this.snapshotPage(newPage);

        newPage.setPixel(x - rx * MapwrightClient.CHUNK_SIZE, y - ry * MapwrightClient.CHUNK_SIZE, RGBA);
    }

    public int getPixelARGB(final int x, final int y) {
        final int rx = Math.floorDiv(x, MapwrightClient.CHUNK_SIZE);
        final int ry = Math.floorDiv(y, MapwrightClient.CHUNK_SIZE);
        return this.getOrCreatePage(rx, ry).getPixel(x - rx * MapwrightClient.CHUNK_SIZE, y - ry * MapwrightClient.CHUNK_SIZE);
    }

    public void putSquare(final int x, final int y, final int RGBA, final int r) {
        for (int i = -r + x; i <= r + x; i++) {
            for (int j = -r + y; j <= r + y; j++) {
                this.putPixel(i, j, RGBA);
            }
        }
    }

    @FunctionalInterface
    public interface RegionGetter {
        void of(int dx, int dy, int value);
    }

    public void forEachInRegion(final int x, final int y, final int w, final int h, final RegionGetter forEach) {
        final int rx1 = Math.floorDiv(x, MapwrightClient.CHUNK_SIZE); // leftmost region
        final int ry1 = Math.floorDiv(y, MapwrightClient.CHUNK_SIZE); // upmost region
        final int rx2 = Math.floorDiv(x + w, MapwrightClient.CHUNK_SIZE); // rightmost region
        final int ry2 = Math.floorDiv(y + h, MapwrightClient.CHUNK_SIZE); // bottommost region
        for (int i = rx1; i <= rx2; i++) { // current region x
            for (int j = ry1; j <= ry2; j++) { // current region y
                final AbstractPage page = this.getOrCreatePage(i, j);
                final int dx1 = i == rx1 ? x - rx1 * MapwrightClient.CHUNK_SIZE : 0; // leftmost relative pixel in region (0-511)
                final int dy1 = j == ry1 ? y - ry1 * MapwrightClient.CHUNK_SIZE : 0; // topmost relative pixel in region (0-511)
                final int dx2 = i == rx2 ? x - rx2 * MapwrightClient.CHUNK_SIZE + w : MapwrightClient.CHUNK_SIZE; // rightmost relative pixel in region (1-MapwrightClient.chunkSze)
                final int dy2 = j == ry2 ? y - ry2 * MapwrightClient.CHUNK_SIZE + h : MapwrightClient.CHUNK_SIZE; // bottommost relative pixel in region (1-MapwrightClient.chunkSze)
                for (int k = dx1; k < dx2; k++) { // current relative pixel x
                    for (int l = dy1; l < dy2; l++) { // current relative pixel y
                        forEach.of(
                                k + i * MapwrightClient.CHUNK_SIZE - x,
                                l + j * MapwrightClient.CHUNK_SIZE - y,
                                page.getPixel(k, l)
                        );
                    }
                }
            }
        }
    }

    @FunctionalInterface
    public interface RegionMapping {
        int apply(int dx, int dy, int previousValue);
    }

    public void putRegion(final int x, final int y, final int w, final int h, final RegionMapping regionMapping) {
        startSnapshot();
        final int rx1 = Math.floorDiv(x, MapwrightClient.CHUNK_SIZE); // leftmost region
        final int ry1 = Math.floorDiv(y, MapwrightClient.CHUNK_SIZE); // upmost region
        final int rx2 = Math.floorDiv(x + w, MapwrightClient.CHUNK_SIZE); // rightmost region
        final int ry2 = Math.floorDiv(y + h, MapwrightClient.CHUNK_SIZE); // bottommost region
        for (int i = rx1; i <= rx2; i++) { // current region x
            for (int j = ry1; j <= ry2; j++) { // current region y
                final AbstractPage page = this.getOrCreatePage(i, j);
                this.snapshotPage(page);
                final int dx1 = i == rx1 ? x - rx1 * MapwrightClient.CHUNK_SIZE : 0; // leftmost relative pixel in region (0-511)
                final int dy1 = j == ry1 ? y - ry1 * MapwrightClient.CHUNK_SIZE : 0; // topmost relative pixel in region (0-511)
                final int dx2 = i == rx2 ? x - rx2 * MapwrightClient.CHUNK_SIZE + w : MapwrightClient.CHUNK_SIZE; // rightmost relative pixel in region (1-MapwrightClient.chunkSze)
                final int dy2 = j == ry2 ? y - ry2 * MapwrightClient.CHUNK_SIZE + h : MapwrightClient.CHUNK_SIZE; // bottommost relative pixel in region (1-MapwrightClient.chunkSze)
                for (int k = dx1; k < dx2; k++) { // current relative pixel x
                    for (int l = dy1; l < dy2; l++) { // current relative pixel y
                        page.setPixel(k, l, regionMapping.apply(
                                k + i * MapwrightClient.CHUNK_SIZE - x,
                                l + j * MapwrightClient.CHUNK_SIZE - y,
                                page.getPixel(k, l)
                        ));
                    }
                }
            }
        }
        endSnapshot();
    }

    public void putConditionalSquare(final int x, final int y, final int RGBA, final int r, final Predicate<Integer> shouldReplace) {
        for (int i = -r + x; i <= r + x; i++) {
            for (int j = -r + y; j <= r + y; j++) {
                if (shouldReplace.test(this.getPixelARGB(i, j))) {
                    this.putPixel(i, j, RGBA);
                }
            }
        }
    }

    public Collection<Pin> getPins() {
        return this.pins.values();
    }

    public void putPin(final Pin.Type type, final Vector2dc pos) {
        this.pins.computeIfAbsent(type, Pin::new).setPosition(pos);
    }

    public void removePin(final Pin.Type type) {
        this.pins.remove(type);
    }

    public SpyglassPins getSpyglassPins() {
        return this.spyglassPins;
    }

    public void replacePage(final int rx, final int ry, final AbstractPage replacement) {
        this.deltaCount(this.pages.get(new Vector2i(rx, ry)), -1);
        this.pages.put(new Vector2i(rx, ry), replacement);
        this.deltaCount(replacement, 1);
    }

    public void reloadPageIO(final Level level, final Minecraft client) {
        this.pageIO = new PageIO(level, client);
        this.pins.clear();
        this.pins.putAll(this.pageIO.readPins());
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
                    if (ep.getImage() != null) {
                        ep.redoImage.copyFrom(ep.getImage());
                    }
                    ep.unboChanges(ep.undoImage);
                }
            }
        }
        this.spyglassPins.tick();
    }

    public void save(final boolean close) {
        if (this.pageIO != null) {
            for (final AbstractPage page : this.pages.values()) {
                page.save(this.pageIO, close);
            }
            this.pageIO.savePins(this.pins);
        }
    }

    public void saveAndClear() {
        this.save(true);
        this.pages.clear();
        this.emptyCount = 0;
        this.loadedCount = 0;

        this.pastHistories.forEach(OperationHistory::clear);
        this.pastHistories.clear();
        this.futureHistories.forEach(OperationHistory::clear);
        this.futureHistories.clear();

        pageIO = null;
    }
}
