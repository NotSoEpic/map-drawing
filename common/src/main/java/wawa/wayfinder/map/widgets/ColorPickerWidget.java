package wawa.wayfinder.map.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.tool.DrawTool;
import wawa.wayfinder.map.tool.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ColorPickerWidget extends AbstractWidget {
    private final List<PaletteSwabWidget> swabs = new ArrayList<>();
    private final SingleToolWidget.Brush brush;
    public ColorPickerWidget(int x, int y, SingleToolWidget.Brush brush) {
        super(x, y, 0, 0, Component.literal("color picker"));
        this.brush = brush;
        for (int i = 0; i < DyeColor.values().length; i++) {
            DyeColor color = DyeColor.values()[i];
            int sx = (i % 4) * 10;
            int sy = (i / 4) * 10;
            width = Math.max(width, sx + 8);
            height = Math.max(height, sy + 8);
            swabs.add(new PaletteSwabWidget(this, sx, sy, color.getTextColor() | 0xFF000000));
        }
        brush.last = swabs.getFirst().tool;
    }

    public List<DrawTool> getBrushes() {
        return swabs.stream().map(swab -> swab.tool).collect(Collectors.toList());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        final int padding = 10;
        return isActive() && ((
                mouseX >= (double)this.getX() - padding
                && mouseY >= (double)this.getY() - padding
                && mouseX < (double)(this.getX() + this.width + padding)
                && mouseY < (double)(this.getY() + this.height + padding)
                ) || swabs.stream().anyMatch(swab -> swab.isMouseOver(mouseX, mouseY))
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (PaletteSwabWidget swab : swabs) {
            if (swab.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return isMouseOver(mouseX, mouseY);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        TooltipRenderUtil.renderTooltipBackground(guiGraphics, getX(), getY(), width, height, 0);
        for (PaletteSwabWidget swab : swabs) {
            swab.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    public static class PaletteSwabWidget extends AbstractWidget {
        private final ColorPickerWidget parent;
        private final int relX;
        private final int relY;
        public final int color;
        public final DrawTool tool;

        public PaletteSwabWidget(ColorPickerWidget parent, int x, int y, int color) {
            super(0, 0, 8, 8, Component.literal("color swab"));
            this.parent = parent;
            this.relX = x;
            this.relY = y;
            this.color = color;
            this.tool = new DrawTool();
            tool.setColor(getABGR());
            tool.icon = WayfinderClient.id("cursor/brush");
        }

        @Override
        public int getX() {
            return parent.getX() + relX;
        }

        @Override
        public int getY() {
            return parent.getY() + relY;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.fill(getX(), getY(), getRight(), getBottom(), color);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            Tool.set(tool);
            parent.brush.last = tool;
        }

        public int getABGR() {
            int B = color & 0xFF;
            int G = color >> 8 & 0xFF;
            int R = color >> 16 & 0xFF;
            int A = color >> 24 & 0xFF;
            return A << 24 | B << 16 | G << 8 | R;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }
}
