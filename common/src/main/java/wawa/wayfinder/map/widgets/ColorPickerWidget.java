package wawa.wayfinder.map.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.tool.PaletteDrawTool;
import wawa.wayfinder.map.tool.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ColorPickerWidget extends AbstractWidget {
    private final List<PaletteSwabWidget> swabs = new ArrayList<>();
    private final SingleToolWidget.Brush brush;
    private final int defaultX;
    private final int defaultY;
    public ColorPickerWidget(final int x, final int y, final SingleToolWidget.Brush brush) {
        super(x, y, 0, 0, Component.literal("color picker"));
        this.brush = brush;
        this.defaultX = x;
        this.defaultY = y;
        for (int i = 0; i < DyeColor.values().length; i++) {
            final DyeColor color = DyeColor.values()[i];
            final int sx = (i % 4) * 10;
            final int sy = (i / 4) * 10;
            this.width = Math.max(this.width, sx + 8);
            this.height = Math.max(this.height, sy + 8);
            this.swabs.add(new PaletteSwabWidget(this, sx, sy, color.getTextColor() | 0xFF000000));
        }
        brush.last = this.swabs.getFirst().tool;
    }

    public void resetPos() {
        this.setX(this.defaultX);
        this.setY(this.defaultY);
    }

    public List<PaletteDrawTool> getBrushes() {
        return this.swabs.stream().map(swab -> swab.tool).collect(Collectors.toList());
    }

    @Override
    public boolean isMouseOver(final double mouseX, final double mouseY) {
        final int padding = 10;
        return this.isActive() && ((
                mouseX >= (double)this.getX() - padding
                && mouseY >= (double)this.getY() - padding
                && mouseX < (double)(this.getX() + this.width + padding)
                && mouseY < (double)(this.getY() + this.height + padding)
                ) || this.swabs.stream().anyMatch(swab -> swab.isMouseOver(mouseX, mouseY))
        );
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        for (final PaletteSwabWidget swab : this.swabs) {
            if (swab.mouseClicked(mouseX, mouseY, button)) {
                this.active = false;
                return true;
            }
        }
        return this.isMouseOver(mouseX, mouseY);
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        TooltipRenderUtil.renderTooltipBackground(guiGraphics, this.getX(), this.getY(), this.width, this.height, 0);
        for (final PaletteSwabWidget swab : this.swabs) {
            swab.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {}

    public static class PaletteSwabWidget extends AbstractWidget {
        private final ColorPickerWidget parent;
        private final int relX;
        private final int relY;
        public final int color;
        public final PaletteDrawTool tool;

        public PaletteSwabWidget(final ColorPickerWidget parent, final int x, final int y, final int color) {
            super(0, 0, 8, 8, Component.literal("color swab"));
            this.parent = parent;
            this.relX = x;
            this.relY = y;
            this.color = color;
            this.tool = new PaletteDrawTool(this.getABGR(), parent.brush);
            this.tool.icon = WayfinderClient.id("cursor/brush");
        }

        @Override
        public int getX() {
            return this.parent.getX() + this.relX;
        }

        @Override
        public int getY() {
            return this.parent.getY() + this.relY;
        }

        @Override
        protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
            guiGraphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), this.color);
        }

        @Override
        public void onClick(final double mouseX, final double mouseY) {
            Tool.set(this.tool);
            this.parent.brush.last = this.tool;
        }

        public int getABGR() {
            final int B = this.color & 0xFF;
            final int G = this.color >> 8 & 0xFF;
            final int R = this.color >> 16 & 0xFF;
            final int A = this.color >> 24 & 0xFF;
            return A << 24 | B << 16 | G << 8 | R;
        }

        @Override
        protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {}
    }
}
