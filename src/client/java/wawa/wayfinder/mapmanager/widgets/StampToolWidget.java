package wawa.wayfinder.mapmanager.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import wawa.wayfinder.RenderHelper;
import wawa.wayfinder.mapmanager.tools.StampTool;
import wawa.wayfinder.mapmanager.tools.Tool;
import wawa.wayfinder.rendering.WayfinderRenderTypes;

public class StampToolWidget extends AbstractWidget {
    private final StampTool stampTool;
    public StampToolWidget(int x, int y, ResourceLocation stamp) {
        super(x, y, 16, 16, Component.literal("stamp"));
        stampTool = new StampTool(stamp);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Tool.set(stampTool);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderHelper.renderTypeBlit(guiGraphics, WayfinderRenderTypes.getPaletteSwap(stampTool.stamp),
                getX(),getY(), 0,
                0.0f, 0.0f,
                width, height, width, height
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
