package wawa.wayfinder.mapmanager.tools;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2i;
import wawa.wayfinder.mapmanager.widgets.MapWidget;

public abstract class Tool {
    private static Tool tool;
    private static Tool prevTool;

    public static void set(@Nullable Tool tool) {
        if (Tool.tool != null)
            Tool.tool.onDeselect();
        if (Tool.tool != tool) {
            Tool.prevTool = Tool.tool;
            Tool.tool = tool;
        }
        if (tool != null)
            tool.onSelect();
    }

    public static Tool get() {
        return tool;
    }

    public static void swap() {
        if (prevTool != null) {
            set(prevTool);
        }
    }

    protected abstract void onSelect();
    protected void onDeselect() {}

    public void leftDown(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {}

    /**
     * Called every frame left click is held on the map
     * @param widget the map
     * @param shift if shift is held
     * @param mouse screen position of the mouse, (0,0) being top left of the map
     * @param world world position of the mouse
     */
    public void leftHold(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {}
    public void leftUp(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {}


    public void rightDown(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {}
    /**
     * Called every frame right click is held on the map
     * @param widget the map
     * @param shift if shift is held
     * @param mouse screen position of the mouse, (0,0) being top left of the map
     * @param world world position of the mouse
     */
    public void rightHold(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {}
    public void rightUp(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {}

    public void ctrlScroll(MapWidget widget, Vector2d mouse, Vector2i world, double verticalAmount) {}

    public static void render(MapWidget widget, GuiGraphics context, boolean shift, Vector2d mouse, Vector2i world) {
        Tool.get().renderTool(widget, context, shift, mouse, world);
        TextureAtlasSprite sprite = Tool.get().getCursorIcon();
        if (sprite != null) {
            Vector2d pos = new Vector2d(Tool.get().getCursorIconOffset()).sub(widget.getX(), widget.getY()).add(mouse);
            RenderSystem.setShaderTexture(0, sprite.atlasLocation());
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            Matrix4f matrix4f = context.pose().last().pose();
            BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            float x1 = (float)pos.x;
            float y1 = (float)pos.y;
            float x2 = x1 + 16;
            float y2 = y1 + 16;
            bufferBuilder.addVertex(matrix4f, x1, y1, 0).setUv(sprite.getU0(), sprite.getV0());
            bufferBuilder.addVertex(matrix4f, x1, y2, 0).setUv(sprite.getU0(), sprite.getV1());
            bufferBuilder.addVertex(matrix4f, x2, y2, 0).setUv(sprite.getU1(), sprite.getV1());
            bufferBuilder.addVertex(matrix4f, x2, y1, 0).setUv(sprite.getU1(), sprite.getV0());
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        }
    }
    public abstract void renderTool(MapWidget widget, GuiGraphics context, boolean shift, Vector2d mouse, Vector2i world);
    public boolean hideWhenInactive() {
        return true;
    }
    public abstract boolean hideMouse(MapWidget widget, Vector2d mouse, Vector2i world);

    @Nullable
    public TextureAtlasSprite getCursorIcon() {
        return null;
    }

    public Vector2i getCursorIconOffset() {
        return new Vector2i();
    }
}
