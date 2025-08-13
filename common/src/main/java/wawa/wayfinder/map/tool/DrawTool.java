package wawa.wayfinder.map.tool;

import com.mojang.blaze3d.platform.NativeImage;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import wawa.wayfinder.Helper;
import wawa.wayfinder.NativeImageTracker;
import wawa.wayfinder.Rendering;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.widgets.MapWidget;

import java.util.function.Consumer;

public class DrawTool extends Tool {
    public ResourceLocation icon = null;
    protected final int internal_color;
    protected final int visual_color;
    protected int r = 0;
    private static final ResourceLocation id = WayfinderClient.id("draw");
    private static DynamicTexture preview = new DynamicTexture(1, 1, false);
    static {
        // crashes the game if this class is loaded too early. boowomp
        preview.getPixels().setPixelRGBA(0, 0, 0xFF000000);
        preview.upload();
        Minecraft.getInstance().getTextureManager().register(id, preview);
    }

    public DrawTool(final int color, final int visual_color) {
        this.internal_color = color;
        this.visual_color = visual_color;
    }

    public int getRadius() {
        return this.r;
    }

    private void rebuildPixels() {
        preview.close();
        final int wh = this.r * 2 + 1;
        final NativeImage im = NativeImageTracker.newImage(wh, wh, true);
        if (this.internal_color == 0) { // black hollow rectangle
            im.fillRect(0, 0, wh, wh, 0xFF000000);
            if (wh >= 3) {
                im.fillRect(1, 1, wh - 2, wh - 2, 0);
            }
        } else {
            im.fillRect(0, 0, wh, wh, this.internal_color);
        }
        preview = new DynamicTexture(im);
        preview.upload();
        Minecraft.getInstance().getTextureManager().register(id, preview);
    }

    @Override
    public void hold(final PageManager activePage, final MapWidget.Mouse mouse, final Vector2d oldWorld, final Vector2d world) {

        switch (mouse) {
            case LEFT -> this.pixelLine(oldWorld.floor(), world.floor(), pos ->  {
                this.putSquare(activePage, pos, this.internal_color);
            });
            case RIGHT -> this.pixelLine(oldWorld.floor(), world.floor(), pos -> {
                this.removeSquare(activePage, pos, 0);
            });
        }
    }

    public void putSquare(final PageManager activePage, final Vector2ic pos, final int targetColor) {
        activePage.startSnapshot();
        activePage.putSquare(pos.x(), pos.y(), targetColor, this.r);
    }

    public void removeSquare(final PageManager activePage, final Vector2ic pos, final int targetColor) {
        activePage.startSnapshot();
        activePage.putSquare(pos.x(), pos.y(), targetColor, this.r);
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
        this.r = Mth.clamp(this.r + (int)scrollY, 0, this.internal_color == 0 ? 8 : 4);
        this.rebuildPixels();
    }

    @Override
    public void renderWorld(final GuiGraphics graphics, final int worldX, final int worldY, final double xOff, final double yOff) {
        final RenderType renderType = VeilRenderType.get(Rendering.RenderTypes.PALETTE_SWAP, id);
        if(renderType == null) return;

        final int wh = this.r * 2 + 1;
        final double x = worldX - this.r + xOff;
        final double y = worldY - this.r + yOff;
        Rendering.renderTypeBlit(graphics, renderType, x, y, 0, 0.0f, 0.0f, wh, wh, wh, wh);
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
        return this.visual_color;
    }

    public int getInternalColor() {
        return this.internal_color;
    }
}
