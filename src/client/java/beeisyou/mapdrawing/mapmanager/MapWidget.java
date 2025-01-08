package beeisyou.mapdrawing.mapmanager;

import beeisyou.mapdrawing.MapDrawingClient;
import beeisyou.mapdrawing.MapRegions;
import beeisyou.mapdrawing.MapScreen;
import beeisyou.mapdrawing.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.function.Function;

public class MapWidget extends ClickableWidget {
    MapScreen parent;
    public final MapRegions regions = MapDrawingClient.regions;
    public Vector2d panning = new Vector2d();
    private int scaleNum = 0;
    public double scale = 1;

    public MapWidget(MapScreen parent, int leftPad, int topPad, int width, int height) {
        super(leftPad, topPad, width, height, Text.of("map"));
        this.parent = parent;
    }

    public MapRegion getOrCreate(int rx, int rz) {
        return regions.computeIfAbsent(new Vector2i(rx, rz), v -> new MapRegion(v.x, v.y));
    }

    public void doIfPresent(int rx, int rz, Function<MapRegion, Boolean> callback) {
        if (regions.containsKey(new Vector2i(rx, rz))) {
            if (callback.apply(regions.get(new Vector2i(rx, rz)))) {
                regions.remove(new Vector2i(rx, rz));
            }
        }
    }

    public Vector2d screenToWorld(double x, double z) {
        return (new Vector2d(x, z).sub(width / 2, height / 2).add(panning)).div(scale);
    }

    public Vector2d worldToScreen(double x, double z) {
        return new Vector2d(x, z).mul(scale).add(width / 2, height / 2).sub(panning);
    }

    public void putPixelScreen(int x, int z, int r, int color) {
        Vector2d pos = screenToWorld(x - getX(), z - getY());
        putPixelWorld((int) pos.x, (int) pos.y, r, color);
    }

    public void putPixelWorld(int x, int z, int r, int color) {
        for (int i = -r; i <= r; i++) {
            for (int j = -r; j <= r; j++) {
                int rx = Math.floorDiv(x + i, 512);
                int rz = Math.floorDiv(z + j, 512);
                MapRegion region = getOrCreate(rx, rz);
                region.putPixelWorld(x + i, z + j, color);
            }
        }
    }

    public void drawLineScreen(double x0, double z0, double x1, double z1, int color) {
        Vector2d pos0 = screenToWorld(x0 - getX(), z0 - getY());
        Vector2d pos1 = screenToWorld(x1 - getX(), z1 - getY());
        drawLineWorld(pos0.x, pos0.y, pos1.x, pos1.y, color);
    }

    public void drawLineWorld(double x0, double z0, double x1, double z1, int color) {
        double dx = x0 - x1;
        double dz = z0 - z1;
        int steps = (int) Math.max(1, Math.ceil(Math.max(Math.abs(dx), Math.abs(dz))));
        dx /= steps;
        dz /= steps;
        double x = x1;
        double z = z1;
        for (int i = 0; i < steps + 1; i++) {
            putPixelWorld((int) Math.floor(x), (int) Math.floor(z), 1, color);
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
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Vector2d mouse = RenderHelper.smootherMouse();
        if (!hovered && mouseButton != MouseButton.RIGHT)
            mouseButton = MouseButton.NONE;

        switch (mouseButton) {
            case LEFT -> drawLineScreen(prevX, prevY, mouse.x, mouse.y, parent.color);
            case RIGHT -> pan(prevX - mouse.x, prevY - mouse.y);
            case MIDDLE -> reset();
        }
        prevX = mouse.x;
        prevY = mouse.y;

        context.getMatrices().push();
        context.getMatrices().translate(getX(), getY(), 0);

        Vector2d ul = screenToWorld(0, 0).div(512).floor();
        Vector2d lr = screenToWorld(width, height).div(512).ceil();
        for (int i = (int) ul.x; i < (int) lr.x; i++) {
            for (int j = (int) ul.y; j < (int) lr.y; j++) {
                doIfPresent(i, j, r -> {
                    renderRegion(context, r);
                    return r.isRemoved();
                });
            }
        }

        context.drawVerticalLine(0, 0, height, ColorHelper.getArgb(255,0,0));
        context.drawVerticalLine(width, 0, height, ColorHelper.getArgb(255,0,0));
        context.drawHorizontalLine(0, width, 0, ColorHelper.getArgb(255,0,0));
        context.drawHorizontalLine(0, width, height, ColorHelper.getArgb(255,0,0));

        Vector2d cursorWorld = screenToWorld(mouse.x - getX(), mouse.y - getY());
        context.drawText(MinecraftClient.getInstance().textRenderer, String.format("%d, %d", (int)cursorWorld.x, (int)cursorWorld.y),
                0, height + 10, ColorHelper.getArgb(0, 0, 255), false);
        context.drawText(MinecraftClient.getInstance().textRenderer, String.format("%d, %d", (int)panning.x, (int)panning.y),
                100, height + 10, ColorHelper.getArgb(0, 0, 255), false);

        Vector2d player = worldToScreen(MinecraftClient.getInstance().player.getX(), MinecraftClient.getInstance().player.getZ())
                .min(new Vector2d(width, height)).max(new Vector2d()).round();
        RenderHelper.fill(context, player.x - 5, player.y - 5, player.x + 5, player.y + 5,
                ColorHelper.getArgb(255, 255, 0));

        MapDrawingClient.movementHistory.render(context, this);

        context.getMatrices().pop();
    }

    private void renderRegion(DrawContext context, MapRegion r) {
        Vector2d ul = worldToScreen(r.rx * 512, r.rz * 512).round().max(new Vector2d());
        Vector2d lr = worldToScreen(r.rx * 512 + 512, r.rz * 512 + 512).round().min(new Vector2d(width, height));
        if (ul.x > width || ul.y > height || lr.x < 0 || lr.y < 0)
            return;
        r.checkDirty();
        if (r.isRemoved())
            return;
        Vector2d uv = worldToScreen(r.rx * 512, r.rz * 512).round().sub(ul).mul(-1);
        Vector2d wh = new Vector2d(lr).sub(ul);
        RenderHelper.drawTexture(context, RenderLayer::getGuiTextured, r.id,
                ul.x, ul.y,
                (float) uv.x, (float) uv.y,
                wh.x, wh.y,
                (int) (512 * scale),(int) (512 * scale));
//        context.drawText(MinecraftClient.getInstance().textRenderer, r.id.toString(), (int) ul.x + 2, (int) ul.y + 2,
//                ColorHelper.getArgb(0, 0, 255), false);
    }

    public void reset() {
        regions.forEach((v, r) -> r.clear());
        regions.clear();
        setScale(0);
        centerWorld(MinecraftClient.getInstance().player.getX(), MinecraftClient.getInstance().player.getZ());
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
        setScale(MathHelper.clamp(scaleNum + num, -3, 1), mouseX, mouseY);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
