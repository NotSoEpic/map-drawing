package wawa.mapwright.data;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import wawa.mapwright.MapwrightClient;

/**
 * A MapwrightClient.chunkSzexMapwrightClient.chunkSze pixel/block area with no associated texture, asynchronously attempting to load a texture when first created
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
    NativeImage redoImage = null;

    public EmptyPage(final int rx, final int ry, final PageManager parent, final PageIO pageIO) {
        super(rx, ry);
        this.parent = parent;
        this.runPageLoadThread(pageIO);
    }

    /**
     * Attempts loading an image asynchronously
     */
    private void runPageLoadThread(final PageIO pageIO) {
        Util.ioPool().execute(() -> {
            this.loadedImage = pageIO.tryLoadImage(this.rx, this.ry);
            this.loading = false;
        });
    }

    @Override
    public void setPixel(final int x, final int y, final int RGBA) {
        // if the file loading is not complete, then either the loaded image or any edits made before will be lost if it does load
        if (!this.isLoading()) {
            if (this.loadedImage == null) {
                this.loadedImage = new NativeImage(MapwrightClient.CHUNK_SIZE, MapwrightClient.CHUNK_SIZE, true);
            }
            this.loadedImage.setPixelRGBA(x, y, RGBA);
        }
    }

    @Override
    public NativeImage getImage() {
        return this.loadedImage;
    }

    @Override
    public NativeImage unboChanges(final NativeImage replacement) {
        if (!this.isLoading()) {
            final DynamicTexture texture = new DynamicTexture(MapwrightClient.CHUNK_SIZE, MapwrightClient.CHUNK_SIZE, false);
            texture.getPixels().copyFrom(replacement);
            this.parent.replacePage(this.rx, this.ry, new Page(this.rx, this.ry, texture)); //I think this is right?

            this.attemptedUndo = false;
            replacement.close();
            this.undoImage = null;
            this.redoImage = null;
            return new NativeImage(MapwrightClient.CHUNK_SIZE, MapwrightClient.CHUNK_SIZE, true);
        } else {
            this.attemptedUndo = true;
            this.undoImage = replacement;
            this.redoImage = new NativeImage(MapwrightClient.CHUNK_SIZE, MapwrightClient.CHUNK_SIZE, true);
            return this.redoImage; // will be modified once the page actually loads
        }

        //replace this empty page with a new one if an undo is requested and we are currently empty
        //if we are currently trying to load, differ the undo until we are loaded
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final double xOff, final double yOff) {
        super.render(guiGraphics, xOff, yOff);
        if (this.loadedImage != null) {
            this.parent.replacePage(this.rx, this.ry, new Page(this.rx, this.ry, new DynamicTexture(this.loadedImage)));
        }
    }

    @Override
    public void save(final PageIO pageIO, final boolean close) {
        if (close) {
            this.close();
        }
    }

    @Override
    protected void close() {
        if (this.loadedImage != null)
            this.loadedImage.close();
    }

    public boolean isLoading() {
        return this.loading;
    }
}
