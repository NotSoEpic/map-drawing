package wawa.wayfinder.map.stamp_bag.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import wawa.wayfinder.WayfinderClient;
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
        if ((mapScreen.toolPicker.getCopiedImage() != null & WayfinderClient.TOOL_MANAGER.get() instanceof CopyTool)
                || StampBagScreen.INSTANCE.getState() != StampBagScreen.ScreenState.IDLE) {
            GUIElementAtlases.STAMP_BAG_OPEN.render(guiGraphics, getX(), getY());
        } else {
            GUIElementAtlases.STAMP_BAG_CLOSED.render(guiGraphics, getX(), getY());
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);

        StampBagScreen ss = mapScreen.stampScreen;
        if (ss.getState() != StampBagScreen.ScreenState.IDLE) {
            ss.changeStage(StampBagScreen.ScreenState.IDLE);
            return;
        }

        if (mapScreen.toolPicker.getCopiedImage() != null && WayfinderClient.TOOL_MANAGER.get() instanceof CopyTool) {
            ss.changeStage(StampBagScreen.ScreenState.SAVING);
        } else {
            ss.changeStage(StampBagScreen.ScreenState.BROWSING);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
