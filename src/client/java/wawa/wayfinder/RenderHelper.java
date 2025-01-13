package wawa.wayfinder;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import wawa.wayfinder.mixin.client.DrawContextAccessor;

import java.awt.*;

public class RenderHelper {
    // DrawContext.fill only accepts integer coordinates, which isn't precise enough
    public static void fill(GuiGraphics context, double x1, double y1, double x2, double y2, int color) {
        RenderType layer = RenderType.gui();
        PoseStack matrices = context.pose();
        matrices.pushPose();

        Matrix4f mat = matrices.last().pose();
        mat.rotate(0, 0, 0, 0);

        if (x1 < x2) {
            double i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            double i = y1;
            y1 = y2;
            y2 = i;
        }

        VertexConsumer vertexConsumer = ((DrawContextAccessor)context).getBufferSource().getBuffer(layer);
        vertexConsumer.addVertex(mat, (float)x1, (float)y1, 0).setColor(color);
        vertexConsumer.addVertex(mat, (float)x1, (float)y2, 0).setColor(color);
        vertexConsumer.addVertex(mat, (float)x2, (float)y2, 0).setColor(color);
        vertexConsumer.addVertex(mat, (float)x2, (float)y1, 0).setColor(color);

        matrices.popPose();
    }

    public static void badDebugText(GuiGraphics context, int x, int y, String text) {
        context.drawString(Minecraft.getInstance().font, text, x, y, Color.BLUE.getRGB(), false);
    }

    public static Vector2d smootherMouse() {
        MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        Window window = Minecraft.getInstance().getWindow();
        // client mouse is per gui pixel (up to 4x less accurate)
        return new Vector2d(mouse.xpos() * window.getGuiScaledWidth() / window.getScreenWidth(),
            mouse.ypos() * window.getGuiScaledHeight() / window.getScreenHeight());
    }

    public static void renderTypeBlit(GuiGraphics guiGraphics, RenderType renderType, int x, int y, int blitOffset, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        renderTypeBlit(guiGraphics, renderType, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    public static void renderTypeBlit(GuiGraphics guiGraphics, RenderType renderType, int x1, int x2, int y1, int y2, int blitOffset, int uWidth, int vHeight, float uOffset, float vOffset, int textureWidth, int textureHeight) {
        renderTypeBlit(guiGraphics, renderType, x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / (float)textureWidth, (uOffset + (float)uWidth) / (float)textureWidth, (vOffset + 0.0F) / (float)textureHeight, (vOffset + (float)vHeight) / (float)textureHeight);
    }

    public static void renderTypeBlit(GuiGraphics guiGraphics, RenderType renderType, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV) {
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix4f, (float)x1, (float)y1, (float)blitOffset).setUv(minU, minV);
        bufferBuilder.addVertex(matrix4f, (float)x1, (float)y2, (float)blitOffset).setUv(minU, maxV);
        bufferBuilder.addVertex(matrix4f, (float)x2, (float)y2, (float)blitOffset).setUv(maxU, maxV);
        bufferBuilder.addVertex(matrix4f, (float)x2, (float)y1, (float)blitOffset).setUv(maxU, minV);

        renderType.draw(bufferBuilder.buildOrThrow());
    }
}
