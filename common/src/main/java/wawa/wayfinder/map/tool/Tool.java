package wawa.wayfinder.map.tool;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.MapScreen;

public abstract class Tool {
    private static Tool tool;

    public static void set(@Nullable Tool tool) {
        if (Tool.tool != null) {
            Tool.tool.onDeselect();
        }
        Tool.tool = tool;
        if (tool != null) {
            tool.onSelect();
        }
    }

    @Nullable
    public static Tool get() {
        return tool;
    }

    public void onSelect() {}

    public void onDeselect() {}

    public void hold(PageManager activePage, MapScreen.Mouse mouse, Vector2d oldWorld, Vector2d world) {}

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
