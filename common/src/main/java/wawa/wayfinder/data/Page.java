package wawa.wayfinder.data;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import wawa.wayfinder.WayfinderClient;

/**
 * A 512x512 pixel area with associated {@link DynamicTexture} created from {@link EmptyPage}
 */
public class Page extends AbstractPage {
    private final ResourceLocation textureID;
    private final DynamicTexture texture;
    private boolean uploadDirty = true; // whether the texture needs to reuploaded
    private boolean diskDirty = false; // whether the texture needs to be saved
    public Page(int rx, int ry, DynamicTexture texture) {
        super(rx, ry);
        this.texture = texture;
        textureID = WayfinderClient.id("map_" + rx + "_" + ry);
        Minecraft.getInstance().getTextureManager().register(textureID, texture);
    }

    public Page(int rx, int ry) {
        this(rx, ry, new DynamicTexture(512, 512, false));
        texture.getPixels().fillRect(0, 0, 512, 512, 0);
    }

    @Override
    public void setPixel(int x, int y, int RGBA) {
        texture.getPixels().setPixelRGBA(x, y, RGBA);
        uploadDirty = true;
        diskDirty = true;
    }

    @Override
    public int getPixel(int x, int y) {
        return texture.getPixels().getPixelRGBA(x, y);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int xOff, int yOff) {
        super.render(guiGraphics, xOff, yOff);
        if (uploadDirty) {
            texture.upload();
        }
        guiGraphics.blit(textureID, left() + xOff, top() + yOff, 0, 0, 512, 512, 512, 512);
    }

    /**
     * Attempts saving its image asynchronously
     * <br>
     * <strong>Be careful about accidentally closing the texture before this is done via {@link DynamicTexture#close()}, {@link net.minecraft.client.renderer.texture.TextureManager#register(ResourceLocation, AbstractTexture)}, or otherwise</strong>
     * @param pageIO instance for output path
     * @param close whether to close the image once done
     */
    @Override
    public void save(PageIO pageIO, boolean close) {
        if (diskDirty) {
            diskDirty = false;
            Util.ioPool().execute(() -> {
                pageIO.trySaveImage(rx, ry, isEmpty() ? null : texture.getPixels());
                if (close) {
                    close();
                }
            });
        }
    }

    public boolean isEmpty() {
        int[] pixels = texture.getPixels().getPixelsRGBA();
        for (int pixel : pixels) {
            if (pixel != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void close() {
        Minecraft.getInstance().getTextureManager().release(textureID);
    }
}
