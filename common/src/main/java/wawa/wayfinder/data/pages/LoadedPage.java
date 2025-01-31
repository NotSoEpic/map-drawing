package wawa.wayfinder.data.pages;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    /**
     * The associated image of this region. null if empty.
     */
    DynamicTexture associatedImage = null;

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

    public boolean failedToLoad = false;

    public long lastRenderedTime;

    public LoadedPage(final int rx, final int ry, @NotNull final PageManager manager) {
        super(rx, ry, manager);
        this.textureID = WayfinderClient.id("map_" + rx + "_" + ry);
    }

    /**
     * @return Whether this page has an image associated with it
     */
    public boolean empty() {
        return this.associatedImage == null;
    }

    public void render(final GuiGraphics guiGraphics, final int xOff, final int yOff) {
        this.lastRenderedTime = Util.getMillis();

        if (this.associatedImage != null && !this.unloaded) {
            if (this.dirtyUpload) {
                this.associatedImage.upload();
                //<- thanks IJ autocomplete
            }

            guiGraphics.blit(this.textureID, this.getGlobalX() + xOff, this.getGlobalY() + yOff, 0, 0, 512, 512, 512, 512);
        }
    }

    /**
     * Sets the given pixel to the given color, creating a new dynamic texture if needed.
     */
    public void setPixel(final int x, final int y, final int rgba) {
        if (rgba == 0) {
            if (this.associatedImage != null) { //erasing
                this.associatedImage.getPixels().setPixelRGBA(x, y, rgba);
            }
        } else {
            if (this.associatedImage == null) {
                this.associatedImage = new DynamicTexture(512, 512, true);
                Minecraft.getInstance().getTextureManager().register(this.textureID, this.associatedImage);
            }

            this.associatedImage.getPixels().setPixelRGBA(x, y, rgba);
        }

        this.dirtyUpload = true;
        this.dirtySave = true;
    }

    /**
     * gets the pixel at the given coordinates
     */
    public int getPixel(final int x, final int y) {
        if (this.associatedImage != null) {
            return this.associatedImage.getPixels().getPixelRGBA(x, y);
        } else {
            return 0;
        }
    }

    public void setImageExternally(final NativeImage image) {
        if (this.associatedImage == null) {
            this.associatedImage = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(this.textureID, this.associatedImage);
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
            if (this.associatedImage != null) {
                boolean empty = true;
                for (final int pixel : this.associatedImage.getPixels().getPixelsRGBA()) {
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
                    if (this.associatedImage == null) {
                        Files.deleteIfExists(path);
                    } else {
                        this.associatedImage.getPixels().writeToFile(path);
                    }

                    if (forced && this.associatedImage != null) {
                        this.closeAndNullify();
                    }
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void closeAndNullify() {
        Minecraft.getInstance().getTextureManager().release(this.textureID);
        this.associatedImage.close();
        this.associatedImage = null;
    }

    @Override
    public boolean isUnloaded() {
        return false;
    }
}
