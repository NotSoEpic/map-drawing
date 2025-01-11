package wawa.wayfinder.mapmanager;

import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class DrawToolWidget extends ClickableWidget {
    private final MapScreen parent;
    private final int colorIndex;
    private final int size;
    public DrawToolWidget(MapScreen parent, int x, int y, int width, int height, int colorIndex, int size) {
        super(x, y, width, height, Text.of("draw tool"));
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
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getRight(), getBottom(), getVisualColor());
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    private int getVisualColor() {
        Color color = WayfinderClient.palette.colors().get(colorIndex);
        return color.getRGB() | 0xFF000000;
    }

    private int getDrawnColor() {
        Color color = ColorPalette.GRAYSCALE.colors().get(colorIndex);
        return color.getRGB() | 0xFF000000;
    }
}
