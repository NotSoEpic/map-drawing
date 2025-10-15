package wawa.mapwright.map.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import org.joml.Vector2d;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.map.MapScreen;

public class DebugTextRenderable implements Renderable {
    private final MapScreen parent;
    public DebugTextRenderable(final MapScreen parent) {
        this.parent = parent;
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        if (Minecraft.getInstance().getDebugOverlay().showDebugScreen()) {
            final Vector2d world = this.parent.screenToWorld(new Vector2d(mouseX, mouseY));
            guiGraphics.drawString(Minecraft.getInstance().font,
                    (int) world.x + " " + (int) world.y, 0, 0, -1, false);
            guiGraphics.drawString(Minecraft.getInstance().font,
                    MapwrightClient.PAGE_MANAGER.pageIO.getPagePath().toString(), 0, 10, -1, false);
            guiGraphics.drawString(Minecraft.getInstance().font,
                    MapwrightClient.PAGE_MANAGER.getDebugCount(), 0, 20, -1, false);
        }
    }
}
