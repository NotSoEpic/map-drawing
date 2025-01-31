package wawa.wayfinder.map.tool;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.widgets.MapWidget;

public abstract class Tool {
    private static Tool tool;
    private static Tool previous;

    public static void set(@Nullable Tool tool) {
        if (Tool.tool != null) {
            Tool.tool.onDeselect();
        }
        Tool.previous = Tool.tool;
        Tool.tool = tool;
        if (tool != null) {
            tool.onSelect();
        }
    }

    @Nullable
    public static Tool get() {
        return tool;
    }

    public static void swap() {
        if (Tool.previous != null) {
            Tool temp = Tool.tool;
            Tool.tool.onDeselect();
            Tool.tool = Tool.previous;
            Tool.tool.onSelect();
            Tool.previous = temp;
        }
    }

    public void onSelect() {}

    public void onDeselect() {}

    public void hold(PageManager activePage, MapWidget.Mouse mouse, Vector2d oldWorld, Vector2d world) {}

    public void release(PageManager activePage) {};

    public void controlScroll(PageManager activePage, double mouseX, double mouseY, double scrollY) {}

    /**
     * Renders tool components relative to the world, before transform pop
     * @param worldX position of mouse cursor in world
     * @param worldY position of mouse cursor in world
     */
    public void renderWorld(GuiGraphics graphics, int worldX, int worldY, int xOff, int yOff) {}

    /**
     * Renders tool components relative to the screen, after transform pop
     * @param mouseX position of mouse cursor in world
     * @param mouseY position of mouse cursor in world
     */
    public void renderScreen(GuiGraphics graphics, double mouseX, double mouseY) {}
}
