package wawa.mapwright.map.tool;

import com.mojang.blaze3d.platform.NativeImage;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.NativeImageTracker;
import wawa.mapwright.Rendering;
import wawa.mapwright.data.PageManager;
import wawa.mapwright.map.stamp_bag.StampBagHandler;
import wawa.mapwright.map.stamp_bag.StampInformation;
import wawa.mapwright.map.widgets.MapWidget;

public class CopyTool extends Tool {
    public static CopyTool INSTANCE = new CopyTool(MapwrightClient.id("copy_tool"));
    private static final ResourceLocation TEXTURE = MapwrightClient.id("tool/copy/copy");
    private final ResourceLocation textureID;
    public NativeImage clipboard = null;
    private Vector2ic start = null;

    public CopyTool(final ResourceLocation textureID) {
        this.textureID = textureID;
    }

    @Override
    public void mouseDown(final PageManager activePage, final MapWidget.MouseType mouseType, final Vector2d world) {
//        final Vector2ic end = new Vector2i(world, RoundingMode.FLOOR);
        if (mouseType == MapWidget.MouseType.LEFT) {
            if (this.clipboard == null) {
                if (this.start == null) {
                    this.start = new Vector2i(world, RoundingMode.FLOOR);
                }
            } /*else {
                activePage.putRegion(end.x() - this.clipboard.getWidth() / 2, end.y() - this.clipboard.getHeight() / 2, this.clipboard.getWidth(), this.clipboard.getHeight(),
                        (dx, dy, old) -> {
                            final int pixelColor = this.clipboard.getPixelRGBA(dx, dy);
                            if (pixelColor == 0) {
                                return activePage.getPixelARGB((end.x() - this.clipboard.getWidth() / 2) + dx, (end.y() - this.clipboard.getHeight() / 2) + dy);
                            } else {
                                return this.clipboard.getPixelRGBA(dx, dy);
                            }
                        }
                );
            }*/
        } /*else if (mouseType == MapWidget.MouseType.RIGHT) {
            if (this.clipboard != null) {
                this.clipboard.close();
                Minecraft.getInstance().getTextureManager().release(this.textureID);
                this.clipboard = null;
            }
        }*/
    }

    @Override
    public void mouseRelease(final PageManager activePage, final MapWidget.MouseType mouseType, final Vector2d world) {
        final Vector2ic end = new Vector2i(world, RoundingMode.FLOOR);

        if (this.start != null) {
            final Vector2ic upper_left = this.start.min(end, new Vector2i());
            final Vector2ic size = this.start.max(end, new Vector2i()).sub(upper_left);

            if (this.clipboard != null) {
                this.clipboard.close();
            }

            if (size.x() > 0 && size.y() > 0) {
	            NativeImage nativeImage = NativeImageTracker.newImage(size.x(), size.y(), false);
                activePage.forEachInRegion(upper_left.x(), upper_left.y(), size.x(), size.y(), nativeImage::setPixelRGBA);

	            StampInformation tempStamp = MapwrightClient.STAMP_HANDLER.temporaryStampInformation;
	            StampTool stampTool = StampTool.INSTANCE;

				stampTool.setActiveStamp(tempStamp); //allow the stamp tool to release texture before we do anything else
	            tempStamp.forceSetTexture(nativeImage);
				MapwrightClient.TOOL_MANAGER.set(stampTool);

            } else {
                this.clipboard = null;
                Minecraft.getInstance().getTextureManager().release(this.textureID);
            }

            this.start = null;
        }
    }

    @Override
    public void renderScreen(final GuiGraphics graphics, final double mouseX, final double mouseY) {
        graphics.blitSprite(TEXTURE, (int) mouseX - 8, (int) mouseY - 10, 16, 16);
    }

    @Override
    public void renderWorld(final GuiGraphics graphics, final int worldX, final int worldY, final double xOff, final double yOff) {
        if (this.start != null) {
            graphics.renderOutline((int) (this.start.x() + xOff), (int) (this.start.y() + yOff),
                    worldX - this.start.x(), worldY - this.start.y(),
                    0xff000000);
        } /*else if (this.clipboard != null) {
            final RenderType renderType = VeilRenderType.get(Rendering.RenderTypes.PALETTE_SWAP, this.textureID);
            if (renderType == null) return;

            Rendering.renderTypeBlit(graphics, renderType, worldX + xOff - (double) (this.clipboard.getWidth() / 2), worldY + yOff - (double) (this.clipboard.getHeight() / 2), 0, 0f, 0f,
                    this.clipboard.getWidth(), this.clipboard.getHeight(), this.clipboard.getWidth(), this.clipboard.getHeight(), 1);

            graphics.renderOutline((int) (worldX + xOff - (double) (this.clipboard.getWidth() / 2)), (int) (worldY + yOff - (double) (this.clipboard.getHeight() / 2)),
                    this.clipboard.getWidth(), this.clipboard.getHeight(),
                    0xff000000);
        }*/
    }
}
