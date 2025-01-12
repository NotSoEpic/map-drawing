package wawa.wayfinder.mapmanager;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.RenderHelper;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.mapmanager.tools.Tool;

import java.util.function.BiFunction;

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

    public Vector2d worldToScreen(Vector2d v, boolean round) {
        return worldToScreen(v.x, v.y, true);
    }

    public void putPixelWorld(int x, int z, int r, int color) {
        putPixelWorld(x, z, r, color, (pixel, current) -> pixel);
    }

    public void putPixelWorld(int x, int z, int r, int color, BiFunction<Integer, Integer, Integer> map) {
        for (int i = 1-r; i < r; i++) {
            for (int j = 1-r; j < r; j++) {
                int rx = Math.floorDiv(x + i, 512);
                int rz = Math.floorDiv(z + j, 512);
                AbstractMapWidgetRegion region = getOrLoad(rx, rz);
                int current = region.getPixelWorld(x + i, z + j);
                region.putPixelWorld(x + i, z + j, map.apply(color, current));
            }
        }
    }

    public int getPixelWorld(int x, int z) {
        int rx = Math.floorDiv(x, 512);
        int rz = Math.floorDiv(z, 512);
        AbstractMapWidgetRegion region = getOrLoad(rx, rz);
        return region.getPixelWorld(x, z);
    }

    public void putTextureWorld(int x, int z, NativeImage pixels) {
        putTextureWorld(x, z, pixels, (pixel, current) -> pixel);
    }

    // map: (texture, current) -> result
    public void putTextureWorld(int x, int z, NativeImage pixels, BiFunction<Integer, Integer, Integer> map) {
        int w = pixels.getWidth();
        int h = pixels.getHeight();
        x -= w/2;
        z -= h/2;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int pixel = pixels.getPixel(i, j);
                if (pixel != 0) {
                    putPixelWorld(x + i, z + j, 1, pixel, map);
                }
            }
        }
    }

    public void drawLineScreen(double x0, double z0, double x1, double z1, int color, int size, BiFunction<Integer, Integer, Integer> map) {
        Vector2i pos0 = new Vector2i(screenToWorld(x0 - getX(), z0 - getY()), RoundingMode.FLOOR);
        Vector2i pos1 = new Vector2i(screenToWorld(x1 - getX(), z1 - getY()), RoundingMode.FLOOR);
        WayfinderClient.lastDrawnPos = pos1;
        drawLineWorld(pos0.x, pos0.y, pos1.x, pos1.y, color, size, map);
    }

    public void drawLineWorld(double x0, double z0, double x1, double z1, int color, int size, BiFunction<Integer, Integer, Integer> map) {
        double dx = x0 - x1;
        double dz = z0 - z1;
        int steps = (int) Math.max(1, Math.ceil(Math.max(Math.abs(dx), Math.abs(dz))));
        dx /= steps;
        dz /= steps;
        double x = x1;
        double z = z1;
        for (int i = 0; i < steps + 1; i++) {
            putPixelWorld((int) Math.floor(x + 0.5), (int) Math.floor(z + 0.5), size, color, map);
            x += dx;
            z += dz;
        }
    }

    enum MouseButton {
        NONE, LEFT, RIGHT, MIDDLE
    }
    MouseButton mouseButton = MouseButton.NONE;
    public double prevX;
    public double prevY;

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
            if (Tool.get() != null) {
                Vector2d mouse = new Vector2d(mouseX, mouseY);
                Vector2i world = new Vector2i(screenToWorld(mouse.x - getX(), mouse.y - getY()), RoundingMode.FLOOR);
                boolean shift = Screen.hasShiftDown();
                switch (mouseButton) {
                    case LEFT -> Tool.get().leftDown(this, shift, mouse, world);
                    case RIGHT -> Tool.get().rightDown(this, shift, mouse, world);
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (Tool.get() != null) {
            Vector2d mouse = new Vector2d(mouseX, mouseY);
            Vector2i world = new Vector2i(screenToWorld(mouse.x - getX(), mouse.y - getY()), RoundingMode.FLOOR);
            boolean shift = Screen.hasShiftDown();
            switch (mouseButton) {
                case LEFT -> Tool.get().leftUp(this, shift, mouse, world);
                case RIGHT -> Tool.get().rightUp(this, shift, mouse, world);
            }
        }
        mouseButton = MouseButton.NONE;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (Tool.get() != null && Screen.hasControlDown()) {
            Vector2d mouse = new Vector2d(mouseX, mouseY);
            Vector2i world = new Vector2i(screenToWorld(mouse.x - getX(), mouse.y - getY()), RoundingMode.FLOOR);
            Tool.get().ctrlScroll(this, mouse, world, verticalAmount);
        } else {
            deltaScale((int) verticalAmount, mouseX, mouseY);
        }
        return true;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.pose().pushPose();
        context.pose().translate(getX(), getY(), 0);

        Vector2d mouse = RenderHelper.smootherMouse();
        handleMouse(mouse);
        context.enableScissor(0, 0, width + 1, height + 1);
        drawRegions(context);
        drawPlayer(context);
        drawMouse(context, mouse);
        context.disableScissor();
        drawDebugText(context, mouse);

        context.pose().popPose();
    }

    private void handleMouse(Vector2d mouse) {
        if (!isHovered && mouseButton != MouseButton.RIGHT)
            mouseButton = MouseButton.NONE;

        Vector2i world = new Vector2i(screenToWorld(mouse.x - getX(), mouse.y - getY()), RoundingMode.FLOOR);

        if (Tool.get() != null) {
            boolean shift = Screen.hasShiftDown();
            switch (mouseButton) {
                case LEFT -> Tool.get().leftHold(this, shift, mouse, world);
                case RIGHT -> Tool.get().rightHold(this, shift, mouse, world);
                case MIDDLE -> pan(prevX - mouse.x, prevY - mouse.y);
            }
        } else if (mouseButton == MouseButton.MIDDLE) {
            pan(prevX - mouse.x, prevY - mouse.y);
        }

        prevX = mouse.x;
        prevY = mouse.y;
    }

    private void drawRegions(GuiGraphics context) {
        context.blit(RenderType::guiTextured, GRID_TEXTURE, 0, 0,
                Math.floorMod((int)Math.round(panning.x), 16), Math.floorMod((int)Math.round(panning.y), 16), width, height, 16, 16, -1);

        Vector2d ul = screenToWorld(0, 0).div(512).floor();
        Vector2d lr = screenToWorld(width, height).div(512).ceil();

        for (int i = (int) ul.x; i < (int) lr.x; i++) {
            for (int j = (int) ul.y; j < (int) lr.y; j++) {
                AbstractMapWidgetRegion region = getOrLoad(i, j);
                region.render(context, this);
            }
        }

        context.vLine(0, 0, height, ARGB.color(255,0,0));
        context.vLine(width, 0, height, ARGB.color(255,0,0));
        context.hLine(0, width, 0, ARGB.color(255,0,0));
        context.hLine(0, width, height, ARGB.color(255,0,0));
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
        Window window = Minecraft.getInstance().getWindow();
        if (Tool.get() == null) {
            GLFW.glfwSetInputMode(window.getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            return;
        }

        Vector2i world = new Vector2i(screenToWorld(mouse.x - getX(), mouse.y - getY()), RoundingMode.FLOOR);

        if (isMouseOver(mouse.x, mouse.y) && Tool.get().hideMouse(this, mouse, world)) {
            GLFW.glfwSetInputMode(window.getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        } else {
            GLFW.glfwSetInputMode(window.getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }

        boolean shift = Screen.hasShiftDown();
        Tool.get().render(this, context, shift, mouse, world);
    }

    private void drawPlayer(GuiGraphics context) {
        Vector2d player = worldToScreen(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getZ(), true)
                .min(new Vector2d(width, height)).max(new Vector2d());
        RenderHelper.fill(context, player.x - 5, player.y - 5, player.x + 5, player.y + 5,
                ARGB.color(255, 255, 0));

        if (Screen.hasAltDown()) WayfinderClient.movementHistory.render(context, this);
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
