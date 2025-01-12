package wawa.wayfinder.mapmanager;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.nio.file.Path;

public abstract class AbstractMapWidgetRegion implements AutoCloseable {
    private final Vector2i region;
    private final ResourceLocation id;
    protected final MapRegions regions;
    private long lastRenderTime;
    public AbstractMapWidgetRegion(int rx, int rz, MapRegions regions) {
        region = new Vector2i(rx, rz);
        id = ResourceLocation.fromNamespaceAndPath("mapmanager", String.format("%d_%d", rx, rz));
        this.regions = regions;
        lastRenderTime = Minecraft.getInstance().getFrameTimeNs();
    }

    public int rx() {
        return region.x;
    }
    public int rz() {
        return region.y;
    }
    public ResourceLocation id() {
        return id;
    }


    public boolean inBoundsRel(int x, int z) {
        return x >= 0 && x < 512 && z >= 0 && z < 512;
    }

    public boolean inBoundsAbs(int x, int z) {
        return inBoundsRel(x - region.x * 512, z - region.y * 512);
    }

    public boolean shouldBeRendered(MapWidget parent) {
        Vector2d ul = parent.worldToScreen(rx() * 512, rz() * 512, true).max(new Vector2d());
        Vector2d lr = parent.worldToScreen(rx() * 512 + 512, rz() * 512 + 512, true).min(new Vector2d(parent.getWidth(), parent.getHeight()));
        return ul.x <= parent.getWidth() && ul.y <= parent.getHeight() && lr.x >= 0 && lr.y >= 0;
    }

    public void render(GuiGraphics context, MapWidget parent) {
        lastRenderTime = Util.getMillis();
    }

    public long getLastRenderTime() {
        return lastRenderTime;
    }

    public boolean putPixelWorld(int x, int z, int color) {
        return putPixelRelative(x - rx() * 512, z - rz() * 512, color);
    }

    public abstract boolean putPixelRelative(int x, int z, int color);

    public int getPixelWorld(int x, int z) {
        return getPixelRelative(x - rx() * 512, z - rz() * 512);
    }

    public abstract int getPixelRelative(int x, int z);

    public Path getPath(Path regionFile) {
        if (regionFile == null)
            return null;
        return regionFile.resolve(String.format("%s.png", id().getPath()));
    }

    public void save(Path regionPath, boolean close) {};

    public void reloadFromHistory() {};
    public void clearHistory() {};
    public abstract boolean hasHistory();
}
