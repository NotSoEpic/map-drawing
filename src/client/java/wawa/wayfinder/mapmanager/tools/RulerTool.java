package wawa.wayfinder.mapmanager.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2i;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.mapmanager.widgets.MapWidget;

public class RulerTool extends Tool {
    private @Nullable Vector2i startPos;
    private @Nullable Vector2i endPos;
    private static final TextureAtlasSprite rulerSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("cursor/ruler"));
    @Override
    protected void onSelect() {
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
    public void renderTool(MapWidget widget, GuiGraphics context, boolean shift, Vector2d mouse, Vector2i world) {
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

        context.drawString(Minecraft.getInstance().font, String.format("(%d, %d)", startPos.x, startPos.y), (int)screenStart.x, (int)screenStart.y, -1, true);
        context.drawString(Minecraft.getInstance().font, String.format("(%d, %d)", target.x, target.y), (int)screenEnd.x, (int)screenEnd.y, -1, true);

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
        return true;
    }

    @Override
    public @Nullable TextureAtlasSprite getCursorIcon() {
        return rulerSprite;
    }

    @Override
    public Vector2i getCursorIconOffset() {
        return new Vector2i(-4, -15);
    }
}
