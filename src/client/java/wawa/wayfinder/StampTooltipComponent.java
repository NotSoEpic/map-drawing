package wawa.wayfinder;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import wawa.wayfinder.color.ColorPaletteManager;
import wawa.wayfinder.rendering.WayfinderRenderTypes;
import wawa.wayfinder.stampitem.StampTextureTooltipData;

public class StampTooltipComponent implements ClientTooltipComponent {
    private final ResourceLocation texture;
    private final int w;
    private final int h;
    private static final int padding = 4 + 2;
    private static final ResourceLocation background = Wayfinder.id("page");
import org.joml.Matrix4f;
import wawa.wayfinder.color.ColorPaletteManager;
import wawa.wayfinder.rendering.WayfinderRenderTypes;
import wawa.wayfinder.stampitem.StampTextureTooltipData;

    public StampTooltipComponent(ResourceLocation texture) {
        this.texture = texture;
        Minecraft.getInstance().getTextureManager().getTexture(texture).bind();
        w = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        h = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
    }

    @Override
    public int getHeight() {
        return w + padding * 2 + 2;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return h + padding * 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics guiGraphics) {
        WayfinderClient.palette = ColorPaletteManager.get(Wayfinder.id("default"));
        guiGraphics.blitSprite(RenderType::guiTextured, background, x, y - 1, w + padding * 2, h + padding * 2);
        guiGraphics.blit(WayfinderRenderTypes::getPaletteSwap, texture, x + padding, y + padding - 1, 0, 0, w, h, w, h);
    }

    public static String fromPathShorthand(String path) {
        return "textures/stamp/" + path + ".png";
    }

    public static StampTooltipComponent fromComponent(StampTextureTooltipData component) {
        return new StampTooltipComponent(component.texture().withPath(StampTooltipComponent::fromPathShorthand));
    }
}
