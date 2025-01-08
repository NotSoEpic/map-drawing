package beeisyou.mapdrawing;

import beeisyou.mapdrawing.mixin.client.DrawContextAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;
import org.joml.Vector2d;

import java.util.function.Function;

public class RenderHelper {
    // DrawContext.fill only accepts integer coordinates, which isn't precise enough
    public static void fill(DrawContext context, double x1, double y1, double x2, double y2, int color) {
        RenderLayer layer = RenderLayer.getGui();
        Matrix4f mat = context.getMatrices().peek().getPositionMatrix();
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

        VertexConsumer vertexConsumer = ((DrawContextAccessor)context).getVertexConsumers().getBuffer(layer);
        vertexConsumer.vertex(mat, (float)x1, (float)y1, 0).color(color);
        vertexConsumer.vertex(mat, (float)x1, (float)y2, 0).color(color);
        vertexConsumer.vertex(mat, (float)x2, (float)y2, 0).color(color);
        vertexConsumer.vertex(mat, (float)x2, (float)y1, 0).color(color);
    }

    public static void drawTexture(
            DrawContext context, Function<Identifier, RenderLayer> renderLayers, Identifier sprite,
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
        RenderLayer renderLayer = renderLayers.apply(sprite);
        Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
        VertexConsumer vertexConsumer = ((DrawContextAccessor)context).getVertexConsumers().getBuffer(renderLayer);
        vertexConsumer.vertex(matrix4f, (float)x1, (float)y1, 0.0F).texture(u1, v1).color(-1);
        vertexConsumer.vertex(matrix4f, (float)x1, (float)y2, 0.0F).texture(u1, v2).color(-1);
        vertexConsumer.vertex(matrix4f, (float)x2, (float)y2, 0.0F).texture(u2, v2).color(-1);
        vertexConsumer.vertex(matrix4f, (float)x2, (float)y1, 0.0F).texture(u2, v1).color(-1);
    }

    public static void badDebugText(DrawContext context, int x, int y, String text) {
        context.drawText(MinecraftClient.getInstance().textRenderer, text, x, y, ColorHelper.getArgb(0, 0, 255), false);
    }

    public static Vector2d smootherMouse() {
        Mouse mouse = MinecraftClient.getInstance().mouse;
        Window window = MinecraftClient.getInstance().getWindow();
        // client mouse is per gui pixel (up to 4x less accurate)
        return new Vector2d(mouse.getX() * window.getScaledWidth() / window.getWidth(),
            mouse.getY() * window.getScaledHeight() / window.getHeight());
    }
}
