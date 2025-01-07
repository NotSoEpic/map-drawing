package beeisyou.mapdrawing.mapmanager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class MapManager {
    public final HashMap<Vector2i, MapRegion> regions = new HashMap<>();
    private int leftPad = 50;
    private int topPad = 30;
    public double width = 300;
    public double height = 200;
    public Vector2d panning = new Vector2d();
    private int scaleNum = 0;
    public double scale = 1;
    private static final Function<Identifier, RenderLayer> GUI_BILINEAR = Util.memoize(
            texture -> RenderLayer.of(
                    "gui_bilinear",
                    VertexFormats.POSITION_TEXTURE_COLOR,
                    VertexFormat.DrawMode.QUADS,
                    786432,
                    RenderLayer.MultiPhaseParameters.builder()
                            .texture(new RenderPhase.Texture(texture, TriState.TRUE, false))
                            .program(RenderPhase.POSITION_TEXTURE_COLOR_PROGRAM)
                            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                            .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
                            .build(false)
            )
    );
    public static RenderLayer getGuiBilinear(Identifier texture) {
        return GUI_BILINEAR.apply(texture);
    }
    public MapManager() {}

    public boolean contains(int rx, int rz) {
        return regions.containsKey(new Vector2i(rx, rz));
    }
    public MapRegion getOrCreate(int rx, int rz) {
        return regions.computeIfAbsent(new Vector2i(rx, rz), v -> new MapRegion(v.x, v.y));
    }

    public void doIfPresent(int rx, int rz, Consumer<MapRegion> callback) {
        if (regions.containsKey(new Vector2i(rx, rz))) {
            callback.accept(regions.get(new Vector2i(rx, rz)));
        }
    }

    public Vector2d screenToWorld(double x, double z) {
        return (new Vector2d(x, z).sub(width / 2, height / 2).add(panning)).div(scale);
    }

    public Vector2d worldToScreen(double x, double z) {
        return new Vector2d(x, z).mul(scale).add(width / 2, height / 2).sub(panning);
    }

    public void putPixelScreen(int x, int z, int r, int color) {
        Vector2d pos = screenToWorld(x - leftPad, z - topPad);
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
        Vector2d pos0 = screenToWorld(x0 - leftPad, z0 - topPad);
        Vector2d pos1 = screenToWorld(x1 - leftPad, z1 - topPad);
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

    public void render(DrawContext context, int screenWidth, int screenHeight, double mouseX, double mouseY) {
        width = (screenWidth - leftPad * 2);
        height = (screenHeight - topPad * 2);

        context.getMatrices().push();
        context.getMatrices().translate(leftPad, topPad, 0);

        Vector2d ul = screenToWorld(0, 0).div(512).floor();
        Vector2d lr = screenToWorld(width, height).div(512).ceil();
        for (int i = (int) ul.x; i < (int) lr.x; i++) {
            for (int j = (int) ul.y; j < (int) lr.y; j++) {
                doIfPresent(i, j, r -> renderRegion(context, r));
            }
        }
        context.drawVerticalLine(0, 0, (int) height, ColorHelper.getArgb(255,0,0));
        context.drawVerticalLine((int) width, 0, (int) height, ColorHelper.getArgb(255,0,0));
        context.drawHorizontalLine(0, (int) width, 0, ColorHelper.getArgb(255,0,0));
        context.drawHorizontalLine(0, (int) width, (int) height, ColorHelper.getArgb(255,0,0));
        Vector2d cursorWorld = screenToWorld(mouseX - leftPad, mouseY - topPad);
        context.drawText(MinecraftClient.getInstance().textRenderer, String.format("%d, %d", (int)cursorWorld.x, (int)cursorWorld.y),
                0, (int) (height + 10), ColorHelper.getArgb(0, 0, 255), false);
        context.drawText(MinecraftClient.getInstance().textRenderer, String.format("%d, %d", (int)panning.x, (int)panning.y),
                100, (int) (height + 10), ColorHelper.getArgb(0, 0, 255), false);
        Vector2d player = worldToScreen(MinecraftClient.getInstance().player.getX(), MinecraftClient.getInstance().player.getZ())
                .min(new Vector2d(width, height)).max(new Vector2d());
        context.fill((int) player.x - 5, (int) player.y - 5, (int) player.x + 5, (int) player.y + 5,
                ColorHelper.getArgb(255, 255, 0));

        context.getMatrices().pop();
    }

    // todo: specific floating point issue on boundary
    private void renderRegion(DrawContext context, MapRegion r) {
        Vector2d ul = worldToScreen(r.rx * 512, r.rz * 512).max(new Vector2d());
        Vector2d lr = worldToScreen(r.rx * 512 + 512, r.rz * 512 + 512).min(new Vector2d(width, height));
        if (ul.x > width || ul.y > height || lr.x < 0 || lr.y < 0)
            return;
        r.checkDirty();
        Vector2d uv = worldToScreen(r.rx * 512, r.rz * 512).sub(ul).mul(-1);
        Vector2d wh = new Vector2d(lr).sub(ul);
        context.drawTexture(RenderLayer::getGuiTextured, r.id,
                (int) ul.x, (int) ul.y,
                (float) uv.x, (float) uv.y,
                (int) wh.x, (int) wh.y,
                (int) (512 * scale), (int) (512 * scale));
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
        Vector2d mouseWorld = screenToWorld(mouseX - leftPad, mouseY - topPad);
        setScale(num);
        centerWorld(mouseWorld.x, mouseWorld.y);
        pan(leftPad + width / 2 - mouseX, topPad + height / 2 - mouseY);
    }

    public void deltaScale(int num, double mouseX, double mouseY) {
        setScale(MathHelper.clamp(scaleNum + num, -3, 1), mouseX, mouseY);
    }
}
