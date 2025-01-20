package wawa.wayfinder.mapmanager.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import wawa.wayfinder.RenderHelper;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.mapmanager.widgets.MapWidget;
import wawa.wayfinder.rendering.WayfinderRenderTypes;

public class SelectTool extends Tool {
    private static Vector2i dragStart;
    private static DynamicTexture selectedImage;
    private static Vector2i wh;
    private static final TextureAtlasSprite rulerSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("cursor/scissors"));
    private static final ResourceLocation selectedId = Wayfinder.id("selected_image");
    @Override
    protected void onSelect() {
        dragStart = null;
        selectedImage = null;
    }

    @Override
    public void leftDown(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        dragStart = world;
        selectedImage = null;
    }

    @Override
    public void rightDown(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        if (selectedImage != null) {
            WayfinderClient.regions.clearHistory();
            widget.putTextureWorld(world.x, world.y, selectedImage.getPixels());
        }
    }

    @Override
    public void leftUp(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        if (dragStart != null) {
            Vector2i ul = new Vector2i(dragStart).min(world);
            Vector2i lr = dragStart.max(world).add(1, 1);
            wh = new Vector2i(lr).sub(ul);
            if (wh.x > 0 && wh.y > 0) {
                selectedImage = new DynamicTexture(wh.x, wh.y, false);
                for (int i = 0; i < wh.x; i++) {
                    for (int j = 0; j < wh.y; j++) {
                        selectedImage.getPixels().setPixelRGBA(i, j, widget.getPixelWorld(ul.x + i, ul.y + j));
                    }
                }
                selectedImage.upload();
                Minecraft.getInstance().getTextureManager().register(selectedId, selectedImage);
            }
        }
        dragStart = null;
    }

    @Override
    public void renderTool(MapWidget widget, GuiGraphics context, boolean shift, Vector2d mouse, Vector2i world) {
        if (dragStart != null) {
            Vector2i roundMouse = new Vector2i(widget.worldToScreen(world.x, world.y, true), RoundingMode.FLOOR);
            Vector2i dragMouse = new Vector2i(widget.worldToScreen(dragStart.x, dragStart.y, true), RoundingMode.FLOOR);
            Vector2i ul = new Vector2i(dragMouse).min(roundMouse);
            Vector2i wh = new Vector2i(dragMouse).max(roundMouse).sub(ul).add((int)widget.scale, (int)widget.scale);
            if (wh.x > 0 && wh.y > 0)
                context.renderOutline(ul.x, ul.y, wh.x, wh.y, -1);
        } else if (selectedImage != null) {
            Vector2i roundMouse = new Vector2i(widget.worldToScreen(world.x - wh.x/2, world.y - wh.y/2, true), RoundingMode.FLOOR);
            int sw = (int) (wh.x * widget.scale);
            int sh = (int) (wh.y * widget.scale);
            RenderHelper.renderTypeBlit(context, WayfinderRenderTypes.getPaletteSwap(selectedId), roundMouse.x, roundMouse.y, 0, 0f, 0f, sw, sh, sw, sh);
        }
    }

    @Override
    public boolean hideMouse(MapWidget widget, Vector2d mouse, Vector2i world) {
        return false;
    }

    @Override
    public @Nullable TextureAtlasSprite getCursorIcon() {
        return rulerSprite;
    }

    @Override
    public Vector2i getCursorIconOffset() {
        return new Vector2i(-7, -16);
    }
}
