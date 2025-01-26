package wawa.wayfinder.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import wawa.wayfinder.WayfinderClient;

public class LoadedPage extends AbstractPage {
    private final ResourceLocation textureID;
    private final DynamicTexture texture;
    private boolean uploadDirty = true; // whether the texture needs to reuploaded
    private boolean diskDirty = false; // whether the texture needs to be saved
    public LoadedPage(int rx, int ry, DynamicTexture texture) {
        super(rx, ry);
        this.texture = texture;
        textureID = WayfinderClient.id("map_" + rx + "_" + ry);
        Minecraft.getInstance().getTextureManager().register(textureID, texture);
    }

    public LoadedPage(int rx, int ry) {
        this(rx, ry, new DynamicTexture(512, 512, false));
        texture.getPixels().fillRect(0, 0, 512, 512, 0);
    }

    @Override
    public @Nullable AbstractPage putPixel(int x, int y, int RGBA) {
        texture.getPixels().setPixelRGBA(x, y, RGBA);
        uploadDirty = true;
        diskDirty = true;
        return null;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        if (uploadDirty) {
            texture.upload();
        }
        guiGraphics.blit(textureID, left, top, 0, 0, 512, 512, 512, 512);
    }
}
