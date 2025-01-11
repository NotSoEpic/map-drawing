package wawa.wayfinder.mapmanager;

import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.RenderHelper;
import wawa.wayfinder.rendering.WayfinderRenderTypes;
import org.joml.Vector2d;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;

/**
 * A 512x512 section of the map that contains an image
 */
public class LoadedMapWidgetRegion extends AbstractMapWidgetRegion {
    public final DynamicTexture texture;
    private boolean dirtyVisual = false; // unuploaded changes
    private boolean dirtySave = false;
    private boolean removed = false;
    public LoadedMapWidgetRegion(int rx, int rz, MapRegions regions) {
        super(rx, rz, regions);
        texture = new DynamicTexture(512, 512, false); // each chunk region is 512x512 blocks, seems fitting
    }

    public void initToClear() {
        texture.getPixels().applyToAllPixels(i -> 0); // reset to clear
        texture.upload();
        registerTexture();
    }

    public void registerTexture() {
        Minecraft.getInstance().getTextureManager().register(id(), texture);
    }

    @Override
    public void render(GuiGraphics context, MapWidget parent) {
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
        RenderHelper.drawTexture(context, WayfinderRenderTypes::getPaletteSwap, id(),
                ul.x, ul.y,
                (float) uv.x, (float) uv.y,
                wh.x, wh.y,
                (int) (512 * parent.scale),(int) (512 * parent.scale));
        if (Minecraft.getInstance().getDebugOverlay().showDebugScreen()) {
            RenderHelper.badDebugText(context, (int)ul.x + 2, (int)ul.y + 2, id().getPath());
        }
    }

    @Override
    public void save(Path regionPath) {
        if (!dirtySave)
            return;
        Path file = getPath(regionPath);
        Util.ioPool().execute(() -> {
            try {
                Files.createDirectories(file.getParent());
                texture.getPixels().writeToFile(file);
            } catch (IOException e) {
                Wayfinder.LOGGER.warn("Failed to save region {} {} to {}\n{}", rx(), rz(), file, e);
            }
        });
    }

    private void clearFile() {
        Path file = getPath(regions.getRegionPath());
        if (file != null) {
            Util.ioPool().execute(() -> {
                try {
                    Files.deleteIfExists(file);
                    dirtySave = false;
                } catch (IOException e) {
                    Wayfinder.LOGGER.warn("Failed to delete region {} {} of {}\n{}", rx(), rz(), file, e);
                }
            });
        } else {
            Wayfinder.LOGGER.warn("No path for map to clear");
        }
    }

    public boolean putPixelRelative(int x, int z, int color, boolean highlight) {
        if (!inBoundsRel(x, z))
            return false;
        if (!highlight || texture.getPixels().getPixel(x, z) == 0) {
            texture.getPixels().setPixel(x, z, color);
            dirtyVisual = true;
            dirtySave = true;
            return true;
        }
        return false;
    }

    public void checkDirty() {
        if (dirtyVisual) {
            if (Arrays.stream(texture.getPixels().getPixelsABGR()).allMatch(i -> i == 0)) {
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
