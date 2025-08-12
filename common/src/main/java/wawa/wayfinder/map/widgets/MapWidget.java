package wawa.wayfinder.map.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.Rendering;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.Pin;
import wawa.wayfinder.map.MapScreen;
import wawa.wayfinder.map.tool.PinTool;
import wawa.wayfinder.map.tool.Tool;

public class MapWidget extends AbstractWidget {
    private static final int OUTER_PADDING = 30;
    private final MapScreen parent;

    public MapWidget(final MapScreen parent) {
        super(OUTER_PADDING, OUTER_PADDING, parent.width - OUTER_PADDING * 2, parent.height - OUTER_PADDING * 2, Component.literal("Map Display"));
        this.parent = parent;
    }

    public Mouse mouse = Mouse.NONE;
    public double oldMouseX;
    public double oldMouseY;
    public enum Mouse {
        NONE, LEFT, RIGHT, MIDDLE
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        final Vector2d panning = this.parent.lerpedPanning.get();

        Rendering.renderMapNineslice(guiGraphics, this.getX(), this.getY(), this.width, this.height, -1, this.parent.backgroundPanning, this.parent.getZoom());

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.getX() + this.width / 2f, this.getY() + this.height / 2f, 0);
        guiGraphics.pose().scale(this.parent.getZoom(), this.parent.getZoom(), 1);
        // offset for rendering elements relative to the world, prevents floating point imprecision at extreme values
        final int xOff = -Math.floorDiv(Mth.floor(panning.x), 512) * 512;
        final int yOff = -Math.floorDiv(Mth.floor(panning.y), 512) * 512;
        guiGraphics.pose().translate(-panning.x - xOff, -panning.y - yOff, 0);
        guiGraphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());

        final Vector2i topLeft = new Vector2i(this.parent.screenToWorld(new Vector2d(this.getX(), this.getY())).div(512), RoundingMode.FLOOR);
        final Vector2i bottomRight = new Vector2i(this.parent.screenToWorld(new Vector2d(this.getRight(), this.getBottom())).div(512), RoundingMode.CEILING);

        for (int x = topLeft.x; x < bottomRight.x; x++) {
            for (int y = topLeft.y; y < bottomRight.y; y++) {
                WayfinderClient.PAGE_MANAGER.getOrCreatePage(x, y).render(guiGraphics, xOff, yOff);
            }
        }
        final Vector2d world = this.parent.screenToWorld(new Vector2d(mouseX, mouseY));

        WayfinderClient.POSITION_HISTORY.render(guiGraphics, xOff, yOff);

        if (Tool.get() != null) {
            Tool.get().renderWorld(guiGraphics, Mth.floor(world.x), Mth.floor(world.y), xOff, yOff);
        }

        for (final Pin pin : WayfinderClient.PAGE_MANAGER.getPins()) {
            boolean highlight = false;
            if (Tool.get() instanceof final PinTool pinTool) {
                highlight = pinTool.currentPin == pin.type;
            }
            pin.draw(guiGraphics, xOff, yOff, highlight);
        }

        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scrollX, final double scrollY) {
        if (Tool.get() != null && Screen.hasControlDown()) {
            Tool.get().controlScroll(WayfinderClient.PAGE_MANAGER, mouseX, mouseY, scrollY);
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
            this.mouse = switch (button) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> Mouse.LEFT;
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> Mouse.RIGHT;
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> Mouse.MIDDLE;
                default -> Mouse.NONE;
            };
            final Vector2d world = this.parent.screenToWorld(new Vector2d(mouseX, mouseY));
            if ((this.mouse == Mouse.LEFT || this.mouse == Mouse.RIGHT) && Tool.get() != null && !Screen.hasAltDown()) {
                Tool.get().hold(WayfinderClient.PAGE_MANAGER, this.mouse, world, world);
            } else if (Screen.hasAltDown()) {
                final int color = WayfinderClient.PAGE_MANAGER.getPixel(Mth.floor(world.x), Mth.floor(world.y));
                this.parent.toolPicker.pickColor(color);
            }
            return true;
        }
        return false;
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        final Vector2d world = this.parent.screenToWorld(new Vector2d(mouseX, mouseY));
        final Vector2d oldWorld = this.parent.screenToWorld(new Vector2d(this.oldMouseX, this.oldMouseY));
        if (this.mouse == Mouse.MIDDLE) {
            this.parent.lerpedPanning.set(this.parent.lerpedPanning.get().add(oldWorld).sub(world));
            this.parent.backgroundPanning.add(this.oldMouseX, this.oldMouseY).sub(mouseX, mouseY);
        } else if (!this.isMouseOver(mouseX, mouseY)) {
            this.mouse = Mouse.NONE;
            return;
        }
        if (Tool.get() != null && !Screen.hasAltDown()) {
            Tool.get().hold(WayfinderClient.PAGE_MANAGER, this.mouse, oldWorld, world);
        }
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        final Tool tool = Tool.get();
        if (tool != null) {
            tool.release(WayfinderClient.PAGE_MANAGER);
        }

        this.mouse = Mouse.NONE;
        return true;
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {}
}
