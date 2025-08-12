package wawa.wayfinder.map.widgets;

import com.mojang.blaze3d.platform.NativeImage;
import foundry.veil.api.client.color.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import wawa.wayfinder.Rendering;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.tool.PaletteDrawTool;
import wawa.wayfinder.map.tool.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ColorPickerWidget extends AbstractWidget {
    public static final int COLOR_COUNT = 8;

    private final List<PaletteSwabWidget> swabs = new ArrayList<>();
    private final SingleToolWidget.BrushWidget brushWidget;
    private final int defaultX;
    private final int defaultY;
    public ColorPickerWidget(final int rightAnchor, final int centerY, final SingleToolWidget.BrushWidget brushWidget) {
        super(rightAnchor, centerY, 0, 0, Component.literal("color picker"));
        this.brushWidget = brushWidget;

        final NativeImage texture = Rendering.getPaletteTexture();

        final int colorCount = Math.min(COLOR_COUNT, texture.getWidth());
        for (int i = 0; i < colorCount; i++) {
            final float n = (float) (i+1) / (COLOR_COUNT + 1);
            final Color color = new Color(n, n, n);
            final int pixelRGBA = texture.getPixelRGBA(i+1, 0);
            final Color trueColor = new Color(Integer.reverseBytes(pixelRGBA) >> 8);
            final int columns = 4;
            final int sx = (i % columns) * 10;
            final int sy = (i / columns) * 10;
            this.width = Math.max(this.width, sx + 8);
            this.height = Math.max(this.height, sy + 8);
            this.swabs.add(new PaletteSwabWidget(this, sx, sy, color.argb(), trueColor.argb()));
        }

        this.defaultX = rightAnchor - this.width - 4;
        this.defaultY = centerY - this.height / 2 + 8;
        this.setX(this.defaultX);
        this.setY(this.defaultY);

        brushWidget.last = this.swabs.getFirst().tool;
    }

    public void openToMouse(final double mouseX, final double mouseY) {
        this.active = true;
        this.setX((int) (mouseX - this.width / 2));
        this.setY((int) (mouseY - this.height / 2));
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
        public final int visualColor;
        public final PaletteDrawTool tool;

        public PaletteSwabWidget(final ColorPickerWidget parent, final int x, final int y, final int color, final int visualColor) {
            super(0, 0, 8, 8, Component.literal("color swab"));
            this.parent = parent;
            this.relX = x;
            this.relY = y;
            this.color = color;
            this.visualColor = visualColor;
            this.tool = new PaletteDrawTool(this.getABGR(), this.visualColor, parent.brushWidget);
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
            if (this.isMouseOver(mouseX, mouseY)) {
                guiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getRight() + 1, this.getBottom() + 1, 0xFFFFFFFF);
                guiGraphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 0xFF000000);
                guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getRight() - 1, this.getBottom() - 1, this.visualColor);
            } else {
                guiGraphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), this.visualColor);
            }

        }

        @Override
        public void onClick(final double mouseX, final double mouseY) {
            Tool.set(this.tool);
            this.parent.brushWidget.last = this.tool;
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
