package wawa.wayfinder.data;

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
    // block coordinates
    public final int left;
    public final int top;
    public final int right;
    public final int bottom;
    private long lastRendertime = 0;

    protected AbstractPage(int rx, int ry) {
        this.rx = rx;
        this.ry = ry;
        this.left = rx * 512;
        this.top = ry * 512;
        this.right = rx * 512 + 512;
        this.bottom = ry * 512 + 512;
    }

    /**
     * @param x relative x coordinate (0-511, add left to get absolute x)
     * @param y relative y coordinate (0-511, add top to get absolute y)
     * @return instance to replace this
     */
    public abstract @Nullable AbstractPage putPixel(int x, int y, int RGBA);

    public long getLastRendertime() {
        return lastRendertime;
    }

    public void render(GuiGraphics guiGraphics) {
        lastRendertime = Util.getMillis();
    }

    public void save(PageIO pageIO, boolean close) {}

    protected void close() {}
}
