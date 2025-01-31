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

    protected AbstractPage(int rx, int ry) {
        this.rx = rx;
        this.ry = ry;
    }

    public int left() {
        return rx * 512;
    }
    public int top() {
        return ry * 512;
    }
    public int right() {
        return (rx + 1) * 512;
    }
    public int bottom() {
        return (ry + 1) * 512;
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
    public int getPixel(int x, int y) {
        return 0;
    }

    public long getLastRendertime() {
        return lastRendertime;
    }

    public void render(GuiGraphics guiGraphics, int xOff, int yOff) {
        lastRendertime = Util.getMillis();
    }

    public void save(PageIO pageIO, boolean close) {}

    protected void close() {}
}
