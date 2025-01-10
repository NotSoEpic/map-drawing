package beeisyou.mapdrawing;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class StampTooltipComponent implements TooltipComponent {
    public final Identifier texture;
    public StampTooltipComponent(Identifier texture) {
        this.texture = texture;
    }
    @Override
    public int getHeight(TextRenderer textRenderer) {
        return 16;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return 16;
    }

    @Override
    public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, VertexConsumerProvider.Immediate vertexConsumers) {
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getGuiTextured(texture));
        consumer.vertex(matrix, x, y, 0).texture(0, 0).color(-1);
        consumer.vertex(matrix, x, y + 16, 0).texture(0, 1).color(-1);
        consumer.vertex(matrix, x + 16, y + 16, 0).texture(1, 1).color(-1);
        consumer.vertex(matrix, x + 16, y, 0).texture(1, 0).color(-1);
    }
}
