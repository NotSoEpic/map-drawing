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

    boolean attemptedUndo = false;
    NativeImage undoImage = null;

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
        if (!isLoading()) {
            if (loadedImage == null) {
                loadedImage = new NativeImage(512, 512, true);
            }
            loadedImage.setPixelRGBA(x, y, RGBA);
        }
    }

    @Override
    public NativeImage getImage() {
        return loadedImage;
    }

    @Override
    public void unboChanges(NativeImage replacement) {
        if (!isLoading()) {
            DynamicTexture texture = new DynamicTexture(512, 512, false);
            texture.getPixels().copyFrom(replacement);
            parent.replacePage(rx, ry, new Page(rx, ry, texture)); //I think this is right?

            attemptedUndo = false;
            replacement.close();
            undoImage = null;
        } else {
            attemptedUndo = true;
            undoImage = replacement;
        }

        //replace this empty page with a new one if an undo is requested and we are currently empty
        //if we are currently trying to load, differ the undo until we are loaded
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

    public boolean isLoading() {
        return loading;
    }
}
