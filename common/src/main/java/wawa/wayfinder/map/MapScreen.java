package wawa.wayfinder.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.input.KeyMappings;
import wawa.wayfinder.map.tool.Tool;

public class MapScreen extends Screen {
    private int zoomNum = 0;
    private float zoom = 1f; // gui pixels per block (>1 zoom in, <1 zoom out)
    public Vector2d panning; // world space coordinate to center on

    public MapScreen(Vector2d openingPos) {
        super(Component.literal("Wayfinder Map"));
        panning = openingPos;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(guiGraphics);
        final int padding = 30;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(width / 2f, height / 2f, 0);
        guiGraphics.pose().scale(zoom, zoom, 1);
        // offset for rendering elements relative to the world, prevents floating point imprecision at extreme values
        int xOff = -Math.floorDiv(Mth.floor(panning.x), 512) * 512;
        int yOff = -Math.floorDiv(Mth.floor(panning.y), 512) * 512;
        guiGraphics.pose().translate(-panning.x - xOff, -panning.y - yOff, 0);
        guiGraphics.enableScissor(padding, padding, width - padding, height - padding);

        Vector2i topLeft = new Vector2i(screenToWorld(new Vector2d(padding, padding)).div(512), RoundingMode.FLOOR);
        Vector2i bottomRight = new Vector2i(screenToWorld(new Vector2d(width - padding, height - padding)).div(512), RoundingMode.CEILING);

        for (int x = topLeft.x; x < bottomRight.x; x++) {
            for (int y = topLeft.y; y < bottomRight.y; y++) {
                WayfinderClient.PAGE_MANAGER.getOrCreateRegion(x, y).render(guiGraphics, xOff, yOff);
            }
        }
        Vector2d world = screenToWorld(new Vector2d(mouseX, mouseY));

        WayfinderClient.POSITION_HISTORY.render(guiGraphics, xOff, yOff);

        if (Tool.get() != null) {
            Tool.get().renderWorld(guiGraphics, Mth.floor(world.x), Mth.floor(world.y), xOff, yOff);
        }

        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();

        if (Tool.get() != null) {
            Tool.get().renderScreen(guiGraphics, mouseX, mouseY);
        }

        guiGraphics.drawString(Minecraft.getInstance().font,
                (int)world.x + " " + (int)world.y, 0, 0, -1, false);
        guiGraphics.drawString(Minecraft.getInstance().font,
                WayfinderClient.PAGE_MANAGER.pageIO.getMapPath().toString(), 0, 10, -1, false);
        guiGraphics.drawString(Minecraft.getInstance().font,
                WayfinderClient.PAGE_MANAGER.getDebugCount(), 0, 20, -1, false);
    }

    /**
     * @param mouse Coordinate in screen space, with top left being (0, 0)
     * @return Coordinate in world space, with top left of block at (0, 0) being (0, 0)
     */
    public Vector2d screenToWorld(Vector2d mouse) {
        return new Vector2d(mouse).sub(width / 2d, height / 2d).div(zoom).add(panning);
    }

    private Mouse mouse = Mouse.NONE;
    private double oldMouseX;
    private double oldMouseY;
    public enum Mouse {
        NONE, LEFT, RIGHT, MIDDLE
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        Vector2d world = screenToWorld(new Vector2d(mouseX, mouseY));
        Vector2d oldWorld = screenToWorld(new Vector2d(oldMouseX, oldMouseY));
        if (mouse == Mouse.MIDDLE) {
            panning.add(oldWorld).sub(world);
        }
        if (Tool.get() != null) {
            Tool.get().hold(WayfinderClient.PAGE_MANAGER, mouse, oldWorld, world);
        }
        oldMouseX = mouseX;
        oldMouseY = mouseY;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (Tool.get() != null && Screen.hasControlDown()) {
            Tool.get().controlScroll(WayfinderClient.PAGE_MANAGER, mouseX, mouseY, scrollY);
        } else {
            zoomNum = Mth.clamp(zoomNum + (int) scrollY, -2, 2);
            zoom = (float) Math.pow(2, zoomNum);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        mouse = Mouse.NONE;
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        mouse = switch (button) {
            default -> Mouse.NONE;
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> Mouse.LEFT;
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> Mouse.RIGHT;
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> Mouse.MIDDLE;
        };
        if ((mouse == Mouse.LEFT || mouse == Mouse.RIGHT) && Tool.get() != null) {
            Vector2d world = screenToWorld(new Vector2d(mouseX, mouseY));
            Tool.get().hold(WayfinderClient.PAGE_MANAGER, mouse, world, world);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyMappings.OPEN_MAP.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
