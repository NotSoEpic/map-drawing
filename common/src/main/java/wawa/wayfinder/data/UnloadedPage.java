package wawa.wayfinder.data;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.jetbrains.annotations.Nullable;
import wawa.wayfinder.WayfinderClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class UnloadedPage extends AbstractPage {
    private PageManager parent;
    private boolean loading = true;
    private NativeImage loadedImage = null;
    public UnloadedPage(int rx, int ry, PageManager parent) {
        super(rx, ry);
        this.parent = parent;
        tryLoadPage();
    }

    private void tryLoadPage() {
        Path path = parent.pageIO.pageFilepath(rx, ry);
        Util.ioPool().execute(() -> {
            File file = new File(path.toUri());
            if (file.isFile()) {
                try {
                    InputStream inputStream = Files.newInputStream(path);
                    loadedImage = NativeImage.read(inputStream);
                } catch (IOException e) {
                    WayfinderClient.LOGGER.error("Failed to load image {}\n{}", path, e);
                }
            }
            loading = false;
        });
    }

    @Override
    public @Nullable AbstractPage putPixel(int x, int y, int RGBA) {
        if (loading)
            return null;
        LoadedPage newPage = new LoadedPage(rx, ry);
        newPage.putPixel(x, y, RGBA);
        return newPage;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        if (loadedImage != null) {
            parent.replacePage(rx, ry, new LoadedPage(rx, ry, new DynamicTexture(loadedImage)));
        }
    }
}
