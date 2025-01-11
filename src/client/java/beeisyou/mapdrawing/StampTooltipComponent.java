package beeisyou.mapdrawing;

import beeisyou.mapdrawing.color.ColorPaletteManager;
import beeisyou.mapdrawing.rendering.MapDrawingRenderLayers;
import beeisyou.mapdrawing.stampitem.StampTextureTooltipData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public record StampTooltipComponent(Identifier texture) implements TooltipComponent {
    @Override
    public int getHeight(TextRenderer textRenderer) {
        return 32 + 2;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return 32;
    }

    @Override
    public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, VertexConsumerProvider.Immediate vertexConsumers) {
        MapDrawingClient.palette = ColorPaletteManager.get(MapDrawing.id("default"));
        VertexConsumer consumer = vertexConsumers.getBuffer(MapDrawingRenderLayers.getPaletteSwap(texture));
        consumer.vertex(matrix, x, y, 0).texture(0, 0).color(-1);
        consumer.vertex(matrix, x, y + 32, 0).texture(0, 1).color(-1);
        consumer.vertex(matrix, x + 32, y + 32, 0).texture(1, 1).color(-1);
        consumer.vertex(matrix, x + 32, y, 0).texture(1, 0).color(-1);
    }

    public static String fromPathShorthand(String path) {
        return "textures/stamp/" + path + ".png";
    }

    public static StampTooltipComponent fromComponent(StampTextureTooltipData component) {
        return new StampTooltipComponent(component.texture().withPath(StampTooltipComponent::fromPathShorthand));
    }
}
