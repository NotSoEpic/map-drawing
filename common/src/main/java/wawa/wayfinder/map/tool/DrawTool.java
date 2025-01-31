package wawa.wayfinder.map.tool;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.widgets.MapWidget;

import java.util.function.Consumer;

public class DrawTool extends Tool {
    public ResourceLocation icon = null;
    private final int color;
    private int r = 0;
    private static final ResourceLocation id = WayfinderClient.id("draw");
    private static DynamicTexture preview = new DynamicTexture(1, 1, false);
    static {
        // crashes the game if this class is loaded too early. boowomp
        preview.getPixels().setPixelRGBA(0, 0, 0xFF000000);
        preview.upload();
        Minecraft.getInstance().getTextureManager().register(id, preview);
    }

    public DrawTool(int color) {
        this.color = color;
    }

    private void rebuildPixels() {
        preview.close();
        int wh = r * 2 + 1;
        NativeImage im = new NativeImage(wh, wh, true);
        if (color == 0) { // black hollow rectangle
            im.fillRect(0, 0, wh, wh, 0xFF000000);
            if (wh >= 3) {
                im.fillRect(1, 1, wh - 2, wh - 2, 0);
            }
        } else {
            im.fillRect(0, 0, wh, wh, color);
        }
        preview = new DynamicTexture(im);
        preview.upload();
        Minecraft.getInstance().getTextureManager().register(id, preview);
    }

    @Override
    public void hold(PageManager activePage, MapWidget.Mouse mouse, Vector2d oldWorld, Vector2d world) {
        activePage.startSnapshot();

        switch (mouse) {
            case LEFT -> pixelLine(oldWorld.floor(), world.floor(), pos -> activePage.putSquare(pos.x, pos.y, color, r));
            case RIGHT -> pixelLine(oldWorld.floor(), world.floor(), pos -> activePage.putSquare(pos.x, pos.y, 0, r));
        }
    }

    @Override
    public void release(final PageManager activePage) {
        activePage.endSnapshot();
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
        rebuildPixels();
    }

    @Override
    public void renderWorld(GuiGraphics graphics, int worldX, int worldY, int xOff, int yOff) {
        int wh = r * 2 + 1;
        graphics.blit(id, worldX - r + xOff, worldY - r + yOff, 0, 0, wh, wh, wh, wh);
    }

    @Override
    public void renderScreen(GuiGraphics graphics, double mouseX, double mouseY) {
        if (icon != null) {
            Vec2 mouse = Helper.preciseMousePos();
            graphics.pose().pushPose();
            graphics.pose().translate(mouse.x % 1, mouse.y % 1, 0);
            graphics.blitSprite(icon, (int)mouse.x - 16, (int)mouse.y - 16, 32, 32);
            graphics.pose().popPose();
        }
    }

    @Override
    public void onSelect() {
        rebuildPixels();
    }

    public int getVisualColor() {
        return color;
    }

    public int getInternalColor() {
        return color;
    }
}
