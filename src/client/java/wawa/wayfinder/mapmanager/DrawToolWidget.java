package wawa.wayfinder.mapmanager;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;
import wawa.wayfinder.mapmanager.tools.PenTool;
import wawa.wayfinder.mapmanager.tools.Tool;

import java.awt.*;

public class DrawToolWidget extends AbstractWidget {
    private final int colorIndex;
    private final int size;
    private static final PenTool toolPencil = new PenTool(1, 0, false);
    public DrawToolWidget(MapScreen parent, int x, int y, int width, int height, int colorIndex, int size) {
        super(x, y, width, height, Component.nullToEmpty("draw tool"));
        this.colorIndex = colorIndex;
        this.size = size;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            toolPencil.size = size;
            toolPencil.highlight = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
            toolPencil.setColorIndex(colorIndex);
            Tool.set(toolPencil);
            return true;
        }
        return false;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getRight(), getBottom(), getVisualColor());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {}

    private int getVisualColor() {
        Color color = WayfinderClient.palette.colors().get(colorIndex);
        return color.getRGB() | 0xFF000000;
    }

    private int getDrawnColor() {
        Color color = ColorPalette.GRAYSCALE.colors().get(colorIndex);
        return color.getRGB() | 0xFF000000;
    }
}
