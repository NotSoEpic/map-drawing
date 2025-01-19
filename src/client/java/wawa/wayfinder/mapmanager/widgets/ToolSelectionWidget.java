package wawa.wayfinder.mapmanager.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.joml.Vector2d;
import org.joml.Vector2i;
import wawa.wayfinder.RenderHelper;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;
import wawa.wayfinder.mapmanager.MapScreen;
import wawa.wayfinder.mapmanager.tools.PenTool;
import wawa.wayfinder.mapmanager.tools.RulerTool;
import wawa.wayfinder.mapmanager.tools.Tool;

public class ToolSelectionWidget extends AbstractWidget {
    private static final TextureAtlasSprite pencilSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/pencil"));
    private static final TextureAtlasSprite pencilHighlight = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/pencil_highlight"));
    private static final TextureAtlasSprite brushSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/brush"));
    private static final TextureAtlasSprite brushHighlight = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/brush_highlight"));
    private static final TextureAtlasSprite brushMask = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/brush_mask"));
    private static final TextureAtlasSprite eraserSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/eraser"));
    private static final TextureAtlasSprite eraserHighlight = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/eraser_highlight"));
    private static final TextureAtlasSprite rulerSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/ruler"));
    private static final TextureAtlasSprite rulerHighlight = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/ruler_highlight"));
    public static final PenTool pencil = new PenTool(1, 0, false);
    public final PalettePickerWidget palettePicker;
    public static final PenTool eraser = new PenTool(1, -1, false).sizeSettings(2, 7);
    public static final RulerTool ruler = new RulerTool();
    private final MapScreen screen;

    public ToolSelectionWidget(MapScreen screen) {
        super(screen.width - (50 + highlightSize) / 2, 30, highlightSize, spacing * 4, Component.literal("tool selection"));
        this.screen = screen;
        palettePicker = new PalettePickerWidget(this, ColorPalette.GRAYSCALE, (i, c) -> i != 0);
        palettePicker.visible = false;
        screen.addRenderableWidget(palettePicker);
    }

    private static final int iconSize = 16;
    private static final int highlightPadding = 1;
    private static final int highlightSize = iconSize + highlightPadding * 2;
    private static final int padding = 1;
    private static final int spacing = highlightSize + padding * 2;
    public int getTool(double mouseX, double mouseY) {
        int rowIndex = (int)Math.floor((mouseY - getY()) / spacing);
        if (rowIndex >= 0 && rowIndex < 4) {
            mouseY = mouseY - rowIndex * spacing + highlightPadding;
            mouseX = mouseX + highlightPadding;
            if (mouseY >= getY() && mouseY <= getY() + iconSize && mouseX >= getX() && mouseX <= getX() + iconSize)
                return rowIndex;
        }
        return -1;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return super.clicked(mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        switch (getTool(mouseX, mouseY)) {
            case 0 -> selectPencil();
            case 1 -> selectBrush();
            case 2 -> selectEraser();
            case 3 -> selectRuler();
        }
    }

    public void selectPencil() {
        Tool.set(pencil);
    }
    public void selectBrush() {
        if (Tool.get() == palettePicker.getLastSelected()) {
            Vector2d mouse = RenderHelper.smootherMouse();
            palettePicker.moveTo(mouse.x, mouse.y);
        }

        Tool.set(palettePicker.getLastSelected());
    }
    public void selectEraser() {
        Tool.set(eraser);
    }
    public void selectRuler() {
        if (Tool.get() == ruler)
            ruler.resetPos();
        Tool.set(ruler);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) || (palettePicker.isMouseOver(mouseX, mouseY));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        palettePicker.visible = getTool(mouseX, mouseY) == 1 || palettePicker.isMouseOver(mouseX, mouseY);
        if (!palettePicker.visible) {
            palettePicker.resetPos();
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(getX(), getY(), 0);
        int hover = getTool(mouseX, mouseY);

        renderToolIcon(guiGraphics, 0, pencilSprite, pencilHighlight, hover == 0, Tool.get() == pencil);

        renderToolIcon(guiGraphics, 1, brushSprite, brushHighlight, hover == 1, Tool.get() == palettePicker.getLastSelected());
        float[] rgb = WayfinderClient.palette.colors().get(palettePicker.getLastSelected().getColorIndex()).getColorComponents(null);
        float val = Tool.get() == palettePicker.getLastSelected() ? 0.5f : 1f;
        guiGraphics.blit(highlightPadding, highlightPadding +spacing, 0, iconSize, iconSize, brushMask, rgb[0] * val, rgb[1] * val, rgb[2] * val, 1);

        renderToolIcon(guiGraphics, 2, eraserSprite, eraserHighlight, hover == 2, Tool.get() == eraser);
        renderToolIcon(guiGraphics, 3, rulerSprite, rulerHighlight, hover == 3, Tool.get() == ruler);
        guiGraphics.pose().popPose();

        if (Tool.get() != ruler && ruler.startPos != null && ruler.endPos != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(screen.map.getX(), screen.map.getY(), 0);
            ruler.renderMeasure(screen.map, guiGraphics, ruler.startPos, ruler.endPos, false);
            guiGraphics.pose().popPose();
        }
    }

    private void renderToolIcon(GuiGraphics guiGraphics, int index, TextureAtlasSprite texture, TextureAtlasSprite highlight, boolean hovered, boolean selected) {
        float val = selected ? 0.5f : 1f;
        if (hovered || selected)
            guiGraphics.blit(0, index * spacing, 0, highlightSize, highlightSize, highlight, val, val, val, 1);
        guiGraphics.blit(highlightPadding, highlightPadding + index * spacing, 0, iconSize, iconSize, texture, val, val, val, 1);
    }

    public void pickColor(MapWidget widget, Vector2i world) {
        int pixelColor = widget.getPixelWorld(world.x, world.y);
        if (!palettePicker.tryPickColor(pixelColor) && pixelColor == ColorPalette.GRAYSCALE.colors().get(0).getRGB()) {
            Tool.set(pencil);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
