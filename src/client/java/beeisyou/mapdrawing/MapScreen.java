package beeisyou.mapdrawing;

import beeisyou.mapdrawing.mapmanager.MapManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.lwjgl.glfw.GLFW;

public class MapScreen extends Screen {
    MapManager manager;
    protected MapScreen() {
        super(Text.translatable("map"));
        leftClick = false;
        manager = MapDrawingClient.mapManager;
        manager.centerWorld(MinecraftClient.getInstance().player.getX(), MinecraftClient.getInstance().player.getZ());
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Mouse mouse = MinecraftClient.getInstance().mouse;
        Window window = MinecraftClient.getInstance().getWindow();
        // client mouse is per gui pixel (up to 4x less accurate)
        double mx = mouse.getX() * window.getScaledWidth() / window.getWidth();
        double my = mouse.getY() * window.getScaledHeight() / window.getHeight();
        if (leftClick) {
            manager.drawLineScreen(prevX, prevY, mx, my,
                    ColorHelper.getArgb(255, 255, 255, 255));
        }
        if (rightClick) {
            manager.pan(prevX - mx, prevY - my);
        }
        prevX = mx;
        prevY = my;
        super.render(context, mouseX, mouseY, delta);
        manager.render(context, width, height, mx, my);
    }

    boolean leftClick;
    boolean rightClick;
    double prevX;
    double prevY;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            leftClick = true;
            rightClick = false;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            rightClick = true;
            leftClick = false;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            manager.reset();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            leftClick = false;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            rightClick = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        manager.deltaScale((int) verticalAmount, mouseX, mouseY);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (MapBindings.openMap.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
