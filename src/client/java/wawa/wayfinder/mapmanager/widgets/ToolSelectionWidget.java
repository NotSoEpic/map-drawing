package wawa.wayfinder.mapmanager.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import wawa.wayfinder.Wayfinder;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.color.ColorPalette;
import wawa.wayfinder.mapmanager.MapScreen;
import wawa.wayfinder.mapmanager.tools.PenTool;
import wawa.wayfinder.mapmanager.tools.RulerTool;
import wawa.wayfinder.mapmanager.tools.Tool;

public class ToolSelectionWidget extends AbstractWidget {
    private static final TextureAtlasSprite pencil_sprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool_pencil"));
    private static final TextureAtlasSprite brush_sprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool_brush"));
    private static final TextureAtlasSprite brush_mask_sprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool_brush_mask"));
    private static final TextureAtlasSprite eraser_sprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool_eraser"));
    private static final TextureAtlasSprite ruler_sprite = Minecraft.getInstance().getGuiSprites().getSprite(Wayfinder.id("tool_ruler"));
    public static final PenTool pencil = new PenTool(1, 0, false);
    public static final PenTool brush = new PenTool(1, 1, true);
    public static final PenTool eraser = new PenTool(1, -1, false);
    public static final RulerTool ruler = new RulerTool();
    private final MapScreen screen;
    private final PalettePickerWidget palettePicker;

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
            case 0 -> Tool.set(pencil);
            case 1 -> Tool.set(brush);
            case 2 -> Tool.set(eraser);
            case 3 -> Tool.set(ruler);
        }
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
        guiGraphics.blit(0, 0, 0, 50, 30, pencil_sprite, 1, 1, 1, 1);
        guiGraphics.blit(0, 30, 0, 50, 30, brush_sprite, 1, 1, 1, 1);

        float[] rgb = WayfinderClient.palette.colors().get(brush.getColorIndex()).getColorComponents(null);
        guiGraphics.blit(0, 30, 1, 50, 30, brush_mask_sprite, rgb[0], rgb[1], rgb[2], 1);

        guiGraphics.blit(0, 60, 0, 50, 30, eraser_sprite, 1, 1, 1, 1);
        guiGraphics.blit(0, 90, 0, 50, 30, ruler_sprite, 1, 1, 1, 1);

//        palettePicker.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
