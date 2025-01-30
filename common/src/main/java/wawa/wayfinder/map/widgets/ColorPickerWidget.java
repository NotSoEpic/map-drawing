package wawa.wayfinder.map.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import wawa.wayfinder.map.tool.DrawTool;
import wawa.wayfinder.map.tool.Tool;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerWidget extends AbstractWidget {
    private final List<PaletteSwabWidget> swabs = new ArrayList<>();
    private static final DrawTool tool = new DrawTool();
    public ColorPickerWidget(int x, int y) {
        super(x, y, 0, 0, Component.literal("color picker"));
        for (int i = 0; i < DyeColor.values().length; i++) {
            DyeColor color = DyeColor.values()[i];
            int sx = x + (i % 2) * 10;
            int sy = y + (i / 2) * 10;
            width = Math.max(width, sx - getX() + 10);
            height = Math.max(height, sy - getY() + 10);
            swabs.add(new PaletteSwabWidget(sx, sy, color.getTextColor() | 0xFF000000));
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) || swabs.stream().anyMatch(swab -> swab.isMouseOver(mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (PaletteSwabWidget swab : swabs) {
            if (swab.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (PaletteSwabWidget swab : swabs) {
            swab.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    public static class PaletteSwabWidget extends AbstractWidget {
        private final int color;

        public PaletteSwabWidget(int x, int y, int color) {
            super(x, y, 8, 8, Component.literal("color swab"));
            this.color = color;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.fill(getX(), getY(), getRight(), getBottom(), color);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            int B = color & 0xFF;
            int G = color >> 8 & 0xFF;
            int R = color >> 16 & 0xFF;
            int A = color >> 24 & 0xFF;
            tool.setColor(A << 24 | B << 16 | G << 8 | R);
            Tool.set(tool);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
    }
}
