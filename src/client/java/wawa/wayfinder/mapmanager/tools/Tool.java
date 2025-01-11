package wawa.wayfinder.mapmanager.tools;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2i;
import wawa.wayfinder.mapmanager.MapWidget;

public abstract class Tool {
    private static Tool tool;

    public static void set(@Nullable Tool tool) {
        Tool.tool = tool;
        if (tool != null)
            tool.onSelect();
    }

    public static Tool get() {
        return tool;
    }

    protected abstract void onSelect();

    /**
     * Called every frame left click is held on the map
     * @param widget the map
     * @param initial if this is the first frame
     * @param shift if shift is held
     * @param mouse screen position of the mouse, (0,0) being top left of the map
     * @param world world position of the mouse
     * @return whether to capture the input
     */
    public abstract boolean leftClick(MapWidget widget, boolean initial, boolean shift, Vector2d mouse, Vector2i world);

    /**
     * Called every frame right click is held on the map
     * @param widget the map
     * @param initial if this is the first frame
     * @param shift if shift is held
     * @param mouse screen position of the mouse, (0,0) being top left of the map
     * @param world world position of the mouse
     * @return whether to capture the input
     */
    public abstract boolean rightClick(MapWidget widget, boolean initial, boolean shift, Vector2d mouse, Vector2i world);
    public abstract void render(MapWidget widget, GuiGraphics context, boolean shift, Vector2d mouse, Vector2i world);
    public abstract boolean hideMouse(MapWidget widget, Vector2d mouse, Vector2i world);
}
