package beeisyou.mapdrawing;

import beeisyou.mapdrawing.mapmanager.MapManager;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

public class MapScreen extends Screen {
    MapManager manager;
    protected MapScreen() {
        super(Text.translatable("map"));
        leftClick = false;
        manager = MapDrawingClient.mapManager;
    }

    double panX;
    double panZ;
    int zoomval = 0;
    double scale = 1;

    public Vector2d mouseToMap(double mouseX, double mouseY) {
        Vector2d map = new Vector2d(mouseX, mouseY);
        map.add(panX, panZ);
        map.div(scale);
        return map;
    }
    public Vector2d mapToMouse(double mapX, double mapY) {
        Vector2d mouse = new Vector2d(mapX, mapY);
        mouse.mul(scale);
        mouse.sub(panX, panZ);
        return mouse;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (leftClick) {
            Vector2d prev = mouseToMap(prevX, prevY);
            Vector2d curr = mouseToMap(mouseX, mouseY);
            manager.drawLine(prev.x, prev.y, curr.x, curr.y,
                    ColorHelper.getArgb(255, 255, 255, 255));
        }
        if (rightClick) {
            panX += prevX - mouseX;
            panZ += prevY - mouseY;
        }
        prevX = mouseX;
        prevY = mouseY;
        super.render(context, mouseX, mouseY, delta);
        context.getMatrices().push();
        context.getMatrices().translate(-panX, -panZ, 0);
        context.getMatrices().scale((float) scale, (float) scale, 1);
        manager.render(context, -panX, -panZ, 100, 100, scale, scale < 1);
        context.getMatrices().pop();
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
            manager.clear();
            panX = 0;
            panZ = 0;
            scale = 1;
            zoomval = 0;
            leftClick = false;
            rightClick = false;
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
        zoomval = (int) MathHelper.clamp(zoomval + verticalAmount, -4, 4);
        Vector2d oldMapPos = mouseToMap(mouseX, mouseY);
        scale = Math.pow(2, zoomval);
        Vector2d newMousePos = mapToMouse(oldMapPos.x, oldMapPos.y);
        panX += newMousePos.x - mouseX;
        panZ += newMousePos.y - mouseY;
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
