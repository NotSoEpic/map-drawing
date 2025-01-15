package wawa.wayfinder.mapmanager.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;
import wawa.wayfinder.mapmanager.tools.PenTool;
import wawa.wayfinder.mapmanager.tools.Tool;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class PalettePickerWidget extends AbstractWidget {
    private static final int hSlots = 4;
    private static final int slotWidth = 8;
    private static final int padding = 2;
    private static final int outerPadding = 4;
    private final int[] paletteIndices;
    public final PaletteBrush[] brushes;
    private PaletteBrush lastSelected;
    public PalettePickerWidget(ToolSelectionWidget parent, ColorPalette palette, BiPredicate<Integer, Color> shownColors) {
        super(0, 0, 0, 0, Component.literal("palette picker"));
        List<Integer> buildingIndices = new ArrayList<>();
        for (int i = 0; i < palette.colors().size(); i++) {
            if (shownColors.test(i, palette.colors().get(i))) {
                buildingIndices.add(i);
            }
        }
        paletteIndices = buildingIndices.stream().mapToInt(i -> i).toArray();
        brushes = new PaletteBrush[paletteIndices.length];
        for (int i = 0; i < paletteIndices.length; i++) {
            brushes[i] = new PaletteBrush(1, paletteIndices[i], true);
        }
        lastSelected = brushes[0];
        width = Math.min(hSlots, paletteIndices.length) * (slotWidth + padding) - padding + outerPadding * 3;
        height = (1 + (paletteIndices.length - 1) / hSlots) * (slotWidth + padding) - padding + outerPadding * 2;
        setX(parent.getX() - width);
        setY(parent.getY() + 30 + 15 - height / 2 - outerPadding);
    }

    public PenTool getLastSelected() {
        return lastSelected;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
//        guiGraphics.renderOutline(getX(), getY(), width, height, -1);
        for (int i = 0; i < paletteIndices.length; i++) {
            int x = getX() + (i % hSlots) * (slotWidth + padding) + outerPadding;
            int y = getY() + (i / hSlots) * (slotWidth + padding) + outerPadding;
            guiGraphics.fill(x, y, x + slotWidth, y + slotWidth, WayfinderClient.palette.colors().get(paletteIndices[i]).getRGB() | 0xFF000000);
        }
    }

    private int getSelectedIndex(double mouseX, double mouseY) {
        for (int i = 0; i < paletteIndices.length; i++) {
            int x = getX() + (i % hSlots) * (slotWidth + padding) + outerPadding;
            int y = getY() + (i / hSlots) * (slotWidth + padding) + outerPadding;
            if (mouseX >= x && mouseX <= x + slotWidth && mouseY >= y && mouseY <= y + slotWidth)
                return i;
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && this.isValidClickButton(button) && this.clicked(mouseX, mouseY)) {
            if (super.clicked(mouseX, mouseY)) {
                int i = getSelectedIndex(mouseX, mouseY);
                if (i != -1) {
                    Tool.set(brushes[i]);
                }
                this.playDownSound(Minecraft.getInstance().getSoundManager());
            }
            return true;
        }
        return false;
    }

    public boolean tryPickColor(int drawnColor) {
        int index = ColorPalette.GRAYSCALE.colors().indexOf(new Color(drawnColor));
        for (int i = 0; i < paletteIndices.length; i++) {
            if (paletteIndices[i] == index) {
                Tool.set(brushes[i]);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    private class PaletteBrush extends PenTool {
        public PaletteBrush(int size, int colorIndex, boolean highlight) {
            super(size, colorIndex, highlight);
        }

        @Override
        public void onSelect() {
            super.onSelect();
            PalettePickerWidget.this.lastSelected = this;
        }
    }
}
