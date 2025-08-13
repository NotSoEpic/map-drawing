package wawa.wayfinder.map.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.Pin;
import wawa.wayfinder.map.tool.PinTool;
import wawa.wayfinder.map.tool.Tool;

import java.util.ArrayList;
import java.util.List;

public class CompassRoseWidget extends AbstractWidget {
    private final TextureAtlasSprite highlight = Minecraft.getInstance().getGuiSprites().getSprite(WayfinderClient.id("compass/rose"));

    public CompassRoseWidget(int x, int y) {
        super(x, y, 29, 29, Component.literal("Compass Rose"));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        final float alpha = Helper.getMouseProximityFade(
                new Vector2d(mouseX, mouseY),
                new Vector2d(this.getX() - width / 2, this.getY() - height / 2)
        );

        guiGraphics.blit(this.getX() - 29, this.getY() - 29, 0, 29, 29, this.highlight, 1, 1, 1, alpha);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
