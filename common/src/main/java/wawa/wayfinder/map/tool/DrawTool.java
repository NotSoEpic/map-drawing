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

    public DrawTool(final int color) {
        this.color = color;
    }

    private void rebuildPixels() {
        preview.close();
        final int wh = this.r * 2 + 1;
        final NativeImage im = new NativeImage(wh, wh, true);
        if (this.color == 0) { // black hollow rectangle
            im.fillRect(0, 0, wh, wh, 0xFF000000);
            if (wh >= 3) {
                im.fillRect(1, 1, wh - 2, wh - 2, 0);
            }
        } else {
            im.fillRect(0, 0, wh, wh, this.color);
        }
        preview = new DynamicTexture(im);
        preview.upload();
        Minecraft.getInstance().getTextureManager().register(id, preview);
    }

    @Override
    public void hold(final PageManager activePage, final MapWidget.Mouse mouse, final Vector2d oldWorld, final Vector2d world) {

        switch (mouse) {
            case LEFT -> this.pixelLine(oldWorld.floor(), world.floor(), pos ->  {
                activePage.startSnapshot();
                activePage.putSquare(pos.x, pos.y, this.color, this.r);
            });
            case RIGHT -> this.pixelLine(oldWorld.floor(), world.floor(), pos -> {
                activePage.startSnapshot();
                activePage.putSquare(pos.x, pos.y, 0, this.r);
            });
        }
    }

    @Override
    public void release(final PageManager activePage) {
        activePage.endSnapshot();
        super.release(activePage);
    }

    private void pixelLine(final Vector2d point1, final Vector2d point2, final Consumer<Vector2i> perPixel) {
        final Vector2d delta = new Vector2d(point1).sub(point2);
        final int steps = (int) Math.max(1, Math.ceil(Math.max(Math.abs(delta.x), Math.abs(delta.y))));
        delta.div(steps);
        final Vector2d pos = new Vector2d(point2);
        for (int i = 0; i < steps + 1; i++) {
            perPixel.accept(new Vector2i(pos.x + 0.5, pos.y + 0.5, RoundingMode.FLOOR));
            pos.add(delta);
        }
    }

    @Override
    public void controlScroll(final PageManager activePage, final double mouseX, final double mouseY, final double scrollY) {
        this.r = Mth.clamp(this.r + (int)scrollY, 0, 4);
        this.rebuildPixels();
    }

    @Override
    public void renderWorld(final GuiGraphics graphics, final int worldX, final int worldY, final int xOff, final int yOff) {
        final int wh = this.r * 2 + 1;
        graphics.blit(id, worldX - this.r + xOff, worldY - this.r + yOff, 0, 0, wh, wh, wh, wh);
    }

    @Override
    public void renderScreen(final GuiGraphics graphics, final double mouseX, final double mouseY) {
        if (this.icon != null) {
            final Vec2 mouse = Helper.preciseMousePos();
            graphics.pose().pushPose();
            graphics.pose().translate(mouse.x % 1, mouse.y % 1, 0);
            graphics.blitSprite(this.icon, (int)mouse.x - 16, (int)mouse.y - 16, 32, 32);
            graphics.pose().popPose();
        }
    }

    @Override
    public void onSelect() {
        this.rebuildPixels();
    }

    public int getVisualColor() {
        return this.color;
    }

    public int getInternalColor() {
        return this.color;
    }
}
