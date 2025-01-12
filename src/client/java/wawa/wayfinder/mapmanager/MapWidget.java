package wawa.wayfinder.mapmanager;

import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.RenderHelper;
import wawa.wayfinder.color.ColorPalette;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The map, rendering drawn regions and player position
 */
public class MapWidget extends AbstractWidget {
    public static final ResourceLocation GRID_TEXTURE = Wayfinder.id("textures/gui/grid.png");
    private final MapScreen parent;
    public final MapRegions regions = WayfinderClient.regions;
    public Vector2d panning = new Vector2d();
    private int scaleNum = 0;
    public double scale = 1;

    public MapWidget(MapScreen parent, int leftPad, int topPad, int width, int height) {
        super(leftPad, topPad, width, height, Component.nullToEmpty("map"));
        this.parent = parent;
    }

    public AbstractMapWidgetRegion getOrLoad(int rx, int rz) {
        if (regions.contains(rx, rz)) {
            return regions.get(rx, rz);
        } else {
            UnloadedMapWidgetRegion region = new UnloadedMapWidgetRegion(rx, rz, regions);
            region.tryLoadRegion();
            regions.put(rx, rz, region);
            return region;
        }
    }

    public Vector2d screenToWorld(double x, double z) {
        return (new Vector2d(x, z).sub(width / 2, height / 2).add(panning)).div(scale);
    }

    public Vector2d worldToScreen(double x, double z, boolean round) {
       Vector2d vec = new Vector2d(x, z).mul(scale);
       if (round) {
           vec.round();
       }
       vec.add(width / 2, height / 2).sub(panning);
       if (round) {
           vec.round();
       }
       return vec;
    }

    public Vector2d worldToScreen(double x, double z) {
        return worldToScreen(x, z, false);
    }

    public void putPixelScreen(int x, int z, int r, int color) {
        Vector2d pos = screenToWorld(x - getX(), z - getY());
        putPixelWorld((int) pos.x, (int) pos.y, r, color, false);
    }

    public void putPixelWorld(int x, int z, int r, int color, boolean highlight) {
        for (int i = 1-r; i < r; i++) {
            for (int j = 1-r; j < r; j++) {
                int rx = Math.floorDiv(x + i, 512);
                int rz = Math.floorDiv(z + j, 512);
                AbstractMapWidgetRegion region = getOrLoad(rx, rz);
                region.putPixelWorld(x + i, z + j, color, highlight);
            }
        }
    }

    public void drawLineScreen(double x0, double z0, double x1, double z1, int color, int size, boolean highlight) {
        Vector2d pos0 = screenToWorld(x0 - getX(), z0 - getY());
        Vector2d pos1 = screenToWorld(x1 - getX(), z1 - getY());
        WayfinderClient.lastDrawnPos = pos1;
        drawLineWorld(pos0.x, pos0.y, pos1.x, pos1.y, color, size, highlight);
    }

    public void drawLineWorld(double x0, double z0, double x1, double z1, int color, int size, boolean highlight) {
        double dx = x0 - x1;
        double dz = z0 - z1;
        int steps = (int) Math.max(1, Math.ceil(Math.max(Math.abs(dx), Math.abs(dz))));
        dx /= steps;
        dz /= steps;
        double x = x1;
        double z = z1;
        for (int i = 0; i < steps + 1; i++) {
            putPixelWorld((int) Math.floor(x), (int) Math.floor(z), size, color, highlight);
            x += dx;
            z += dz;
        }
    }

    enum MouseButton {
        NONE, LEFT, RIGHT, MIDDLE
    }
    MouseButton mouseButton = MouseButton.NONE;
    double prevX;
    double prevY;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (active && visible && isMouseOver(mouseX, mouseY)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                mouseButton = MouseButton.LEFT;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                mouseButton = MouseButton.RIGHT;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                mouseButton = MouseButton.MIDDLE;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        mouseButton = MouseButton.NONE;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        deltaScale((int)verticalAmount, mouseX, mouseY);
        return true;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.pose().pushPose();
        context.pose().translate(getX(), getY(), 0);

        Vector2d mouse = RenderHelper.smootherMouse();
        handleMouse(context, mouse);
        drawRegions(context);
        drawDebugText(context, mouse);
        drawMouse(context, mouse);
        drawPlayer(context);

        context.pose().popPose();
    }

    private void handleMouse(GuiGraphics context, Vector2d mouse) {
        if (!isHovered && mouseButton != MouseButton.RIGHT)
            mouseButton = MouseButton.NONE;

        Color color = ColorPalette.GRAYSCALE.colors().get(WayfinderClient.penColorIndex);
        int penColor = color.getRGB();

        boolean shift = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
        if (shift && WayfinderClient.lastDrawnPos != null) {
            Vector2d mouseWorld = screenToWorld(mouse.x - getX(), mouse.y - getY());
            switch (mouseButton) {
                case LEFT -> {
                    drawLineWorld(WayfinderClient.lastDrawnPos.x, WayfinderClient.lastDrawnPos.y, mouseWorld.x, mouseWorld.y,
                            penColor, WayfinderClient.penSize, WayfinderClient.highlight);
                    WayfinderClient.lastDrawnPos = mouseWorld;
                }
                case RIGHT -> {
                    drawLineWorld(WayfinderClient.lastDrawnPos.x, WayfinderClient.lastDrawnPos.y, mouseWorld.x, mouseWorld.y,
                            0, WayfinderClient.penSize, false);
                    WayfinderClient.lastDrawnPos = mouseWorld;
                }
                case MIDDLE -> pan(prevX - mouse.x, prevY - mouse.y);
            }
        } else {
            switch (mouseButton) {
                case LEFT -> drawLineScreen(prevX, prevY, mouse.x, mouse.y, penColor, WayfinderClient.penSize, WayfinderClient.highlight);
                case RIGHT -> drawLineScreen(prevX, prevY, mouse.x, mouse.y, 0, WayfinderClient.penSize, false);
                case MIDDLE -> pan(prevX - mouse.x, prevY - mouse.y);
            }
        }
        prevX = mouse.x;
        prevY = mouse.y;
    }
    private void drawRegions(GuiGraphics context) {
        context.blit(GRID_TEXTURE, 0, 0,
                Math.floorMod((int)Math.round(panning.x), 16), Math.floorMod((int)Math.round(panning.y), 16), width, height, 16, 16, -1);

        Vector2d ul = screenToWorld(0, 0).div(512).floor();
        Vector2d lr = screenToWorld(width, height).div(512).ceil();

        for (int i = (int) ul.x; i < (int) lr.x; i++) {
            for (int j = (int) ul.y; j < (int) lr.y; j++) {
                AbstractMapWidgetRegion region = getOrLoad(i, j);
                region.render(context, this);
            }
        }


        context.vLine(0, 0, height, Color.RED.getRGB());
        context.vLine(width, 0, height, Color.RED.getRGB());
        context.hLine(0, width, 0, Color.RED.getRGB());
        context.hLine(0, width, height, Color.RED.getRGB());
    }

    private void drawDebugText(GuiGraphics context, Vector2d mouse) {
        Vector2d cursorWorld = screenToWorld(mouse.x - getX(), mouse.y - getY());
        RenderHelper.badDebugText(context, 0, height + 2, String.format("%d, %d", (int)cursorWorld.x, (int)cursorWorld.y));
        RenderHelper.badDebugText(context, 100, height + 2, String.format("%d, %d", (int)panning.x, (int)panning.y));
        RenderHelper.badDebugText(context, 200, height + 2, String.format("%d (%d / %d)",
                regions.getLoaded() + regions.getUnloaded(), regions.getLoaded(), regions.getUnloaded()));
        RenderHelper.badDebugText(context, 0, height + 12, regions.getRegionPath().toString());
    }

    private void drawMouse(GuiGraphics context, Vector2d mouse) {

        Color color = WayfinderClient.palette.colors().get(WayfinderClient.penColorIndex);
        int penColor = color.getRGB();

        Vector2d ul = screenToWorld((int)mouse.x - getX(), (int)mouse.y - getY()).sub(WayfinderClient.penSize - 0.5, WayfinderClient.penSize - 0.5).round();
        Vector2d w = screenToWorld((int)mouse.x - getX(), (int)mouse.y - getY()).add(WayfinderClient.penSize - 0.5, WayfinderClient.penSize - 0.5).round();
        boolean shift = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
        if (shift && WayfinderClient.lastDrawnPos != null) {
            // todo: this is slightly off
            Vector2d oul = new Vector2d(WayfinderClient.lastDrawnPos).sub(WayfinderClient.penSize - 0.5, WayfinderClient.penSize - 0.5).round();
            double dx = oul.x - ul.x;
            double dz = oul.y - ul.y;
            int steps = (int) Math.max(1, Math.ceil(Math.max(Math.abs(dx), Math.abs(dz))));
            dx /= steps;
            dz /= steps;
            double x = ul.x;
            double z = ul.y;
            ul = worldToScreen(ul.x, ul.y, true);
            w = worldToScreen(w.x, w.y, true).sub(ul);
            for (int i = 0; i < steps + 1; i++) {
                Vector2d awawa = worldToScreen(Math.round(x), Math.round(z), true);
                context.fill((int) awawa.x, (int) awawa.y, (int) (awawa.x + w.x), (int) (awawa.y + w.y), penColor);
                x += dx;
                z += dz;
            }
        } else {
            ul = worldToScreen(ul.x, ul.y, true);
            w = worldToScreen(w.x, w.y, true).sub(ul);
            context.fill((int) Math.floor(ul.x), (int) Math.floor(ul.y), (int) Math.floor(ul.x + w.x), (int) Math.floor(ul.y + w.y), penColor);
        }
    }

    private void drawPlayer(GuiGraphics context) {
        Vector2d player = worldToScreen(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getZ(), true)
                .min(new Vector2d(width, height)).max(new Vector2d());
        RenderHelper.fill(context, player.x - 5, player.y - 5, player.x + 5, player.y + 5,
                Color.YELLOW.getRGB());

        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_ALT)) WayfinderClient.movementHistory.render(context, this);
    }

    public void applyLoadedRegion(UnloadedMapWidgetRegion unloaded, LoadedMapWidgetRegion loaded) {
        regions.put(unloaded.rx(), unloaded.rz(), loaded);
    }

    public void reset() {
        regions.clear();
        setScale(0);
        centerWorld(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getZ());
    }

    public void pan(double dx, double dz) {
        panning.add(dx, dz);
    }

    public void centerWorld(double x, double z) {
        panning.set(x * scale, z * scale);
    }

    public void setScale(int num) {
        scaleNum = num;
        scale = Math.pow(2, scaleNum);
    }

    public void setScale(int num, double mouseX, double mouseY) {
        Vector2d mouseWorld = screenToWorld(mouseX - getX(), mouseY - getY());
        setScale(num);
        centerWorld(mouseWorld.x, mouseWorld.y);
        pan(getX() + width / 2 - mouseX, getY() + height / 2 - mouseY);
    }

    public void deltaScale(int num, double mouseX, double mouseY) {
        setScale(Mth.clamp(scaleNum + num, -3, 2), mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
