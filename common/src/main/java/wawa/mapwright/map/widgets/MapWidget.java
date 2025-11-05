package wawa.mapwright.map.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.lwjgl.glfw.GLFW;
import wawa.mapwright.Helper;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.Rendering;
import wawa.mapwright.data.Pin;
import wawa.mapwright.data.SpyglassPins;
import wawa.mapwright.map.MapScreen;
import wawa.mapwright.map.background.MapBackground;
import wawa.mapwright.map.tool.PanTool;
import wawa.mapwright.map.tool.PinTool;

public class MapWidget extends AbstractWidget {
    private static final int OUTER_PADDING = 30;
    private static final int INNER_PADDING = 10;
    private static final int CORNER_SIZE = 8;
    private final MapScreen parent;
    private final MapBackground background;

    public MapWidget(final MapScreen parent) {
        super(OUTER_PADDING, OUTER_PADDING, parent.width - OUTER_PADDING * 2, parent.height - OUTER_PADDING * 2, Component.literal("Map Display"));
        this.background = new MapBackground(this.width - CORNER_SIZE * 2, this.height - CORNER_SIZE * 2);
        MapwrightClient.LOGGER.info("Size difference: {}, {}",
                this.width - this.background.getTrueWidth() - CORNER_SIZE * 2,
                this.height - this.background.getTrueHeight() - CORNER_SIZE * 2);
        this.setWidth(this.background.getTrueWidth() + CORNER_SIZE * 2);
        this.setHeight(this.background.getTrueHeight() + CORNER_SIZE * 2);
        this.setX((parent.width - this.width) / 2);
        this.setY((parent.height - this.height) / 2);
        this.parent = parent;
    }

    public MouseType mouseType = MouseType.NONE;
    public double oldMouseX;
    public double oldMouseY;
    public enum MouseType {
        NONE, LEFT, RIGHT, MIDDLE
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        final Vector2d panning = this.parent.lerpedPanning.get();
        final float scale = this.parent.getScale();

        this.background.render(guiGraphics, this.getX(), this.getY(),
                CORNER_SIZE, this.width, this.height,
                this.parent.backgroundPanning, -1
        );

        guiGraphics.pose().pushPose();
        final double hw = this.width / 2d;
        final double hh = this.height / 2d;
        guiGraphics.pose().translate(this.getX() + hw, this.getY() + hh, 0);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1);
        // offset for rendering elements relative to the world, prevents floating point imprecision at extreme values
        final double xOff = -panning.x;
        final double yOff = -panning.y;
        guiGraphics.enableScissor(this.getX() + INNER_PADDING, this.getY() + INNER_PADDING, this.getRight() - INNER_PADDING, this.getBottom() - INNER_PADDING);

        final Vector2d topLeftWorld = this.parent.screenToWorld(new Vector2d(this.getX(), this.getY()));
        final Vector2d bottomRightWorld = this.parent.screenToWorld(new Vector2d(this.getRight(), this.getBottom()));
        final Vector2i topLeft = new Vector2i(topLeftWorld.x / MapwrightClient.CHUNK_SIZE, topLeftWorld.y / MapwrightClient.CHUNK_SIZE, RoundingMode.FLOOR);
        final Vector2i bottomRight = new Vector2i(bottomRightWorld.x / MapwrightClient.CHUNK_SIZE, bottomRightWorld.y / MapwrightClient.CHUNK_SIZE, RoundingMode.CEILING);

        for (int x = topLeft.x; x < bottomRight.x; x++) {
            for (int y = topLeft.y; y < bottomRight.y; y++) {
                MapwrightClient.PAGE_MANAGER.getOrCreatePage(x, y).render(guiGraphics, xOff, yOff);
            }
        }

        final Vec2 mouse = Helper.preciseMousePos();
        final Vector2d world = this.parent.screenToWorld(new Vector2d(mouse.x, mouse.y));

        MapwrightClient.TOOL_MANAGER.get().renderWorld(guiGraphics, Mth.floor(world.x), Mth.floor(world.y), xOff, yOff);

        guiGraphics.disableScissor();

        guiGraphics.pose().popPose();

        final Vector2dc mouseScreen = new Vector2d(mouseX - this.getX() - hw, mouseY - this.getY() - hh);
        final Vector4dc transformedScreenBounds = new Vector4d(
                -hw + INNER_PADDING, -hh + INNER_PADDING, this.width - hw - INNER_PADDING, this.height - hh - INNER_PADDING
        );

        for (final Pin pin : MapwrightClient.PAGE_MANAGER.getPins()) {
            boolean highlight = false;
            if (MapwrightClient.TOOL_MANAGER.get() instanceof final PinTool pinTool) {
                highlight = pinTool.currentPin == pin.type;
            }
            pin.draw(guiGraphics, mouseScreen, xOff, yOff, scale, highlight, transformedScreenBounds);
        }

        for (final SpyglassPins.PinData pin : MapwrightClient.PAGE_MANAGER.getSpyglassPins().getPins()) {
            pin.pin().draw(guiGraphics, mouseScreen, xOff, yOff, scale, false, transformedScreenBounds);
        }

        final Vec3 playerPos = Minecraft.getInstance().player.position();
        Rendering.renderHead(guiGraphics, new Vector2d(playerPos.x, playerPos.z), mouseScreen, xOff, yOff, scale, transformedScreenBounds);

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scrollX, final double scrollY) {
        if (Screen.hasControlDown()) {
            MapwrightClient.TOOL_MANAGER.get().controlScroll(MapwrightClient.PAGE_MANAGER, mouseX, mouseY, scrollY);
        } else {
            this.parent.deltaZoom((int)scrollY);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            this.oldMouseX = mouseX;
            this.oldMouseY = mouseY;
            MouseType newType = switch (button) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> MouseType.LEFT;
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> MouseType.RIGHT;
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> MouseType.MIDDLE;
                default -> MouseType.NONE;
            };
            final Vector2d world = this.parent.screenToWorld(new Vector2d(mouseX, mouseY));
            if (newType != this.mouseType && this.mouseType != MouseType.NONE) {
                MapwrightClient.TOOL_MANAGER.get().mouseRelease(MapwrightClient.PAGE_MANAGER, this.mouseType, world);
            }
            this.mouseType = newType;
            if (Screen.hasControlDown()) {
                // noop
            } else if (Screen.hasControlDown()) {
                final int color = MapwrightClient.PAGE_MANAGER.getPixelARGB(Mth.floor(world.x), Mth.floor(world.y));
                this.parent.toolPicker.pickColor(color);
            } else if (this.mouseType == MouseType.LEFT || this.mouseType == MouseType.RIGHT) {
                MapwrightClient.TOOL_MANAGER.get().mouseDown(MapwrightClient.PAGE_MANAGER, this.mouseType, world);
                MapwrightClient.TOOL_MANAGER.get().mouseMove(MapwrightClient.PAGE_MANAGER, this.mouseType, world, world);
            }
            return true;
        }
        return false;
    }

    private boolean shouldPan() {
        if (this.mouseType == MouseType.MIDDLE) {
            return true;
        }
        if (this.mouseType == MouseType.LEFT || this.mouseType == MouseType.RIGHT) {
            return Screen.hasControlDown() || MapwrightClient.TOOL_MANAGER.get() instanceof PanTool;
        }
        return false;
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        final Vector2d world = this.parent.screenToWorld(new Vector2d(mouseX, mouseY));
        final Vector2d oldWorld = this.parent.screenToWorld(new Vector2d(this.oldMouseX, this.oldMouseY));
        if (this.shouldPan()) {
            this.parent.lerpedPanning.set(this.parent.lerpedPanning.get().add(oldWorld).sub(world));
            this.parent.backgroundPanning.add(this.oldMouseX, this.oldMouseY).sub(mouseX, mouseY);
        } else if (!this.isMouseOver(mouseX, mouseY)) {
            this.mouseType = MouseType.NONE;
            return;
        } else if (!Screen.hasAltDown() && !Screen.hasControlDown()) {
            MapwrightClient.TOOL_MANAGER.get().mouseMove(MapwrightClient.PAGE_MANAGER, this.mouseType, oldWorld, world);
        }
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        final Vector2d world = this.parent.screenToWorld(new Vector2d(mouseX, mouseY));
        if (this.mouseType !=  MouseType.NONE) {
            MapwrightClient.TOOL_MANAGER.get().mouseRelease(MapwrightClient.PAGE_MANAGER, this.mouseType, world);
        }

        this.mouseType = MouseType.NONE;
        return true;
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {}
}
