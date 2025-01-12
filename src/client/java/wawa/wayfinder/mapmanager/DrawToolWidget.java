package wawa.wayfinder.mapmanager;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;

import java.awt.*;

public class DrawToolWidget extends AbstractWidget {
    private final MapScreen parent;
    private final int colorIndex;
    private final int size;
    public DrawToolWidget(MapScreen parent, int x, int y, int width, int height, int colorIndex, int size) {
        super(x, y, width, height, Component.nullToEmpty("draw tool"));
        this.parent = parent;
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
            WayfinderClient.penColorIndex = colorIndex;
            WayfinderClient.penSize = size;
            WayfinderClient.highlight = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
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
        return color.getRGB();
    }

    private int getDrawnColor() {
        Color color = ColorPalette.GRAYSCALE.colors().get(colorIndex);
        return color.getRGB();
    }
}
