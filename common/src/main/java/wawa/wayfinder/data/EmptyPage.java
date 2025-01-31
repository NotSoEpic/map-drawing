package wawa.wayfinder.data;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;

/**
 * A 512x512 pixel/block area with no associated texture, asynchronously attempting to load a texture when first created
 * <br>
 * On loading success, will become a {@link Page} with the image when next rendered
 * <br>
 * On loading failure (e.g. file doesn't exist), will allow editing to become a {@link Page}
 */
public class EmptyPage extends AbstractPage {
    private final PageManager parent;
    private boolean loading = true;
    private NativeImage loadedImage = null;
    public EmptyPage(int rx, int ry, PageManager parent, PageIO pageIO) {
        super(rx, ry);
        this.parent = parent;
        runPageLoadThread(pageIO);
    }

    /**
     * Attempts loading an image asynchronously
     */
    private void runPageLoadThread(PageIO pageIO) {
        Util.ioPool().execute(() -> {
            loadedImage = pageIO.tryLoadImage(rx, ry);
            loading = false;
        });
    }

    @Override
    public void setPixel(int x, int y, int RGBA) {
        // if the file loading is not complete, then either the loaded image or any edits made before will be lost if it does load
        if (!loading) {
            if (loadedImage == null) {
                loadedImage = new NativeImage(512, 512, true);
            }
            loadedImage.setPixelRGBA(x, y, RGBA);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int xOff, int yOff) {
        super.render(guiGraphics, xOff, yOff);
        if (loadedImage != null) {
            parent.replacePage(rx, ry, new Page(rx, ry, new DynamicTexture(loadedImage)));
        }
    }

    @Override
    public void save(PageIO pageIO, boolean close) {
        if (close) {
            close();
        }
    }

    @Override
    protected void close() {
        if (loadedImage != null)
            loadedImage.close();
    }
}
