package wawa.mapwright.map.tool;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import wawa.mapwright.data.PageManager;
import wawa.mapwright.map.widgets.MapWidget;

public abstract class Tool {
    public void onSelect() {}

    public void onDeselect() {}

    public void mouseDown(final PageManager activePage, final MapWidget.MouseType mouseType, final Vector2d world) {}

    public void mouseMove(final PageManager activePage, final MapWidget.MouseType mouseType, final Vector2dc oldWorld, final Vector2dc world) {}

    public void mouseRelease(final PageManager activePage, final MapWidget.MouseType mouseType, final Vector2d world) {}

    public void controlScroll(final PageManager activePage, final double mouseX, final double mouseY, final double scrollY) {}

    /**
     * Renders tool components relative to the world, before transform pop
     * @param worldX position of mouse cursor in world
     * @param worldY position of mouse cursor in world
     */
    public void renderWorld(final GuiGraphics graphics, final int worldX, final int worldY, final double xOff, final double yOff) {}

    /**
     * Renders tool components relative to the screen, after transform pop
     * Fractional gui coordinates are customName in the PoseStack before this function and popped afterwards
     * @param mouseX position of mouse cursor in gui coordinates
     * @param mouseY position of mouse cursor in gui coordinates
     */
    public void renderScreen(final GuiGraphics graphics, final double mouseX, final double mouseY) {}

    public void tick(final boolean mapOpen) {}
}
