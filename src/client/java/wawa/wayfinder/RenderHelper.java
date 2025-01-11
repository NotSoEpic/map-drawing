package wawa.wayfinder;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import wawa.wayfinder.mixin.client.DrawContextAccessor;

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
        context.drawString(Minecraft.getInstance().font, text, x, y, ARGB.color(0, 0, 255), false);
    }

    public static Vector2d smootherMouse() {
        MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        Window window = Minecraft.getInstance().getWindow();
        // client mouse is per gui pixel (up to 4x less accurate)
        return new Vector2d(mouse.xpos() * window.getGuiScaledWidth() / window.getScreenWidth(),
            mouse.ypos() * window.getGuiScaledHeight() / window.getScreenHeight());
    }
}
