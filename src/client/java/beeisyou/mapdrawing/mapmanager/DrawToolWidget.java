package beeisyou.mapdrawing.mapmanager;

import beeisyou.mapdrawing.MapDrawingClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class DrawToolWidget extends ClickableWidget {
    private final MapScreen parent;
    private final int color;
    private final int size;
    public DrawToolWidget(MapScreen parent, int x, int y, int width, int height, int color, int size) {
        super(x, y, width, height, Text.of("draw tool"));
        this.parent = parent;
        this.color = color;
        this.size = size;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            MapDrawingClient.penColor = color;
            MapDrawingClient.penSize = size;
            MapDrawingClient.highlight = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
            return true;
        }
        return false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getRight(), getBottom(), color);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
