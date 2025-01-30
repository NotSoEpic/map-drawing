package wawa.wayfinder.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Vector2d;
import wawa.wayfinder.LerpedVector2d;
import wawa.wayfinder.input.KeyMappings;
import wawa.wayfinder.map.tool.Tool;
import wawa.wayfinder.map.widgets.ColorPickerWidget;
import wawa.wayfinder.map.widgets.DebugTextRenderable;
import wawa.wayfinder.map.widgets.MapWidget;

import java.util.List;

public class MapScreen extends Screen {
    private int zoomNum = 0;
    private float zoom = 1f; // gui pixels per block (>1 zoom in, <1 zoom out)
    public LerpedVector2d lerpedPanning; // world space coordinate to center on
    private MapWidget mapWidget;

    public MapScreen(Vector2d openingPos, Vector2d endingPos) {
        super(Component.literal("Wayfinder Map"));
        lerpedPanning = new LerpedVector2d(openingPos, endingPos);
    }

    @Override
    protected void init() {
        super.init();
        mapWidget = new MapWidget(this);
        addRenderableWidget(mapWidget);
        addRenderableWidget(new ColorPickerWidget(width - 25, 35));
        addRenderableOnly(new DebugTextRenderable(this));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        lerpedPanning.tickProgress(0.05 * Minecraft.getInstance().getTimer().getRealtimeDeltaTicks());

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (Tool.get() != null) {
            Tool.get().renderScreen(guiGraphics, mouseX, mouseY);
        }
    }

    // widgets that are rendered last (on top) have the highest interaction priority
    @Override
    public List<? extends GuiEventListener> children() {
        return super.children().reversed();
    }

    /**
     * @param mouse Coordinate in screen space, with top left being (0, 0)
     * @return Coordinate in world space, with top left of block at (0, 0) being (0, 0)
     */
    public Vector2d screenToWorld(Vector2d mouse) {
        return new Vector2d(mouse).sub(width / 2d, height / 2d).div(zoom).add(lerpedPanning.get());
    }

    public void deltaZoom(int delta) {
        zoomNum = Mth.clamp(zoomNum + delta, -2, 2);
        zoom = (float) Math.pow(2, zoomNum);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        mapWidget.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyMappings.OPEN_MAP.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public float getZoom() {
        return zoom;
    }
}
