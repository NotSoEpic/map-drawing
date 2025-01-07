package beeisyou.mapdrawing.mapmanager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.function.Function;

public class MapManager {
    public final HashMap<Vector2i, MapRegion> regions = new HashMap<>();
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

    public void putPixelAbs(int x, int z, int color) {
        int rx = Math.floorDiv(x, 512);
        int rz = Math.floorDiv(z, 512);
        MapRegion region = getOrCreate(rx, rz);
        region.putPixelAbs(x, z, color);
    }

    public void drawLine(double x0, double z0, double x1, double z1, int color) {
        double dx = x0 - x1;
        double dz = z0 - z1;
        int steps = (int) Math.max(1, Math.ceil(Math.max(Math.abs(dx), Math.abs(dz))));
        dx /= steps;
        dz /= steps;
        double x = x1;
        double z = z1;
        for (int i = 0; i < steps + 1; i++) {
            putPixelAbs((int)Math.floor(x), (int)Math.floor(z), color);
            x += dx;
            z += dz;
        }
    }

    public void clear() {
        regions.forEach((v, r) -> r.clear());
        regions.clear();
    }

    public void render(DrawContext context, double centerX, double centerZ, double width, double height, double scale, boolean bilinear) {
//        // min/max world block coordinates
//        double minX = centerX - width * 0.5 / scale;
//        double minZ = centerZ - height * 0.5 / scale;
//        double maxX = centerX + width * 0.5 / scale;
//        double maxZ = centerZ + height * 0.5 / scale;
//
//        for (int i = Math.floorDiv((int) minX, 512); i < Math.ceilDiv((int) maxX, 512); i++) {
//            for (int j = Math.floorDiv((int) minZ, 512); j < Math.ceilDiv((int) maxZ, 512); j++) {
//                if (contains(i, j)) {
//                    MapRegion region = getOrCreate(i, j);
//                    int left = Math.max((int)minX, i * 512);
//                    int top = Math.max((int)minZ, j * 512);
//                    int right = Math.min((int)maxX, i * 512 + 512);
//                    int bottom = Math.min((int)maxZ, j * 512 + 512);
//                    context.drawTexture(RenderLayer::getGuiTextured, region.id,
//                            (int)((left + centerX) * scale), (int)((top + centerZ) * scale), left - i * 512, top - j * 512,
//                            (int)((right - left) * scale), (int)((bottom - top) * scale), right - left, bottom - top);
//                    context.drawText(
//                            MinecraftClient.getInstance().textRenderer, region.id.getPath(),
//                            (int)((left + centerX) * scale), (int)((top + centerZ) * scale), ColorHelper.getArgb(255, 127, 127, 127), false);
//                }
//            }
//        }

        regions.forEach((v, r) -> {
            r.checkDirty();
            context.drawTexture(RenderLayer::getGuiTextured, r.id,
                    v.x * 512, v.y * 512, 0, 0, 512, 512, 512, 512);
            context.drawText(MinecraftClient.getInstance().textRenderer, r.id.getPath(), v.x * 512, v.y * 512, ColorHelper.getArgb(255, 127, 127, 127), false);
        });
    }
}
