package wawa.wayfinder.mapmanager.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2i;
import wawa.wayfinder.mapmanager.widgets.MapWidget;

public class RulerTool extends Tool {
    private @Nullable Vector2i startPos;
    private @Nullable Vector2i endPos;
    @Override
    protected void onSelect() {
        startPos = null;
        endPos = null;
    }

    @Override
    protected void onDeselect() {
        startPos = null;
        endPos = null;
    }

    @Override
    public void leftDown(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        startPos = world;
        endPos = null;
    }

    @Override
    public void rightDown(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        endPos = null;
    }

    @Override
    public void leftUp(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        endPos = world;
        if (endPos.equals(startPos)) {
            endPos = null;
            startPos = null;
        }
    }

    @Override
    public void rightUp(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        endPos = world;
        if (endPos.equals(startPos)) {
            endPos = null;
            startPos = null;
        }
    }

    @Override
    public void render(MapWidget widget, GuiGraphics context, boolean shift, Vector2d mouse, Vector2i world) {
        if (startPos == null)
            return;
        Vector2i target = endPos;
        if (target == null)
            target = world;
        if (startPos.equals(target))
            return;
        Vector2d screenStart = widget.worldToScreen(startPos.x, startPos.y, true);
        Vector2d screenEnd = widget.worldToScreen(target.x, target.y, true);
        double dx = screenEnd.x - screenStart.x;
        double dz = screenEnd.y - screenStart.y;
        int steps = (int) Math.max(1, Math.ceil(Math.max(Math.abs(dx), Math.abs(dz))));
        dx /= steps;
        dz /= steps;
        double x = screenStart.x;
        double z = screenStart.y;
        for (int i = 0; i < steps + 1; i++) {
            if (x > 0 && x < context.guiWidth() && z > 0 && z < context.guiHeight())
                context.fill((int)x, (int)z, (int)x + 1, (int)z + 1, -1);
            x += dx;
            z += dz;
        }

        double dist = target.distance(startPos);
        Vector2d halfway = new Vector2d(screenEnd).add(screenStart).div(2);
        context.drawString(Minecraft.getInstance().font, String.format("%.0f blocks", dist), (int)halfway.x, (int)halfway.y, -1, true);
    }

    @Override
    public boolean hideWhenInactive() {
        return false;
    }

    @Override
    public boolean hideMouse(MapWidget widget, Vector2d mouse, Vector2i world) {
        return false;
    }
}
