package wawa.mapwright.map.stamp_bag.widgets.abstract_classes;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import wawa.mapwright.map.StampBagScreen;

public abstract class AbstractStampScreenWidget extends AbstractWidget {

    public final StampBagScreen parentScreen;
    public AbstractStampScreenWidget(int x, int y, int width, int height, StampBagScreen parentScreen) {
        super(x, y, width, height, Component.empty());
        this.parentScreen = parentScreen;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
