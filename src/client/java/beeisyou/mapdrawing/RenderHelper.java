package beeisyou.mapdrawing;

import beeisyou.mapdrawing.mixin.client.DrawContextAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

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
}
