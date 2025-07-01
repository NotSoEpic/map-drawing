package wawa.wayfinder.data;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

/**
 * A 512x512 pixel/block region of map data
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
        return this.rx * 512;
    }
    public int top() {
        return this.ry * 512;
    }
    public int right() {
        return (this.rx + 1) * 512;
    }
    public int bottom() {
        return (this.ry + 1) * 512;
    }

    /**
     * @param x relative x coordinate (0-511)
     * @param y relative y coordinate (0-511)
     */
    public abstract void setPixel(int x, int y, int RGBA);

    @Nullable
    public abstract NativeImage getImage();

    public abstract void unboChanges(NativeImage replacement);

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

    public void render(final GuiGraphics guiGraphics, final int xOff, final int yOff) {
        this.lastRendertime = Util.getMillis();
    }

    public void save(final PageIO pageIO, final boolean close) {}

    protected void close() {}
}
