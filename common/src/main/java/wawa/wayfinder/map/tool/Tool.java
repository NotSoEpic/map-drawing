package wawa.wayfinder.map.tool;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.widgets.MapWidget;

public abstract class Tool {
    private static Tool tool;
    private static Tool previous;

    public static void set(@Nullable final Tool tool) {
        if (Tool.tool != null) {
            Tool.tool.onDeselect();
        }
        Tool.previous = Tool.tool;
        Tool.tool = tool;
        if (tool != null) {
            tool.onSelect();
            GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        }
    }

    @Nullable
    public static Tool get() {
        return tool;
    }

    public static void swap() {
        if (Tool.previous != null) {
            final Tool temp = Tool.tool;
            Tool.tool.onDeselect();
            Tool.tool = Tool.previous;
            Tool.tool.onSelect();
            Tool.previous = temp;
        }
    }

    public void onSelect() {}

    public void onDeselect() {}

    public void hold(final PageManager activePage, final MapWidget.Mouse mouse, final Vector2d oldWorld, final Vector2d world) {}

    public void release(final PageManager activePage) {}

    public void controlScroll(final PageManager activePage, final double mouseX, final double mouseY, final double scrollY) {}

    /**
     * Renders tool components relative to the world, before transform pop
     * @param worldX position of mouse cursor in world
     * @param worldY position of mouse cursor in world
     */
    public void renderWorld(final GuiGraphics graphics, final int worldX, final int worldY, final double xOff, final double yOff) {}

    /**
     * Renders tool components relative to the screen, after transform pop
     * @param mouseX position of mouse cursor in world
     * @param mouseY position of mouse cursor in world
     */
    public void renderScreen(final GuiGraphics graphics, final double mouseX, final double mouseY) {}
}
