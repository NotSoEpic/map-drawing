package wawa.wayfinder.map;

import com.mojang.blaze3d.platform.Window;
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
import wawa.wayfinder.Helper;
import wawa.wayfinder.LerpedVector2d;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.stamp_bag.widgets.StampBagWidget;
import wawa.wayfinder.map.widgets.CompassRoseWidget;
import wawa.wayfinder.map.widgets.DebugTextRenderable;
import wawa.wayfinder.map.widgets.MapWidget;
import wawa.wayfinder.map.widgets.ToolPickerWidget;
import wawa.wayfinder.platform.Services;
import wawa.wayfinder.platform.services.IKeyMappings;

import java.util.ArrayList;
import java.util.List;

public class MapScreen extends Screen {
    private int zoomNum = 0;
    private float zoom = 1f; // gui pixels per block (>1 zoom in, <1 zoom out)
    public LerpedVector2d lerpedPanning; // world space coordinate to center on
    public Vector2d backgroundPanning = new Vector2d(); // panning irrespective of zoom
    private MapWidget mapWidget;
    public ToolPickerWidget toolPicker;
    public CompassRoseWidget compassRose;

    public List<AbstractWidget> allWidgets = new ArrayList<>();

    public StampBagWidget stampBag;

    public StampBagScreen stampScreen;

    public static boolean cursorAdjusted;

    public MapScreen(final Vector2d openingPos, final Vector2d endingPos) {
        super(Component.literal("Wayfinder Map"));
        this.lerpedPanning = new LerpedVector2d(openingPos, endingPos);
        cursorAdjusted = false;
    }

    @Override
    protected void init() {
        super.init();
        this.mapWidget = new MapWidget(this);
        allWidgets.add(mapWidget);
        this.addRenderableWidget(this.mapWidget);

        int toolX = this.width - 15 - 16 / 2;
        this.toolPicker = new ToolPickerWidget(toolX, 30);
        allWidgets.add(toolPicker);
        this.addRenderableWidget(this.toolPicker);

        this.stampBag = new StampBagWidget(toolX, toolPicker.finalToolY + 20, this);
        allWidgets.add(stampBag);
        addRenderableWidget(stampBag);

        this.compassRose = new CompassRoseWidget(this.width - 45, this.height - 45);
        this.addRenderableOnly(this.compassRose);
        this.addRenderableOnly(new DebugTextRenderable(this));
        GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
//        if (!cursorAdjusted) { // Init is run whenever the screen is resized & the cursor is set in the super of the constructor. its evil but it works
//            Window window = Minecraft.getInstance().getWindow();
//            GLFW.glfwSetCursorPos(window.getWindow(), window.getScreenWidth() / 2f, window.getScreenHeight() / 2.75f);
//            cursorAdjusted = true;
//        }

        stampScreen = StampBagScreen.INSTANCE;
        stampScreen.setMapScreen(this);
        stampScreen.changeStage(stampScreen.getState());
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        final Vector2d diffTracker = new Vector2d();
        this.lerpedPanning.tickProgress(0.05 * Minecraft.getInstance().getTimer().getRealtimeDeltaTicks(), diffTracker);
        this.backgroundPanning.add(diffTracker.mul(this.zoom));

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        stampScreen.renderScreen(guiGraphics, mouseX, mouseY, partialTick);

        final Vec2 mouse = Helper.preciseMousePos();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(mouse.x % 1, mouse.y % 1, 0);
        WayfinderClient.TOOL_MANAGER.get().renderScreen(guiGraphics, mouseX, mouseY);
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
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (!stampScreen.hasAnyTextBoxFoxused()) {
            if (Services.KEY_MAPPINGS.matches(IKeyMappings.Normal.OPEN_MAP, keyCode, scanCode, modifiers)) {
                this.onClose();
                return true;
            }

            if (Services.KEY_MAPPINGS.matches(IKeyMappings.Normal.SWAP, keyCode, scanCode, modifiers)) {
                WayfinderClient.TOOL_MANAGER.swap();
            }

            if (Services.KEY_MAPPINGS.matches(IKeyMappings.Normal.UNDO, keyCode, scanCode, modifiers)) {
                WayfinderClient.PAGE_MANAGER.undoChanges();
            }

            if (Services.KEY_MAPPINGS.matches(IKeyMappings.Normal.REDO, keyCode, scanCode, modifiers)) {
                WayfinderClient.PAGE_MANAGER.redoChanges();
            }

            switch (Services.KEY_MAPPINGS.getToolSwap(keyCode, scanCode, modifiers)) {
                case HAND -> this.toolPicker.pickHand();
                case BRUSH -> this.toolPicker.pickBrush();
                case PENCIL -> this.toolPicker.pickPencil();
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
        minecraft.level.playLocalSound(minecraft.getInstance().player, SoundEvents.BOOK_PUT, SoundSource.MASTER, 1f, 0.5f);
        stampScreen.parentClose();
        WayfinderClient.PAGE_MANAGER.getSpyglassPins().clear();
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
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }

    @Override
    public <T extends GuiEventListener & NarratableEntry> T addWidget(T listener) {
        return super.addWidget(listener);
    }

    @Override
    public void removeWidget(GuiEventListener listener) {
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
