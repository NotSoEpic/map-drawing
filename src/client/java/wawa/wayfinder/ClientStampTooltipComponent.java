package wawa.wayfinder;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;
import wawa.wayfinder.color.ColorPaletteManager;
import wawa.wayfinder.rendering.WayfinderRenderTypes;
import wawa.wayfinder.stampitem.StampComponent;

import java.util.ArrayList;
import java.util.List;

public class ClientStampTooltipComponent implements ClientTooltipComponent {
    private final StampComponent component;
    private final List<Vector2i> sizes;
    private int width;
    private int height;
    private static final int padding = 4 + 2;
    private static final ResourceLocation background = Wayfinder.id("page");

    public ClientStampTooltipComponent(StampComponent component) {
        this.component = component;
        this.sizes = new ArrayList<>(component.textures().size());
        for (ResourceLocation texture : component.textures()) {
            Minecraft.getInstance().getTextureManager().getTexture(texture.withPath(ClientStampTooltipComponent::fromPathShorthand)).bind();
            int texWidth = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            int texHeight = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
            sizes.add(new Vector2i(texWidth, texHeight));
        };
        recalculateBounds();
    }

    private void recalculateBounds() {
        width = 0;
        height = 0;
        int spriteX = 0;
        for (int i = 0; i < sizes.size(); i++) {
            int j = (i + component.selectedIndex) % sizes.size();
            width = Math.max(width, sizes.get(j).x + padding * 2 + spriteX);
            height = Math.max(height, sizes.get(j).y);
            spriteX += padding * 2;
        }
    }

    @Override
    public int getHeight() {
        return height + padding * 2 + 2;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return width + padding * 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        WayfinderClient.palette = ColorPaletteManager.get(Wayfinder.id("default"));

        int len = component.textures().size();
        for (int i = len - 1; i >= 0; i--) {
            int j = (i + component.selectedIndex) % len;
            ResourceLocation texture = component.textures().get(j).withPath(ClientStampTooltipComponent::fromPathShorthand);
            Vector2i size = sizes.get(j);
            int spriteX = i * padding * 2;
            int spriteY = (height - size.y) / 2;
            guiGraphics.blitSprite(background, x + spriteX, y - 1 + spriteY, size.x + padding * 2, size.y + padding * 2);

            RenderHelper.renderTypeBlit(guiGraphics, WayfinderRenderTypes.getPaletteSwap(texture),
                    x + padding + spriteX, y + padding - 1 + spriteY, 0,
                    0.0f, 0.0f,
                    size.x, size.y, size.x, size.y
            );
        }
    }

    public static String fromPathShorthand(String path) {
        return "textures/stamp/" + path + ".png";
    }
}
