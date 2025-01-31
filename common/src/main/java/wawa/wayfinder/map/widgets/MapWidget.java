package wawa.wayfinder.map.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.MapScreen;
import wawa.wayfinder.map.tool.Tool;

public class MapWidget extends AbstractWidget {
    private static final int padding = 30;
    private final MapScreen parent;

    public MapWidget(MapScreen parent) {
        super(padding, padding, parent.width - padding * 2, parent.height - padding * 2, Component.literal("Map Display"));
        this.parent = parent;
    }

    public Mouse mouse = Mouse.NONE;
    public double oldMouseX;
    public double oldMouseY;
    public enum Mouse {
        NONE, LEFT, RIGHT, MIDDLE
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Vector2d panning = parent.lerpedPanning.get();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(getX() + width / 2f, getY() + height / 2f, 0);
        guiGraphics.pose().scale(parent.getZoom(), parent.getZoom(), 1);
        // offset for rendering elements relative to the world, prevents floating point imprecision at extreme values
        int xOff = -Math.floorDiv(Mth.floor(panning.x), 512) * 512;
        int yOff = -Math.floorDiv(Mth.floor(panning.y), 512) * 512;
        guiGraphics.pose().translate(-panning.x - xOff, -panning.y - yOff, 0);
        guiGraphics.enableScissor(getX(), getY(), getRight(), getBottom());

        Vector2i topLeft = new Vector2i(parent.screenToWorld(new Vector2d(getX(), getY())).div(512), RoundingMode.FLOOR);
        Vector2i bottomRight = new Vector2i(parent.screenToWorld(new Vector2d(getRight(), getBottom())).div(512), RoundingMode.CEILING);

        for (int x = topLeft.x; x < bottomRight.x; x++) {
            for (int y = topLeft.y; y < bottomRight.y; y++) {
                WayfinderClient.PAGE_MANAGER.getOrCreatePage(x, y).render(guiGraphics, xOff, yOff);
            }
        }
        Vector2d world = parent.screenToWorld(new Vector2d(mouseX, mouseY));

        WayfinderClient.POSITION_HISTORY.render(guiGraphics, xOff, yOff);

        if (Tool.get() != null) {
            Tool.get().renderWorld(guiGraphics, Mth.floor(world.x), Mth.floor(world.y), xOff, yOff);
        }

        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (Tool.get() != null && Screen.hasControlDown()) {
            Tool.get().controlScroll(WayfinderClient.PAGE_MANAGER, mouseX, mouseY, scrollY);
        } else {
            parent.deltaZoom((int)scrollY);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            oldMouseX = mouseX;
            oldMouseY = mouseY;
            mouse = switch (button) {
                default -> Mouse.NONE;
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> Mouse.LEFT;
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> Mouse.RIGHT;
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> Mouse.MIDDLE;
            };
            Vector2d world = parent.screenToWorld(new Vector2d(mouseX, mouseY));
            if ((mouse == Mouse.LEFT || mouse == Mouse.RIGHT) && Tool.get() != null && !Screen.hasAltDown()) {
                Tool.get().hold(WayfinderClient.PAGE_MANAGER, mouse, world, world);
            } else if (Screen.hasAltDown()) {
                int color = WayfinderClient.PAGE_MANAGER.getPixel(Mth.floor(world.x), Mth.floor(world.y));
                parent.toolPicker.pickColor(color);
            }
            return true;
        }
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        Vector2d world = parent.screenToWorld(new Vector2d(mouseX, mouseY));
        Vector2d oldWorld = parent.screenToWorld(new Vector2d(oldMouseX, oldMouseY));
        if (mouse == Mouse.MIDDLE) {
            parent.lerpedPanning.set(parent.lerpedPanning.get().add(oldWorld).sub(world));
        } else if (!isMouseOver(mouseX, mouseY)) {
            mouse = Mouse.NONE;
            return;
        }
        if (Tool.get() != null && !Screen.hasAltDown()) {
            Tool.get().hold(WayfinderClient.PAGE_MANAGER, mouse, oldWorld, world);
        }
        oldMouseX = mouseX;
        oldMouseY = mouseY;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Tool tool = Tool.get();
        if (tool != null) {
            tool.release(WayfinderClient.PAGE_MANAGER);
        }

        mouse = Mouse.NONE;
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
