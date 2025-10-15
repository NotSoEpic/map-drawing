package wawa.mapwright.data;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;
import wawa.mapwright.MapwrightClient;

/**
 * A MapwrightClient.chunkSzexMapwrightClient.chunkSze pixel/block region of map data
 */
public abstract class AbstractPage {
    // region coordinates
    public final int rx;
    public final int ry;
    private long lastRendertime = 0;

    protected AbstractPage(final int rx, final int ry) {
        this.rx = rx;
        this.ry = ry;
    }

    public int left() {
        return this.rx * MapwrightClient.CHUNK_SIZE;
    }
    public int top() {
        return this.ry * MapwrightClient.CHUNK_SIZE;
    }
    public int right() {
        return (this.rx + 1) * MapwrightClient.CHUNK_SIZE;
    }
    public int bottom() {
        return (this.ry + 1) * MapwrightClient.CHUNK_SIZE;
    }

    /**
     * @param x relative x coordinate (0-511)
     * @param y relative y coordinate (0-511)
     */
    public abstract void setPixel(int x, int y, int RGBA);

    @Nullable
    public abstract NativeImage getImage();

    /**
     * @param replacement Reference data. Must be closed to prevent a memory leak.
     * @return previous image data
     */
    public abstract NativeImage unboChanges(NativeImage replacement); // unbo

    /**
     * @param x relative x coordinate (0-511)
     * @param y relative y coordinate (0-511)
     */
    public int getPixel(final int x, final int y) {
        return 0;
    }

    public long getLastRendertime() {
        return this.lastRendertime;
    }

    public void render(final GuiGraphics guiGraphics, final double xOff, final double yOff) {
        this.lastRendertime = Util.getMillis();
    }

    public void save(final PageIO pageIO, final boolean close) {}

    protected void close() {}
}
