package wawa.wayfinder.mapmanager;

import wawa.wayfinder.Wayfinder;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;

/**
 * A 512x512 section of the map, either still in the state of loading or having no image
 */
public class UnloadedMapWidgetRegion extends AbstractMapWidgetRegion {
    private boolean loading = false;
    private LoadedMapWidgetRegion loaded = null;
    private NativeImage loadedImage = null;

    public UnloadedMapWidgetRegion(int rx, int rz, MapRegions regions) {
        super(rx, rz, regions);
    }

    public void tryLoadRegion() {
        loading = true;
        Util.ioPool().execute(this::loadRegionIO);
    }

    private void loadRegionIO() {
        Path path = getPath(regions.getRegionPath());
        if (path != null) {
            try {
                File file = new File(path.toUri());
                if (file.isFile()) {
                    InputStream inputStream = Files.newInputStream(path);
                    loadedImage = NativeImage.read(inputStream);
                }
                loading = false;
            } catch (IOException e) {
                Wayfinder.LOGGER.warn("Failed to load {}\n{}", path, e);
                loading = false;
            }
        } else {
            loading = false;
            Wayfinder.LOGGER.warn("No path for map to load");
        }
    }

    @Override
    public boolean putPixelRelative(int x, int z, int color, boolean highlight) {
        if (!loading) {
            loaded = new LoadedMapWidgetRegion(rx(), rz(), regions);
            loaded.initToClear();
            regions.put(rx(), rz(), loaded);
            return loaded.putPixelRelative(x, z, color, highlight);
        }
        return false;
    }

    @Override
    public void render(GuiGraphics context, MapWidget parent) {
        super.render(context, parent);

        if (loadedImage != null) {
            loaded = new LoadedMapWidgetRegion(rx(), rz(), regions);
            loaded.texture.setPixels(loadedImage);
            loaded.texture.upload();
            loaded.registerTexture();
            regions.put(rx(), rz(), loaded);
        }
    }

    @Override
    public void save(Path path) {}
}
