package wawa.wayfinder.map.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.Pin;
import wawa.wayfinder.map.tool.PaletteDrawTool;
import wawa.wayfinder.map.tool.PinTool;
import wawa.wayfinder.map.tool.Tool;

import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class SingleToolWidget extends AbstractWidget {
    private final ResourceLocation sprite;
    private final ResourceLocation highlight;
    private final Function<SingleToolWidget, Tool> toolFunction;
    public SingleToolWidget(final int x, final int y, final ResourceLocation sprite, final ResourceLocation highlight,
                            final Function<SingleToolWidget, Tool> toolFunction, final Component message) {
        super(x, y, 16, 16, message);
        this.sprite = sprite;
        this.highlight = highlight;
        this.toolFunction = toolFunction;
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        if (this.isMouseOver(mouseX, mouseY)) {
            guiGraphics.blitSprite(this.highlight, this.getX() - 1, this.getY() - 1, 18, 18);
        }
        guiGraphics.blitSprite(this.sprite, this.getX(), this.getY(), 16, 16);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            Tool.set(this.toolFunction.apply(this));
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {}

    public static class BrushWidget extends SingleToolWidget {
        public PaletteDrawTool last;
        private static final TextureAtlasSprite mask = Minecraft.getInstance().getGuiSprites().getSprite(WayfinderClient.id("tool/brush_mask"));
        private final ColorPickerWidget colorPicker;

        public BrushWidget(final int x, final int y) {
            super(x, y,
                    WayfinderClient.id("tool/brush"),
                    WayfinderClient.id("tool/brush_highlight"),
                    w -> ((BrushWidget)w).last,
                    Component.literal("brush")
            );
            this.colorPicker = new ColorPickerWidget(this.getX() - 5, this.getY(), this);
            this.colorPicker.active = false;
        }

        public List<PaletteDrawTool> getBrushes() {
            return this.colorPicker.getBrushes();
        }

        public void openToMouse(final double mouseX, final double mouseY) {
            this.colorPicker.openToMouse(mouseX, mouseY);
        }

        @Override
        protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            final float[] rgb = new Color(this.last.getVisualColor()).getRGBColorComponents(null);
            guiGraphics.blit(this.getX(), this.getY(), 0, 16, 16, mask, rgb[0], rgb[1], rgb[2], 1);

            if (this.isMouseOver(mouseX, mouseY) || (this.colorPicker.isMouseOver(mouseX, mouseY) && this.colorPicker.isActive())) {
                this.colorPicker.active = true;
            } else {
                this.colorPicker.active = false;
                this.colorPicker.resetPos();
            }

            if (this.colorPicker.isActive()) {
                this.colorPicker.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public boolean isMouseOver(final double mouseX, final double mouseY) {
            return super.isMouseOver(mouseX, mouseY) || this.colorPicker.isMouseOver(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            if (!super.mouseClicked(mouseX, mouseY, button)) {
                return this.colorPicker.mouseClicked(mouseX, mouseY, button);
            }
            return false;
        }
    }

    public static class PinWidget extends SingleToolWidget {
        private Pin.Type last;

        public PinWidget(final int x, final int y, final Function<SingleToolWidget, Tool> toolFunction, final Component message) {
            super(x, y, Pin.TEXTURE, Pin.TEXTURE_HIGHLIGHT, toolFunction, message);
            this.last = ((PinTool)toolFunction.apply(this)).currentPin;
        }

        @Override
        protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
            this.last.draw(guiGraphics, this.getX(), this.getY(), this.isMouseOver(mouseX, mouseY), false);
        }
    }
}
