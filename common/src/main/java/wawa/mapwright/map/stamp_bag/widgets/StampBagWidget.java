package wawa.mapwright.map.stamp_bag.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.gui.GUIElementAtlases;
import wawa.mapwright.map.MapScreen;
import wawa.mapwright.map.StampBagScreen;
import wawa.mapwright.map.tool.CopyTool;

public class StampBagWidget extends AbstractWidget {

    private final MapScreen mapScreen;

    public StampBagWidget(int x, int y, MapScreen screen) {
        super(x, y, 16, 16, Component.literal("Stamp Bag"));
        mapScreen = screen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float v) {
        if ((mapScreen.toolPicker.getImageFromScissorTool() != null & MapwrightClient.TOOL_MANAGER.get() instanceof CopyTool)
                || StampBagScreen.INSTANCE.getState() != StampBagScreen.ScreenState.IDLE) {
            GUIElementAtlases.STAMP_BAG_OPEN.render(guiGraphics, getX(), getY());
            if (isHovered) {
                GUIElementAtlases.STAMP_BAG_OPEN_HIGHLIGHT.render(guiGraphics, getX() - 1, getY() - 1);
            }
        } else {
            GUIElementAtlases.STAMP_BAG_CLOSED.render(guiGraphics, getX(), getY());
            if (isHovered) {
                GUIElementAtlases.STAMP_BAG_CLOSED_HIGHLIGHT.render(guiGraphics, getX() - 1, getY() - 1);
                guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("mapwright.tool.stamp"), mouseX, mouseY);
            }
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

        if (mapScreen.toolPicker.getImageFromScissorTool() != null && MapwrightClient.TOOL_MANAGER.get() instanceof CopyTool) {
            ss.changeStage(StampBagScreen.ScreenState.SAVING);
        } else {
            ss.changeStage(StampBagScreen.ScreenState.BROWSING);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
