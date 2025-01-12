package wawa.wayfinder;

import net.minecraft.util.FastColor;
import wawa.wayfinder.mixin.client.DrawContextAccessor;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

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

    public static void drawTexture(
            GuiGraphics context, Function<ResourceLocation, RenderType> renderLayers, ResourceLocation sprite,
            double x, double y, float u, float v, double width, double height, int textureWidth, int textureHeight
    ) {
        double x1 = x;
        double y1 = y;
        double x2 = x1 + width;
        double y2 = y1 + height;
        float u1 = u / textureWidth;
        float u2 = (float) ((u + width) / textureWidth);
        float v1 = v / textureHeight;
        float v2 = (float) ((v + height) / textureHeight);
        RenderType renderLayer = renderLayers.apply(sprite);
        PoseStack matrices = context.pose();
        matrices.pushPose();

        Matrix4f posMat = matrices.last().pose();
        VertexConsumer vertexConsumer = ((DrawContextAccessor)context).getBufferSource().getBuffer(renderLayer);
        vertexConsumer.addVertex(posMat, (float)x1, (float)y1, 0.0F).setUv(u1, v1).setColor(-1);
        vertexConsumer.addVertex(posMat, (float)x1, (float)y2, 0.0F).setUv(u1, v2).setColor(-1);
        vertexConsumer.addVertex(posMat, (float)x2, (float)y2, 0.0F).setUv(u2, v2).setColor(-1);
        vertexConsumer.addVertex(posMat, (float)x2, (float)y1, 0.0F).setUv(u2, v1).setColor(-1);

        matrices.popPose();
    }

    public static void badDebugText(GuiGraphics context, int x, int y, String text) {
        context.drawString(Minecraft.getInstance().font, text, x, y, FastColor.ARGB32.color(0, 0, 255), false);
    }

    public static Vector2d smootherMouse() {
        MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        Window window = Minecraft.getInstance().getWindow();
        // client mouse is per gui pixel (up to 4x less accurate)
        return new Vector2d(mouse.xpos() * window.getGuiScaledWidth() / window.getScreenWidth(),
            mouse.ypos() * window.getGuiScaledHeight() / window.getScreenHeight());
    }
}
