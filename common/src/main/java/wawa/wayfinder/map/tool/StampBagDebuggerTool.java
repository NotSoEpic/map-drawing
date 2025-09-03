package wawa.wayfinder.map.tool;

import com.mojang.blaze3d.platform.NativeImage;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2d;
import wawa.wayfinder.Rendering;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.stamp_bag.StampBagHandler;
import wawa.wayfinder.map.stamp_bag.StampInformation;
import wawa.wayfinder.mixin.NativeImageAccessor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StampBagDebuggerTool extends Tool {

    private StampInformation information = null;
    private final ResourceLocation id = WayfinderClient.id("stampbagdebugger_tool");

    private DynamicTexture renderable = null;

    @Override
    public void onSelect() {
        information = null;

        ArrayList<StampInformation> collection = new ArrayList<>();
        WayfinderClient.STAMP_HANDLER.bulkRequestStamps(collection, 0);

        information = collection.getLast();
    }

    @Override
    public void onDeselect() {
        information = null;

        Minecraft.getInstance().getTextureManager().release(id);

        renderable = null;
    }

    @Override
    public void renderWorld(GuiGraphics graphics, int worldX, int worldY, double xOff, double yOff) {
        if (information != null) {
            if (renderable == null) {
                NativeImage requestedImage = information.getRequestedImage();
                if (requestedImage == null) {
                    return;
                }

                renderable = new DynamicTexture(requestedImage);
                Minecraft.getInstance().getTextureManager().register(id, renderable);
            }

            final RenderType renderType = VeilRenderType.get(Rendering.RenderTypes.PALETTE_SWAP, this.id);
            if (renderType != null) {
                double x = worldX + xOff - (double) (renderable.getPixels().getWidth() / 2);
                double y = worldY + yOff - (double) (renderable.getPixels().getHeight() / 2);
                Rendering.renderTypeBlit(graphics, renderType, x, y, 0, 0f, 0f,
                        renderable.getPixels().getWidth(), renderable.getPixels().getHeight(), renderable.getPixels().getWidth(), renderable.getPixels().getHeight(), 1);

                graphics.drawString(Minecraft.getInstance().font, information.customName(), (int) x, (int) y - 4, Color.GREEN.getRGB());
                graphics.drawString(Minecraft.getInstance().font, information.fileName(), (int) x, (int) y - 16, Color.GREEN.getRGB());
                graphics.drawString(Minecraft.getInstance().font, "is favorited: " + information.isFavorited(), (int) x, (int) y - 26, Color.GREEN.getRGB());
            }
        }
    }
}
