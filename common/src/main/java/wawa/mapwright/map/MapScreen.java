package wawa.mapwright.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import wawa.mapwright.Helper;
import wawa.mapwright.LerpedVector2d;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.map.stamp_bag.widgets.StampBagWidget;
import wawa.mapwright.map.tool.PanTool;
import wawa.mapwright.map.widgets.CompassWidget;
import wawa.mapwright.map.widgets.DebugTextRenderable;
import wawa.mapwright.map.widgets.MapWidget;
import wawa.mapwright.map.widgets.ToolPickerWidget;
import wawa.mapwright.platform.Services;
import wawa.mapwright.platform.services.IKeyMappings;

import java.util.ArrayList;
import java.util.List;

public class MapScreen extends Screen {
    private int zoomNum = 0;
    private float zoom = 1f; // gui pixels per block (>1 zoom in, <1 zoom out)
    public LerpedVector2d lerpedPanning; // world space coordinate to center on
    public Vector2d backgroundPanning = new Vector2d(); // panning irrespective of zoom
    private MapWidget mapWidget;
    public ToolPickerWidget toolPicker;
    public CompassWidget compassWidget;

    public List<AbstractWidget> allWidgets = new ArrayList<>();

    public StampBagWidget stampBag;

    public StampBagScreen stampScreen;

    public static boolean cursorAdjusted;

    public MapScreen(final Vector2d openingPos) {
        super(Component.literal("Mapwright Map"));
        this.lerpedPanning = new LerpedVector2d(openingPos, MapwrightClient.targetPanningPosition);
        cursorAdjusted = false;
    }

    @Override
    protected void init() {
        super.init();
        this.mapWidget = new MapWidget(this);
        this.allWidgets.add(this.mapWidget);
        this.addRenderableWidget(this.mapWidget);

        final int toolX = this.width - 15 - 16 / 2;
        this.toolPicker = new ToolPickerWidget(toolX, 30);
        this.allWidgets.add(this.toolPicker);
        this.addRenderableWidget(this.toolPicker);

        this.stampBag = new StampBagWidget(toolX, this.toolPicker.finalToolY + 20, this);
        this.allWidgets.add(this.stampBag);
        this.addRenderableWidget(this.stampBag);

        this.compassWidget = new CompassWidget(this.width - 100, this.height - 90);
        this.addRenderableOnly(this.compassWidget);
        this.addRenderableOnly(new DebugTextRenderable(this));

        if (MapwrightClient.TOOL_MANAGER.get() instanceof PanTool) {
            GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        } else {
            GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        }

//        if (!cursorAdjusted) { // Init is run whenever the screen is resized & the cursor is set in the super of the constructor. its evil but it works
//            Window window = Minecraft.getInstance().getWindow();
//            GLFW.glfwSetCursorPos(window.getWindow(), window.getScreenWidth() / 2f, window.getScreenHeight() / 2.75f);
//            cursorAdjusted = true;
//        }

        this.stampScreen = StampBagScreen.INSTANCE;
        this.stampScreen.setMapScreen(this);
        this.stampScreen.changeStage(this.stampScreen.getState());
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        final Vector2d diffTracker = new Vector2d();
        this.lerpedPanning.tickProgress(0.05 * Minecraft.getInstance().getTimer().getRealtimeDeltaTicks(), diffTracker);
        this.backgroundPanning.add(diffTracker.mul(this.zoom));

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        this.stampScreen.renderScreen(guiGraphics, mouseX, mouseY, partialTick);

        final Vec2 mouse = Helper.preciseMousePos();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(mouse.x % 1, mouse.y % 1, 0);
        MapwrightClient.TOOL_MANAGER.get().renderScreen(guiGraphics, mouseX, mouseY);
        guiGraphics.pose().popPose();
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
        final boolean result = super.mouseReleased(mouseX, mouseY, button);
        this.mapWidget.mouseType = MapWidget.MouseType.NONE;
        return result;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        StampBagScreen.INSTANCE.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (!this.stampScreen.hasAnyTextBoxFoxused()) {
            if (Services.KEY_MAPPINGS.matches(IKeyMappings.Normal.OPEN_MAP, keyCode, scanCode, modifiers)) {
                this.onClose();
                return true;
            }

            if (Services.KEY_MAPPINGS.matches(IKeyMappings.Normal.SWAP, keyCode, scanCode, modifiers)) {
                MapwrightClient.TOOL_MANAGER.swap();
            }

            if (Services.KEY_MAPPINGS.matches(IKeyMappings.Normal.UNDO, keyCode, scanCode, modifiers)) {
                MapwrightClient.PAGE_MANAGER.undoChanges();
            }

            if (Services.KEY_MAPPINGS.matches(IKeyMappings.Normal.REDO, keyCode, scanCode, modifiers)) {
                MapwrightClient.PAGE_MANAGER.redoChanges();
            }

            switch (Services.KEY_MAPPINGS.getToolSwap(keyCode, scanCode, modifiers)) {
                case HAND -> this.toolPicker.pickHand();
                case BRUSH -> this.toolPicker.pickBrush();
                case PEN -> this.toolPicker.pickPen();
                case ERASER -> this.toolPicker.pickEraser();
                case null -> {
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.minecraft.level.playLocalSound(this.Minecraft.getInstance().player, SoundEvents.BOOK_PUT, SoundSource.MASTER, 1f, 0.5f);
        StampBagScreen.INSTANCE.changeStage(StampBagScreen.ScreenState.IDLE);
        this.stampScreen.parentClose();
        MapwrightClient.PAGE_MANAGER.getSpyglassPins().clear();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public float getScale() {
        return this.zoom;
    }

    //all these have been changed to public for usages in StampBagScreen
    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(final T widget) {
        return super.addRenderableWidget(widget);
    }

    @Override
    public <T extends GuiEventListener & NarratableEntry> T addWidget(final T listener) {
        return super.addWidget(listener);
    }

    @Override
    public void removeWidget(final GuiEventListener listener) {
        super.removeWidget(listener);
    }

    @Override
    public void clearWidgets() {
        super.clearWidgets();
    }

    @Override
    public void rebuildWidgets() {
        super.rebuildWidgets();
    }
}
