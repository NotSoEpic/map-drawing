package wawa.wayfinder.map.stamp_bag.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import wawa.wayfinder.gui.GUIElementAtlases;
import wawa.wayfinder.map.MapScreen;
import wawa.wayfinder.map.StampBagScreen;
import wawa.wayfinder.map.tool.CopyTool;

public class StampBagWidget extends AbstractWidget {

    private final MapScreen mapScreen;

    public StampBagWidget(int x, int y, MapScreen screen) {
        super(x, y, 16, 16, Component.literal("Stamp Bag"));
        mapScreen = screen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        if (mapScreen.toolPicker.getCopiedImage() == null) {
            GUIElementAtlases.STAMP_BAG_CLOSED.render(guiGraphics, getX(), getY());
        } else {
            GUIElementAtlases.STAMP_BAG_OPEN.render(guiGraphics, getX(), getY());
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);

        if (mapScreen.toolPicker.getCopiedImage() != null) {
            mapScreen.stampScreen.changeStage(StampBagScreen.ScreenState.SAVING);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
