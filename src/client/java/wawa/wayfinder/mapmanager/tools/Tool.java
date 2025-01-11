package wawa.wayfinder.mapmanager.tools;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2d;
import org.joml.Vector2i;
import wawa.wayfinder.mapmanager.MapWidget;

public interface Tool {
    void onSelect();

    /**
     * Called every frame left click is held on the map
     * @param widget the map
     * @param initial if this is the first frame
     * @param shift if shift is held
     * @param mouse screen position of the mouse, (0,0) being top left of the map
     * @param world world position of the mouse
     * @return whether to capture the input
     */
    boolean leftClick(MapWidget widget, boolean initial, boolean shift, Vector2d mouse, Vector2i world);

    /**
     * Called every frame right click is held on the map
     * @param widget the map
     * @param initial if this is the first frame
     * @param shift if shift is held
     * @param mouse screen position of the mouse, (0,0) being top left of the map
     * @param world world position of the mouse
     * @return whether to capture the input
     */
    boolean rightClick(MapWidget widget, boolean initial, boolean shift, Vector2d mouse, Vector2i world);
    void render(MapWidget widget, GuiGraphics context, boolean shift, Vector2d mouse, Vector2i world);
    boolean hideMouse(MapWidget widget, Vector2d mouse, Vector2i world);
}
