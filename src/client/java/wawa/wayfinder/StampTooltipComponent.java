package wawa.wayfinder;

import wawa.wayfinder.color.ColorPaletteManager;
import wawa.wayfinder.rendering.WayfinderRenderTypes;
import wawa.wayfinder.stampitem.StampTextureTooltipData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public record StampTooltipComponent(ResourceLocation texture) implements ClientTooltipComponent {
    @Override
    public int getHeight() {
        return 32 + 2;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return 32;
    }

    @Override
    public void renderText(Font textRenderer, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource vertexConsumers) {
        WayfinderClient.palette = ColorPaletteManager.get(Wayfinder.id("default"));
        VertexConsumer consumer = vertexConsumers.getBuffer(WayfinderRenderTypes.getPaletteSwap(texture));
        consumer.addVertex(matrix, x, y, 0).setUv(0, 0).setColor(-1);
        consumer.addVertex(matrix, x, y + 32, 0).setUv(0, 1).setColor(-1);
        consumer.addVertex(matrix, x + 32, y + 32, 0).setUv(1, 1).setColor(-1);
        consumer.addVertex(matrix, x + 32, y, 0).setUv(1, 0).setColor(-1);
    }

    public static String fromPathShorthand(String path) {
        return "textures/stamp/" + path + ".png";
    }

    public static StampTooltipComponent fromComponent(StampTextureTooltipData component) {
        return new StampTooltipComponent(component.texture().withPath(StampTooltipComponent::fromPathShorthand));
    }
}
