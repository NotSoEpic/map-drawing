package wawa.wayfinder.map.tool;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.MapScreen;

import java.util.function.Consumer;

public class DrawTool extends Tool {
    private int r = 0;
    private static final ResourceLocation id = WayfinderClient.id("draw");
    private static final DynamicTexture preview = new DynamicTexture(1, 1, false);
    static {
        // crashes the game if this class is loaded too early. boowomp
        preview.getPixels().setPixelRGBA(0, 0, -1);
        preview.upload();
        Minecraft.getInstance().getTextureManager().register(id, preview);
    }
    @Override
    public void hold(PageManager activePage, MapScreen.Mouse mouse, Vector2d oldWorld, Vector2d world) {
        switch (mouse) {
            case LEFT -> pixelLine(oldWorld.floor(), world.floor(), pos -> activePage.putSquare(pos.x, pos.y, -1, r));
            case RIGHT -> pixelLine(oldWorld.floor(), world.floor(), pos -> activePage.putSquare(pos.x, pos.y, 0, r));
        }
    }

    private void pixelLine(Vector2d point1, Vector2d point2, Consumer<Vector2i> perPixel) {
        Vector2d delta = new Vector2d(point1).sub(point2);
        int steps = (int) Math.max(1, Math.ceil(Math.max(Math.abs(delta.x), Math.abs(delta.y))));
        delta.div(steps);
        Vector2d pos = new Vector2d(point2);
        for (int i = 0; i < steps + 1; i++) {
            perPixel.accept(new Vector2i(pos.x + 0.5, pos.y + 0.5, RoundingMode.FLOOR));
            pos.add(delta);
        }
    }

    @Override
    public void controlScroll(PageManager activePage, double mouseX, double mouseY, double scrollY) {
        r = Mth.clamp(r + (int)scrollY, 0, 4);
    }

    @Override
    public void renderWorld(GuiGraphics graphics, int worldX, int worldY, int xOff, int yOff) {
        graphics.blit(id, worldX - r + xOff, worldY - r + yOff, 0, 0, r * 2 + 1, r * 2 + 1);
    }
}
