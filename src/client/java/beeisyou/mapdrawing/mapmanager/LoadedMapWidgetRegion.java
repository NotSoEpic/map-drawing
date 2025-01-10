package beeisyou.mapdrawing.mapmanager;

import beeisyou.mapdrawing.MapDrawing;
import beeisyou.mapdrawing.RenderHelper;
import beeisyou.mapdrawing.rendering.MapDrawingRenderLayers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Util;
import org.joml.Vector2d;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * A 512x512 section of the map that contains an image
 */
public class LoadedMapWidgetRegion extends AbstractMapWidgetRegion {
    public final NativeImageBackedTexture texture;
    private boolean dirtyVisual = false; // unuploaded changes
    private boolean dirtySave = false;
    private boolean removed = false;
    public LoadedMapWidgetRegion(int rx, int rz, MapRegions regions) {
        super(rx, rz, regions);
        texture = new NativeImageBackedTexture(512, 512, false); // each chunk region is 512x512 blocks, seems fitting
    }

    public void initToClear() {
        texture.getImage().apply(i -> 0); // reset to clear
        texture.upload();
        registerTexture();
    }

    public void registerTexture() {
        MinecraftClient.getInstance().getTextureManager().registerTexture(id(), texture);
    }

    @Override
    public void render(DrawContext context, MapWidget parent) {
        super.render(context, parent);

        Vector2d ul = new Vector2d();
        Vector2d lr = new Vector2d();
        if (!shouldBeRendered(parent, ul, lr))
            return;
        checkDirty();
        if (isRemoved()) {
            clearFile();
            regions.put(rx(), rz(), new UnloadedMapWidgetRegion(rx(), rz(), regions));
            return;
        }
        Vector2d uv = parent.worldToScreen(rx() * 512, rz() * 512, true).sub(ul).mul(-1);
        Vector2d wh = new Vector2d(lr).sub(ul);
        RenderHelper.drawTexture(context, MapDrawingRenderLayers::getPaletteSwap, id(),
                ul.x, ul.y,
                (float) uv.x, (float) uv.y,
                wh.x, wh.y,
                (int) (512 * parent.scale),(int) (512 * parent.scale));
        if (MinecraftClient.getInstance().getDebugHud().shouldShowDebugHud()) {
            RenderHelper.badDebugText(context, (int)ul.x + 2, (int)ul.y + 2, id().getPath());
        }
    }

    @Override
    public void save(Path regionPath) {
        if (!dirtySave)
            return;
        Path file = getPath(regionPath);
        Util.getIoWorkerExecutor().execute(() -> {
            try {
                Files.createDirectories(file.getParent());
                texture.getImage().writeTo(file);
            } catch (IOException e) {
                MapDrawing.LOGGER.warn("Failed to save region {} {} to {}\n{}", rx(), rz(), file, e);
            }
        });
    }

    private void clearFile() {
        Path file = getPath(regions.getRegionPath());
        if (file != null) {
            Util.getIoWorkerExecutor().execute(() -> {
                try {
                    Files.deleteIfExists(file);
                    dirtySave = false;
                } catch (IOException e) {
                    MapDrawing.LOGGER.warn("Failed to delete region {} {} of {}\n{}", rx(), rz(), file, e);
                }
            });
        } else {
            MapDrawing.LOGGER.warn("No path for map to clear");
        }
    }

    public boolean putPixelRelative(int x, int z, int color, boolean highlight) {
        if (!inBoundsRel(x, z))
            return false;
        if (!highlight || texture.getImage().getColorArgb(x - rx() * 512, z - rz() * 512) == 0) {
            texture.getImage().setColorArgb(x, z, color);
            dirtyVisual = true;
            dirtySave = true;
            return true;
        }
        return false;
    }

    public void checkDirty() {
        if (dirtyVisual) {
            if (Arrays.stream(texture.getImage().copyPixelsAbgr()).allMatch(i -> i == 0)) {
                removed = true;
            } else {
                texture.upload();
                dirtyVisual = false;
            }
        }
    }

    public boolean isRemoved() {
        return removed;
    }
}
