package wawa.wayfinder.map.tool;

import com.mojang.blaze3d.platform.NativeImage;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
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

public class CopyTool extends Tool {
    public static CopyTool INSTANCE = new CopyTool(WayfinderClient.id("copy_tool"));
    private final ResourceLocation textureID;
    private NativeImage clipboard = null;
    private Vector2ic start = null;
    private boolean initialClick = true;

    public CopyTool(final ResourceLocation textureID) {
        this.textureID = textureID;
    }

    @Override
    public void hold(final PageManager activePage, final MapWidget.Mouse mouse, final Vector2d oldWorld, final Vector2d world) {
        final Vector2ic end = new Vector2i(world, RoundingMode.FLOOR);
        if (mouse == MapWidget.Mouse.LEFT && this.initialClick) {
            if (this.clipboard == null) {
                if (this.start == null) {
                    this.start = new Vector2i(world, RoundingMode.FLOOR);
                }
            } else {
                activePage.putRegion(end.x(), end.y(), this.clipboard.getWidth(), this.clipboard.getHeight(),
                    (dx, dy, old) -> this.clipboard.getPixelRGBA(dx, dy)
                );
            }
        }

        if (mouse == MapWidget.Mouse.RIGHT) {
            if (this.clipboard != null) {
                this.clipboard.close();
                Minecraft.getInstance().getTextureManager().release(this.textureID);
                this.clipboard = null;
            }
        }

        if (mouse == MapWidget.Mouse.NONE && this.start != null) {

            final Vector2ic upper_left = this.start.min(end, new Vector2i());
            final Vector2ic size = this.start.max(end, new Vector2i()).sub(upper_left);

            if (this.clipboard != null) {
                this.clipboard.close();
            }

            if (size.x() > 0 && size.y() > 0) {
                this.clipboard = NativeImageTracker.newImage(size.x(), size.y(), false);
                activePage.forEachInRegion(upper_left.x(), upper_left.y(), size.x(), size.y(), this.clipboard::setPixelRGBA);
                Minecraft.getInstance().getTextureManager().register(this.textureID, new DynamicTexture(this.clipboard));
            } else {
                this.clipboard = null;
                Minecraft.getInstance().getTextureManager().release(this.textureID);
            }

            this.start = null;
        }

        this.initialClick = mouse == MapWidget.Mouse.NONE;
    }

    @Override
    public void release(final PageManager activePage) {
        this.initialClick = true;
    }

    @Override
    public void renderScreen(final GuiGraphics graphics, final double mouseX, final double mouseY) {
        final Vec2 mouse = Helper.preciseMousePos();
        graphics.pose().pushPose();
        graphics.pose().translate(mouse.x % 1, mouse.y % 1, 0);
        graphics.blitSprite(WayfinderClient.id("cursor/pencil"), (int)mouse.x - 16, (int)mouse.y - 16, 32, 32);
        graphics.pose().popPose();
    }

    @Override
    public void renderWorld(final GuiGraphics graphics, final int worldX, final int worldY, final double xOff, final double yOff) {
        if (this.clipboard != null) {
            final RenderType renderType = VeilRenderType.get(Rendering.RenderTypes.PALETTE_SWAP, this.textureID);
            if(renderType == null) return;

            Rendering.renderTypeBlit(graphics, renderType, worldX + xOff, worldY + yOff, 0, 0f, 0f,
                    this.clipboard.getWidth(), this.clipboard.getHeight(), this.clipboard.getWidth(), this.clipboard.getHeight(),1);
        }
    }
}
