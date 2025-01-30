package wawa.wayfinder.map.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.tool.DrawTool;
import wawa.wayfinder.map.tool.Tool;

import java.awt.*;
import java.util.function.Function;

public class SingleToolWidget extends AbstractWidget {
    private final ResourceLocation sprite;
    private final ResourceLocation highlight;
    private final Function<SingleToolWidget, Tool> toolFunction;
    public SingleToolWidget(int x, int y, ResourceLocation sprite, ResourceLocation highlight,
                            Function<SingleToolWidget, Tool> toolFunction, Component message) {
        super(x, y, 16, 16, message);
        this.sprite = sprite;
        this.highlight = highlight;
        this.toolFunction = toolFunction;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isMouseOver(mouseX, mouseY)) {
            guiGraphics.blitSprite(highlight, getX() - 1, getY() - 1, 18, 18);
        }
        guiGraphics.blitSprite(sprite, getX(), getY(), 16, 16);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            Tool.set(toolFunction.apply(this));
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    public static class Brush extends SingleToolWidget {
        public DrawTool last;
        private static final TextureAtlasSprite mask = Minecraft.getInstance().getGuiSprites().getSprite(WayfinderClient.id("tool/brush_mask"));
        private final ColorPickerWidget colorPicker;

        public Brush(int x, int y) {
            super(x, y,
                    WayfinderClient.id("tool/brush"),
                    WayfinderClient.id("tool/brush_highlight"),
                    w -> ((Brush)w).last,
                    Component.literal("brush")
            );
            colorPicker = new ColorPickerWidget(getX() - 40, getY() - 20, this);
            colorPicker.active = false;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            float[] rgb = new Color(last.getVisualColor()).getRGBColorComponents(null);
            guiGraphics.blit(getX(), getY(), 0, 16, 16, mask, rgb[2], rgb[1], rgb[0], 1);

            colorPicker.active = isMouseOver(mouseX, mouseY) || (colorPicker.isMouseOver(mouseX, mouseY) && colorPicker.isActive());

            if (colorPicker.isActive()) {
                colorPicker.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return super.isMouseOver(mouseX, mouseY) || colorPicker.isMouseOver(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!super.mouseClicked(mouseX, mouseY, button)) {
                return colorPicker.mouseClicked(mouseX, mouseY, button);
            }
            return false;
        }
    }
}
