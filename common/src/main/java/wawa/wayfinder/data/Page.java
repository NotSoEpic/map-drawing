package wawa.wayfinder.data;

import com.mojang.blaze3d.platform.NativeImage;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import wawa.wayfinder.Rendering;
import wawa.wayfinder.WayfinderClient;

/**
 * A 512x512 pixel area with associated {@link DynamicTexture} created from {@link EmptyPage}
 */
public class Page extends AbstractPage {
    private final ResourceLocation textureID;
    private final DynamicTexture texture;
    private boolean uploadDirty = true; // whether the texture needs to reuploaded
    private boolean diskDirty = false; // whether the texture needs to be saved

    public Page(final int rx, final int ry, final DynamicTexture texture) {
        super(rx, ry);
        this.texture = texture;
        this.textureID = WayfinderClient.id("map_" + rx + "_" + ry);
        Minecraft.getInstance().getTextureManager().register(this.textureID, texture);
    }

    public Page(final int rx, final int ry) {
        this(rx, ry, new DynamicTexture(512, 512, false));
        this.texture.getPixels().fillRect(0, 0, 512, 512, 0);
    }

    @Override
    public void setPixel(final int x, final int y, final int RGBA) {
        this.texture.getPixels().setPixelRGBA(x, y, RGBA);
        this.uploadDirty = true;
        this.diskDirty = true;
    }

    @Override
    public NativeImage getImage() {
        return this.texture.getPixels();
    }

    @Override
    public NativeImage unboChanges(final NativeImage replacement) {
        final NativeImage previous = new NativeImage(512, 512, false);
        previous.copyFrom(this.texture.getPixels());
        this.texture.getPixels().copyFrom(replacement);
        replacement.close();

        this.diskDirty = true;
        this.uploadDirty = true;

        return previous;
    }

    @Override
    public int getPixel(final int x, final int y) {
        return this.texture.getPixels().getPixelRGBA(x, y);
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final double xOff, final double yOff) {
        super.render(guiGraphics, xOff, yOff);
        final RenderType renderType = VeilRenderType.get(Rendering.RenderTypes.PALETTE_SWAP, this.textureID);
        if(renderType == null) return;

        if (this.uploadDirty) {
            this.texture.upload();
        }

        Rendering.renderTypeBlit(guiGraphics, renderType,
                this.left() + xOff, this.top() + yOff, 0, 0.0f, 0.0f, 512, 512, 512, 512);
    }

    /**
     * Attempts saving its image asynchronously
     * <br>
     * <strong>Be careful about accidentally closing the texture before this is done via {@link DynamicTexture#close()}, {@link net.minecraft.client.renderer.texture.TextureManager#register(ResourceLocation, AbstractTexture)}, or otherwise</strong>
     *
     * @param pageIO instance for output path
     * @param close  whether to close the image once done
     */
    @Override
    public void save(final PageIO pageIO, final boolean close) {
        if (this.diskDirty) {
            this.diskDirty = false;
            Util.ioPool().execute(() -> {
                pageIO.trySaveImage(this.rx, this.ry, this.isEmpty() ? null : this.texture.getPixels());
                if (close) {
                    this.close();
                }
            });
        }
    }

    public boolean isEmpty() {
        final int[] pixels = this.texture.getPixels().getPixelsRGBA();
        for (final int pixel : pixels) {
            if (pixel != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void close() {
        Minecraft.getInstance().getTextureManager().release(this.textureID);
    }
}
