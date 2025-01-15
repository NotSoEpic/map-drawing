package wawa.wayfinder.mapmanager.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import wawa.wayfinder.RenderHelper;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;
import wawa.wayfinder.mapmanager.widgets.MapWidget;
import wawa.wayfinder.rendering.WayfinderRenderTypes;

import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class PenTool extends Tool {
    public int size;
    private int colorIndex;
    public boolean highlight;
    private int minSize = 1;
    private int maxSize = 5;
    private static final DynamicTexture pen = new DynamicTexture(1, 1, false);
    private static final ResourceLocation textureId = Wayfinder.id("pen");
    private static final TextureAtlasSprite pencilSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("cursor/pencil"));
    private static final TextureAtlasSprite brushSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("cursor/brush"));
    private static final TextureAtlasSprite eraserSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("cursor/eraser"));

    @Override
    public void onSelect() {
        setColorIndex(colorIndex);
        Minecraft.getInstance().getTextureManager().register(textureId, pen);
    }

    public PenTool(int size, int colorIndex, boolean highlight) {
        this.size = size;
        this.colorIndex = colorIndex;
        this.highlight = highlight;
    }

    public PenTool sizeSettings(int min, int max) {
        this.minSize = min;
        this.maxSize = max;
        changeSize(0);
        return this;
    }

    @Override
    public void leftHold(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        if (shift) {
            Vector2i lastWorld = WayfinderClient.lastDrawnPos;
            if (lastWorld == null) {
                widget.drawLineScreen(mouse.x, mouse.y, mouse.x, mouse.y, getDrawnColor(), size, getPixelMap());
            } else {
                widget.drawLineWorld(lastWorld.x, lastWorld.y, world.x, world.y, getDrawnColor(), size, getPixelMap());
            }
        } else {
            widget.drawLineScreen(widget.prevX, widget.prevY, mouse.x, mouse.y, getDrawnColor(), size, getPixelMap());
        }
        WayfinderClient.lastDrawnPos = new Vector2i(world);
    }

    @Override
    public void leftDown(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        if (Screen.hasAltDown()) {
            Color color = new Color(widget.getPixelWorld(world.x, world.y));
            if (color.getAlpha() != 0) {
                int i = ColorPalette.GRAYSCALE.colors().indexOf(color);
                if (i != -1) {
                    setColorIndex(i);
                }
            }
        } else {
            WayfinderClient.regions.clearHistory();
        }
    }

    @Override
    public void rightDown(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        WayfinderClient.regions.clearHistory();
    }

    @Override
    public void rightHold(MapWidget widget, boolean shift, Vector2d mouse, Vector2i world) {
        if (Screen.hasAltDown())
            return;
        if (shift) {
            Vector2i lastWorld = WayfinderClient.lastDrawnPos;
            if (lastWorld == null) {
                widget.drawLineScreen(mouse.x, mouse.y, mouse.x, mouse.y, 0, size, getPixelMap());
            } else {
                widget.drawLineWorld(lastWorld.x, lastWorld.y, world.x, world.y, 0, size, getPixelMap());
            }
        } else {
            widget.drawLineScreen(widget.prevX, widget.prevY, mouse.x, mouse.y, 0, size, getPixelMap());
        }
        WayfinderClient.lastDrawnPos = new Vector2i(world);
    }

    private BiFunction<Integer, Integer, Integer> getPixelMap() {
        if (highlight) {
            return (pixel, current) -> highlightPredicate().test(pixel, current) ? pixel : current;
        }
        return (pixel, current) -> pixel;
    }

    private BiPredicate<Integer, Integer> highlightPredicate() {
        return (pixel, current) -> {
            // todo: make this not hardcoded
            if (current == ColorPalette.GRAYSCALE.colors().get(0).getRGB())
                return false;
            return true;
        };
    }

    @Override
    public void ctrlScroll(MapWidget widget, Vector2d mouse, Vector2i world, double verticalAmount) {
        changeSize((int)verticalAmount);
    }

    private void changeSize(int delta) {
        size = Mth.clamp(size + delta, minSize, maxSize);
    }

    @Override
    public void renderTool(MapWidget widget, GuiGraphics context, boolean shift, Vector2d mouse, Vector2i world) {
        int wh = size * 2 - 1;

        int swh = (int) (wh * widget.scale);
        if (shift && WayfinderClient.lastDrawnPos != null) {
            double dx = WayfinderClient.lastDrawnPos.x - world.x;
            double dz = WayfinderClient.lastDrawnPos.y - world.y;
            int steps = (int) Math.max(1, Math.ceil(Math.max(Math.abs(dx), Math.abs(dz))));
            dx /= steps;
            dz /= steps;
            double x = world.x;
            double z = world.y;
            for (int i = 0; i < steps + 1; i++) {
                Vector2d ul = widget.worldToScreen(Math.round(x), Math.round(z), true)
                        .sub(new Vector2d(size - 1).floor().mul(widget.scale));

                if (ul.x > -size * widget.scale * 2 && ul.x < context.guiWidth() &&
                        ul.y > -size * widget.scale * 2 && ul.y < context.guiHeight()) {
                    RenderHelper.renderTypeBlit(context, WayfinderRenderTypes.getPaletteSwap(textureId),
                            (int) ul.x, (int) ul.y, 0, 0.0f, 0.0f,
                            swh, swh, swh, swh
                    );
                }
                x += dx;
                z += dz;
            }
        } else {
            world = new Vector2i(widget.worldToScreen(new Vector2d(world).sub(size - 1, size - 1), true), RoundingMode.FLOOR);

            RenderHelper.renderTypeBlit(context, WayfinderRenderTypes.getPaletteSwap(textureId),
                    world.x, world.y, 0, 0.0f, 0.0f,
                    swh, swh, swh, swh
            );
        }
    }

    @Override
    public boolean hideMouse(MapWidget widget, Vector2d mouse, Vector2i world) {
        return true;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
        pen.getPixels().applyToAllPixels(i -> getDrawnColor());
        pen.upload();
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public int getVisualColor() {
        if (colorIndex == -1)
            return 0;
        return WayfinderClient.palette.colors().get(colorIndex).getRGB();
    }

    public int getDrawnColor() {
        if (colorIndex == -1)
            return 0;
        return ColorPalette.GRAYSCALE.colors().get(colorIndex).getRGB();
    }

    @Override
    public @Nullable TextureAtlasSprite getCursorIcon() {
        if (colorIndex == -1)
            return eraserSprite;
        return highlight ? brushSprite : pencilSprite;
    }

    @Override
    public Vector2i getCursorIconOffset() {
        if (colorIndex == -1)
            return new Vector2i(-4, -15);
        return highlight ? new Vector2i(-4, -15) : new Vector2i(-1, -15);
    }
}
