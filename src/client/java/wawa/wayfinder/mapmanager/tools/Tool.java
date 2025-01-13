package wawa.wayfinder.mapmanager.tools;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2i;
import wawa.wayfinder.mapmanager.widgets.MapWidget;

public abstract class Tool {
    private static Tool tool;

    public static void set(@Nullable Tool tool) {
        if (Tool.tool != null)
            Tool.tool.onDeselect();
        Tool.tool = tool;
        if (tool != null)
            tool.onSelect();
    }

    public static Tool get() {
        return tool;
    }

    protected abstract void onSelect();
    protected void onDeselect() {}

    public void leftDown(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {}

    /**
     * Called every frame left click is held on the map
     * @param widget the map
     * @param shift if shift is held
     * @param mouse screen position of the mouse, (0,0) being top left of the map
     * @param world world position of the mouse
     */
    public void leftHold(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {}
    public void leftUp(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {}


    public void rightDown(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {}
    /**
     * Called every frame right click is held on the map
     * @param widget the map
     * @param shift if shift is held
     * @param mouse screen position of the mouse, (0,0) being top left of the map
     * @param world world position of the mouse
     */
    public void rightHold(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {}
    public void rightUp(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {}

    public void ctrlScroll(MapWidget widget, Vector2d mouse, Vector2i world, double verticalAmount) {}

    public abstract void render(MapWidget widget, GuiGraphics context, boolean shift, Vector2d mouse, Vector2i world);
    public boolean hideWhenInactive() {
        return true;
    }
    public abstract boolean hideMouse(MapWidget widget, Vector2d mouse, Vector2i world);
}
