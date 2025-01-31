package wawa.wayfinder.data.pages;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A loaded region of a WayFinder map. Represents either an empty page, or a page that has image data associated with it.
 */
public class LoadedPage extends AbstractPage {

    /**
     * The Texture id for the associatedImage.
     */
    private final ResourceLocation textureID;

    private DynamicTexture associatedImage = null;

    /**
     * Whether this associated image should be reuploaded to the GPU.
     */
    boolean dirtyUpload;

    /**
     * whether to save this page occasionally
     */
    boolean dirtySave;

    /**
     * Whether this page has been unloaded or not. Prevents rendering etc from executing
     */
    boolean unloaded = false;

    private boolean failedToLoad = false;

    private long lastRenderedTime;

    public LoadedPage(final int rx, final int ry, @NotNull final PageManager manager) {
        super(rx, ry, manager);
        this.textureID = WayfinderClient.id("map_" + rx + "_" + ry);
    }

    public void render(final GuiGraphics guiGraphics, final int xOff, final int yOff) {
        this.lastRenderedTime = Util.getMillis();

        if (this.getAssociatedImage() != null && !this.unloaded) {
            if (this.dirtyUpload) {
                this.getAssociatedImage().upload();
                //<- thanks IJ autocomplete
            }

            guiGraphics.blit(this.textureID, this.getGlobalX() + xOff, this.getGlobalY() + yOff, 0, 0, 512, 512, 512, 512);
        }
    }

    /**
     * Sets the given pixel to the given color, creating a new dynamic texture if needed.
     */
    public void setPixel(final int x, final int y, final int rgba) {
        this.getAssociatedImage().getPixels().setPixelRGBA(x, y, rgba);

        this.dirtyUpload = true;
        this.dirtySave = true;
    }

    /**
     * gets the pixel at the given coordinates
     */
    public int getPixel(final int x, final int y) {
        if (this.getAssociatedImage() != null) {
            return this.getAssociatedImage().getPixels().getPixelRGBA(x, y);
        } else {
            return 0;
        }
    }

    public void createImageIfEmpty() {
        if (this.getAssociatedImage() == null) {
            this.associatedImage = new DynamicTexture(512, 512, true);
            Minecraft.getInstance().getTextureManager().register(this.textureID, this.getAssociatedImage());
        }
    }

    public void setImageExternally(final NativeImage image) {
        if (this.getAssociatedImage() == null) {
            this.associatedImage = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(this.textureID, this.getAssociatedImage());
        }
    }

    //saving

    /**
     * Unloads this page and clears all relevant information, saving its image to disk.
     *
     * @param close Whether this page should directly close and return null instead of a new UnloadedPage
     * @return the unloaded variant of this page
     */
    public UnloadedPage unloadPage(final boolean close) {
        this.dirtyUpload = false;
        this.dirtySave = false;

        this.savePage(close);

        this.unloaded = true;
        return new UnloadedPage(this.rx, this.ry, this.manager);
    }

    public void savePage() {
        this.savePage(false);
    }

    /**
     * Saves the associated image, if not empty, to disk.
     *
     * @param forced whether the save should be forced. IE exiting game
     */
    public void savePage(final boolean forced) {
        if (this.dirtySave || forced) {
            //let's check to see if our image is empty now or not!
            if (this.getAssociatedImage() != null) {
                boolean empty = true;
                for (final int pixel : this.getAssociatedImage().getPixels().getPixelsRGBA()) {
                    if (pixel != 0) {
                        empty = false;
                        break;
                    }
                }

                if (empty) {
                    this.closeAndNullify();
                }
            }

            Util.ioPool().execute(() -> {
                try {
                    final Path path = this.manager.pageIO.pageFilepath(this.rx, this.ry);
                    if (this.getAssociatedImage() == null) {
                        Files.deleteIfExists(path);
                    } else {
                        this.getAssociatedImage().getPixels().writeToFile(path);
                    }

                    if (forced && this.getAssociatedImage() != null) {
                        this.closeAndNullify();
                    }
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void closeAndNullify() {
        Minecraft.getInstance().getTextureManager().release(this.textureID);
        this.getAssociatedImage().close();
        this.associatedImage = null;
    }

    @Override
    public boolean isUnloaded() {
        return false;
    }

    /**
     * The associated image of this region. null if empty.
     */
    public DynamicTexture getAssociatedImage() {
        return this.associatedImage;
    }

    public boolean isFailedToLoad() {
        return this.failedToLoad;
    }

    public long getLastRenderedTime() {
        return this.lastRenderedTime;
    }

    public void setFailedToLoad(final boolean failedToLoad) {
        this.failedToLoad = failedToLoad;
    }
}
