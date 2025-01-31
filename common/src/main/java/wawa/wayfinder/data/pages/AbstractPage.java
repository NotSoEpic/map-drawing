package wawa.wayfinder.data.pages;

import wawa.wayfinder.data.PageManager;

/**
 * A 512x512 pixel/block region of map data
 */
public abstract class AbstractPage {
    // region coordinates
    public final int rx;
    public final int ry;

    protected PageManager manager;

    protected AbstractPage(int rx, int ry, PageManager manager) {
        this.rx = rx;
        this.ry = ry;

        this.manager = manager;
    }

    public int getGlobalX() {
        return rx * 512;
    }

    public int getGlobalY() {
        return ry * 512;
    }

    public int getGlobalXOffset() {
        return getGlobalX() + 512;
    }

    public int getGlobalYOffset() {
        return getGlobalY() + 512;
    }

    abstract public boolean isUnloaded();

    protected void close() {
    }
}
