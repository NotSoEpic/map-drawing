package wawa.wayfinder.mapmanager;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.joml.RoundingMode;
import org.joml.Vector2i;
import wawa.wayfinder.RenderHelper;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.rendering.WayfinderRenderLayers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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

        if (!shouldBeRendered(parent))
            return;
        checkDirty();
        if (isRemoved()) {
            clearFile();
            regions.put(rx(), rz(), new UnloadedMapWidgetRegion(rx(), rz(), regions));
            return;
        }
        Vector2i ul = new Vector2i(parent.worldToScreen(rx() * 512, rz() * 512, true), RoundingMode.FLOOR);
        Vector2i wh = new Vector2i(512 * parent.scale, 512 * parent.scale, RoundingMode.FLOOR);
        context.blit(WayfinderRenderLayers::getPaletteSwap, id(), ul.x, ul.y, 0, 0, wh.x, wh.y, wh.x, wh.y);
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
