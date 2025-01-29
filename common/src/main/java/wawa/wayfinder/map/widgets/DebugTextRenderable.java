package wawa.wayfinder.map.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import org.joml.Vector2d;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.MapScreen;

public class DebugTextRenderable implements Renderable {
    private final MapScreen parent;
    public DebugTextRenderable(MapScreen parent) {
        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Vector2d world = parent.screenToWorld(new Vector2d(mouseX, mouseY));
        guiGraphics.drawString(Minecraft.getInstance().font,
                (int)world.x + " " + (int)world.y, 0, 0, -1, false);
        guiGraphics.drawString(Minecraft.getInstance().font,
                WayfinderClient.PAGE_MANAGER.pageIO.getMapPath().toString(), 0, 10, -1, false);
        guiGraphics.drawString(Minecraft.getInstance().font,
                WayfinderClient.PAGE_MANAGER.getDebugCount(), 0, 20, -1, false);
    }
}
