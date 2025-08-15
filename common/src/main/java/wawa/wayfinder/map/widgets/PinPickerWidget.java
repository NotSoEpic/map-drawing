package wawa.wayfinder.map.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.Pin;
import wawa.wayfinder.map.tool.PinTool;

import java.util.ArrayList;
import java.util.List;

public class PinPickerWidget extends AbstractWidget {
    private final SingleToolWidget.PinWidget pinWidget;
    private final int defaultX;
    private final int defaultY;
    private final List<PinSwabWidget> swabs = new ArrayList<>();

    public PinPickerWidget(final int rightAnchor, final int centerY, final SingleToolWidget.PinWidget pinWidget) {
        super(rightAnchor, centerY, 0, 16, Component.literal("pin picker"));
        this.pinWidget = pinWidget;

        Pin.getTypes().forEach(pinType -> {
            this.width += 16;
            this.swabs.add(new PinSwabWidget(this, this.width - 16, 0, pinType));
        });

        this.defaultX = rightAnchor - this.width - 4;
        this.defaultY = centerY - this.height / 2 + 8;
        this.setX(this.defaultX);
        this.setY(this.defaultY);
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        TooltipRenderUtil.renderTooltipBackground(guiGraphics, this.getX(), this.getY(), this.width, this.height, 0);
        for (final PinSwabWidget swab : this.swabs) {
            swab.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean isMouseOver(final double mouseX, final double mouseY) {
        final int padding = 10;
        return this.isActive() && ((
                mouseX >= (double)this.getX() - padding
                        && mouseY >= (double)this.getY() - padding
                        && mouseX < (double)(this.getX() + this.width + padding)
                        && mouseY < (double)(this.getY() + this.height + padding)
        ) || this.swabs.stream().anyMatch(swab -> swab.isMouseOver(mouseX, mouseY))
        );
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        for (final PinSwabWidget swab : this.swabs) {
            if (swab.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public static class PinSwabWidget extends  AbstractWidget {
        private final PinPickerWidget parent;
        private final int relX;
        private final int relY;
        private final Pin.Type type;
        public PinSwabWidget(final PinPickerWidget parent, final int x, final int y, final Pin.Type type) {
            super(x, y, 16, 16, Component.literal("pin swab"));
            this.parent = parent;
            this.relX = x;
            this.relY = y;
            this.type = type;
        }

        @Override
        public int getX() {
            return this.parent.getX() + this.relX;
        }

        @Override
        public int getY() {
            return this.parent.getY() + this.relY;
        }

        @Override
        protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
            this.type.draw(guiGraphics, this.getX(), this.getY(), this.isMouseOver(mouseX, mouseY), false, 1);
        }

        @Override
        public void onClick(final double mouseX, final double mouseY) {
            final PinTool tool = this.parent.pinWidget.getTool();
            this.parent.pinWidget.last = this.type;
            tool.setPinType(this.type);
            WayfinderClient.TOOL_MANAGER.set(tool);
        }

        @Override
        protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {}
    }
}
