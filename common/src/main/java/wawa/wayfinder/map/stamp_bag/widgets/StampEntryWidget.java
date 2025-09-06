package wawa.wayfinder.map.stamp_bag.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import wawa.wayfinder.map.StampBagScreen;
import wawa.wayfinder.map.stamp_bag.widgets.abstract_classes.AbstractStampScreenWidget;

import java.util.function.Consumer;

public class StampEntryWidget extends AbstractStampScreenWidget {


    public StampEntryWidget(int x, int y, int width, int height, StampBagScreen parentScreen) {
        super(x, y, width, height, parentScreen);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {

    }
}
