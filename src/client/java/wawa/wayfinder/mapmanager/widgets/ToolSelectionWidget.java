package wawa.wayfinder.mapmanager.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;
import wawa.wayfinder.mapmanager.MapScreen;
import wawa.wayfinder.mapmanager.tools.PenTool;
import wawa.wayfinder.mapmanager.tools.RulerTool;
import wawa.wayfinder.mapmanager.tools.Tool;

public class ToolSelectionWidget extends AbstractWidget {
    private static final TextureAtlasSprite pencilSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/pencil"));
    private static final TextureAtlasSprite brushSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/brush"));
    private static final TextureAtlasSprite brushMaskSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/brush_mask"));
    private static final TextureAtlasSprite eraserSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/eraser"));
    private static final TextureAtlasSprite rulerSprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool/ruler"));
    public static final PenTool pencil = new PenTool(1, 0, false);
    public final PalettePickerWidget palettePicker;
    public static final PenTool eraser = new PenTool(1, -1, false).sizeSettings(2, 7);
    public static final RulerTool ruler = new RulerTool();
    private final MapScreen screen;

    public ToolSelectionWidget(MapScreen screen) {
        super(screen.width - 50, 30, 50, 30 * 4, Component.literal("tool selection"));
        this.screen = screen;
        palettePicker = new PalettePickerWidget(this, ColorPalette.GRAYSCALE, (i, c) -> i != 0);
        palettePicker.visible = false;
        screen.addRenderableWidget(palettePicker);
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return super.clicked(mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        switch (Math.floorDiv((int)mouseY - getY(), 30)) {
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
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isMouseOver(mouseX, mouseY) && Math.floorDiv(mouseY - getY(), 30) == 1) {
            palettePicker.visible = true;
            screen.setDrawingEnabled(false);
        }
        else if (!palettePicker.isMouseOver(mouseX, mouseY)) {
            palettePicker.visible = false;
            screen.setDrawingEnabled(true);
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(getX(), getY(), 0);
        guiGraphics.blit(0, 0, 0, 50, 30, pencilSprite);
        guiGraphics.blit(0, 30, 0, 50, 30, brushSprite);

        float[] rgb = WayfinderClient.palette.colors().get(palettePicker.getLastSelected().getColorIndex()).getColorComponents(null);
        guiGraphics.blit(0, 30, 1, 50, 30, brushMaskSprite, rgb[0], rgb[1], rgb[2], 1);

        guiGraphics.blit(0, 60, 0, 50, 30, eraserSprite);
        guiGraphics.blit(0, 90, 0, 50, 30, rulerSprite);

        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(screen.map.getX(), screen.map.getY(), 0);
        if (Tool.get() != ruler && ruler.startPos != null && ruler.endPos != null) {
            ruler.renderMeasure(screen.map, guiGraphics, ruler.startPos, ruler.endPos, false);
        }
        guiGraphics.pose().popPose();
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
