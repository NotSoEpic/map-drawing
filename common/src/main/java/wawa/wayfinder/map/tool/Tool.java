package wawa.wayfinder.map.tool;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2d;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.widgets.MapWidget;

public abstract class Tool {
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
