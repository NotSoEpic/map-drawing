package wawa.wayfinder.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.LerpedVector2d;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.input.KeyMappings.NormalMappings;
import wawa.wayfinder.map.tool.Tool;
import wawa.wayfinder.map.widgets.DebugTextRenderable;
import wawa.wayfinder.map.widgets.MapWidget;
import wawa.wayfinder.map.widgets.SideTabWidget;
import wawa.wayfinder.map.widgets.ToolPickerWidget;

import java.util.List;

import static wawa.wayfinder.input.KeyMappings.ToolPickerMappings;

public class MapScreen extends Screen {
    private int zoomNum = 0;
    private float zoom = 1f; // gui pixels per block (>1 zoom in, <1 zoom out)
    public LerpedVector2d lerpedPanning; // world space coordinate to center on
    private MapWidget mapWidget;
    public ToolPickerWidget toolPicker;

    public MapScreen(final Vector2d openingPos, final Vector2d endingPos) {
        super(Component.literal("Wayfinder Map"));
        this.lerpedPanning = new LerpedVector2d(openingPos, endingPos);
    }

    @Override
    protected void init() {
        super.init();
        this.mapWidget = new MapWidget(this);
        this.addRenderableWidget(this.mapWidget);
        this.toolPicker = new ToolPickerWidget(this.width - 15 - 16/2, 30);
        this.addRenderableWidget(this.toolPicker);
        this.addRenderableOnly(new DebugTextRenderable(this));
        this.addRenderableWidget(new SideTabWidget(30 - 16, 30 + 8, "Toggle player",
                () -> WayfinderClient.id(WayfinderClient.POSITION_HISTORY.visible ? "tabs/player_visible" : "tabs/player_hidden"),
                () -> WayfinderClient.POSITION_HISTORY.visible = !WayfinderClient.POSITION_HISTORY.visible));
        this.addRenderableWidget(new SideTabWidget(30 - 16, 30 + 8 + 28, "Clear history",
                () -> WayfinderClient.id("tabs/clear_history"), () -> WayfinderClient.POSITION_HISTORY.clear()));
        if (Tool.get() != null) {
            GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        }
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        this.lerpedPanning.tickProgress(0.05 * Minecraft.getInstance().getTimer().getRealtimeDeltaTicks());

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
    public Vector2d screenToWorld(final Vector2d mouse) {
        return new Vector2d(mouse).sub(this.width / 2d, this.height / 2d).div(this.zoom).add(this.lerpedPanning.get());
    }

    public void deltaZoom(final int delta) {
        this.zoomNum = Mth.clamp(this.zoomNum + delta, -2, 2);
        this.zoom = (float) Math.pow(2, this.zoomNum);
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        this.mapWidget.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        this.mapWidget.mouse = MapWidget.Mouse.NONE;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (NormalMappings.OPEN_MAP.mapping.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

        if (NormalMappings.SWAP.mapping.matches(keyCode, scanCode)) {
            Tool.swap();
        }

        if (NormalMappings.UNDO.mapping.matches(keyCode, scanCode) && Screen.hasControlDown()) {
            WayfinderClient.PAGE_MANAGER.undoChanges();
        }

        for (final ToolPickerMappings mapping : ToolPickerMappings.values()) {
            if (mapping.mapping.matches(keyCode, scanCode)) {
                mapping.swapToTool(this.toolPicker);
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public float getZoom() {
        return this.zoom;
    }
}
