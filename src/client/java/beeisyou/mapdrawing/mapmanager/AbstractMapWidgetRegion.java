package beeisyou.mapdrawing.mapmanager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.nio.file.Path;

public abstract class AbstractMapWidgetRegion {
    private final Vector2i region;
    private final Identifier id;
    protected final MapRegions regions;
    private long lastRenderTime;
    public AbstractMapWidgetRegion(int rx, int rz, MapRegions regions) {
        region = new Vector2i(rx, rz);
        id = Identifier.of("mapmanager", String.format("%d_%d", rx, rz));
        this.regions = regions;
        lastRenderTime = MinecraftClient.getInstance().getRenderTime();
    }

    public int rx() {
        return region.x;
    }
    public int rz() {
        return region.y;
    }
    public Identifier id() {
        return id;
    }


    public boolean inBoundsRel(int x, int z) {
        return x >= 0 && x < 512 && z >= 0 && z < 512;
    }

    public boolean inBoundsAbs(int x, int z) {
        return inBoundsRel(x - region.x * 512, z - region.y * 512);
    }

    public boolean shouldBeRendered(MapWidget parent, Vector2d ul, Vector2d lr) {
        Vector2d ul_ = parent.worldToScreen(rx() * 512, rz() * 512, true).max(new Vector2d());
        Vector2d lr_ = parent.worldToScreen(rx() * 512 + 512, rz() * 512 + 512, true).min(new Vector2d(parent.getWidth(), parent.getHeight()));
        ul.set(ul_);
        lr.set(lr_);
        return ul.x <= parent.getWidth() && ul.y <= parent.getHeight() && lr.x >= 0 && lr.y >= 0;
    }

    public boolean shouldBeRendered(MapWidget parent) {
        return shouldBeRendered(parent, new Vector2d(), new Vector2d());
    }

    public void render(DrawContext context, MapWidget parent) {
        lastRenderTime = Util.getMeasuringTimeMs();
    }

    public long getLastRenderTime() {
        return lastRenderTime;
    }

    public boolean putPixelWorld(int x, int z, int color, boolean highlight) {
        return putPixelRelative(x - rx() * 512, z - rz() * 512, color, highlight);
    }

    public abstract boolean putPixelRelative(int x, int z, int color, boolean highlight);
    public abstract void clear();
    public Path getPath() {
        return MinecraftClient.getInstance().getLevelStorage().getSavesDirectory()
                .resolve("pages").resolve(String.format("%s.png", id().getPath()));
    }
    public abstract void save();
}
