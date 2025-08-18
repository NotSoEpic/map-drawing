package wawa.wayfinder.map.tool;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2d;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.data.Pin;
import wawa.wayfinder.map.widgets.MapWidget;

public class PinTool extends Tool {
    public Pin.Type currentPin = Pin.DEFAULT;

    public void setPinType(final Pin.Type newPin) {
        this.currentPin = newPin;
    }

    @Override
    public void mouseDown(final PageManager activePage, final MapWidget.MouseType mouseType, final Vector2d world) {
        if (mouseType == MapWidget.MouseType.LEFT) {
            activePage.putPin(this.currentPin, world);
        } else if (mouseType == MapWidget.MouseType.RIGHT) {
            activePage.removePin(this.currentPin);
        }
    }

    @Override
    public void renderScreen(final GuiGraphics graphics, final double mouseX, final double mouseY) {
        if (this.currentPin != null) {
            this.currentPin.draw(graphics, mouseX, mouseY, false, true, 1);
        }
    }
}
