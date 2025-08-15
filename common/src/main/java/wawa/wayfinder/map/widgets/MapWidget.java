package wawa.wayfinder.map.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.Rendering;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.Pin;
import wawa.wayfinder.data.SpyglassPins;
import wawa.wayfinder.map.MapScreen;
import wawa.wayfinder.map.tool.PinTool;
import wawa.wayfinder.map.tool.Tool;

public class MapWidget extends AbstractWidget {
    private static final int OUTER_PADDING = 30;
    private static final int INNER_PADDING = 10;
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
        final float scale = this.parent.getScale();

        Rendering.renderMapNineslice(guiGraphics, this.getX(), this.getY(), this.width, this.height, -1, this.parent.backgroundPanning, scale);

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
        final Vector2i topLeft = new Vector2i(topLeftWorld.x / 512, topLeftWorld.y / 512, RoundingMode.FLOOR);
        final Vector2i bottomRight = new Vector2i(bottomRightWorld.x / 512, bottomRightWorld.y / 512, RoundingMode.CEILING);

        for (int x = topLeft.x; x < bottomRight.x; x++) {
            for (int y = topLeft.y; y < bottomRight.y; y++) {
                WayfinderClient.PAGE_MANAGER.getOrCreatePage(x, y).render(guiGraphics, xOff, yOff);
            }
        }
        final Vector2d world = this.parent.screenToWorld(new Vector2d(mouseX, mouseY));

        if (Tool.get() != null) {
            Tool.get().renderWorld(guiGraphics, Mth.floor(world.x), Mth.floor(world.y), xOff, yOff);
        }

        guiGraphics.disableScissor();

        guiGraphics.pose().popPose();

        final Vector2dc mouseScreen = new Vector2d(mouseX - this.getX() - hw, mouseY - this.getY() - hh);
        final Vector4dc transformedScreenBounds = new Vector4d(
                -hw + INNER_PADDING, -hh + INNER_PADDING, this.width - hw - INNER_PADDING, this.height - hh - INNER_PADDING
        );
        for (final Pin pin : WayfinderClient.PAGE_MANAGER.getPins()) {
            boolean highlight = false;
            if (Tool.get() instanceof final PinTool pinTool) {
                highlight = pinTool.currentPin == pin.type;
            }
            pin.draw(guiGraphics, mouseScreen, xOff, yOff, scale, highlight, transformedScreenBounds);
        }
        for (final SpyglassPins.PinData pin : WayfinderClient.PAGE_MANAGER.getSpyglassPins().getPins()) {
            pin.pin().draw(guiGraphics, mouseScreen, xOff, yOff, scale, false, transformedScreenBounds);
        }

        final Vec3 playerPos = Minecraft.getInstance().player.position();
        Rendering.renderHead(guiGraphics, new Vector2d(playerPos.x, playerPos.z), mouseScreen, xOff, yOff, scale, transformedScreenBounds);

        guiGraphics.pose().popPose();
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
            if ((this.mouse == Mouse.LEFT || this.mouse == Mouse.RIGHT) && Tool.get() != null && !Screen.hasAltDown() && !Screen.hasControlDown()) {
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
        if (this.mouse == Mouse.MIDDLE || (this.mouse == Mouse.LEFT && Screen.hasControlDown())) {
            this.parent.lerpedPanning.set(this.parent.lerpedPanning.get().add(oldWorld).sub(world));
            this.parent.backgroundPanning.add(this.oldMouseX, this.oldMouseY).sub(mouseX, mouseY);
        } else if (!this.isMouseOver(mouseX, mouseY)) {
            this.mouse = Mouse.NONE;
            return;
        }
        if (Tool.get() != null && !Screen.hasAltDown() && !Screen.hasControlDown()) {
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
