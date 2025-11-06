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
import wawa.mapwright.map.stamp_bag.StampInformation;
import wawa.mapwright.map.tool.CopyTool;
import wawa.mapwright.map.tool.StampTool;

public class StampBagWidget extends AbstractWidget {

    private final MapScreen mapScreen;

    public StampBagWidget(final int x, final int y, final MapScreen screen) {
        super(x, y, 16, 16, Component.literal("Stamp Bag"));
        this.mapScreen = screen;
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float v) {
        if ((this.mapScreen.toolPicker.getImageFromScissorTool() != null & MapwrightClient.TOOL_MANAGER.get() instanceof CopyTool)
                || StampBagScreen.INSTANCE.getState() != StampBagScreen.ScreenState.IDLE) {
            GUIElementAtlases.STAMP_BAG_OPEN.render(guiGraphics, this.getX(), this.getY());
            if (this.isHovered) {
                GUIElementAtlases.STAMP_BAG_OPEN_HIGHLIGHT.render(guiGraphics, this.getX() - 1, this.getY() - 1);
            }
        } else {
            GUIElementAtlases.STAMP_BAG_CLOSED.render(guiGraphics, this.getX(), this.getY());
            if (this.isHovered) {
                GUIElementAtlases.STAMP_BAG_CLOSED_HIGHLIGHT.render(guiGraphics, this.getX() - 1, this.getY() - 1);
                guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("mapwright.tool.stamp"), mouseX, mouseY);
            }
        }
    }

    @Override
    public void onClick(final double mouseX, final double mouseY) {
        super.onClick(mouseX, mouseY);

        final StampBagScreen ss = this.mapScreen.stampScreen;
        if (ss.getState() != StampBagScreen.ScreenState.IDLE) {
            ss.changeStage(StampBagScreen.ScreenState.IDLE);
            return;
        }

	    if (MapwrightClient.TOOL_MANAGER.get() instanceof StampTool st && st.isTempStamp()) {
            ss.changeStage(StampBagScreen.ScreenState.SAVING);
        } else {
            ss.changeStage(StampBagScreen.ScreenState.BROWSING);
        }
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {

    }
}
