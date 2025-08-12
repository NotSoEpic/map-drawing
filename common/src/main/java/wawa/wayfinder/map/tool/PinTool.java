package wawa.wayfinder.map.tool;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2d;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.data.Pin;
import wawa.wayfinder.map.widgets.MapWidget;

public class PinTool extends Tool {
    private boolean isHeld;
    public Pin.Type currentPin = Pin.TYPES.entrySet().stream().findFirst().get().getValue();

    public void setPinType(final Pin.Type newPin) {
        this.currentPin = newPin;
    }

    @Override
    public void hold(final PageManager activePage, final MapWidget.Mouse mouse, final Vector2d oldWorld, final Vector2d world) {
        if (mouse == MapWidget.Mouse.LEFT) {
            if (!this.isHeld) {
                this.isHeld = true;

                activePage.putPin(this.currentPin, world);
            }
        }
    }

    @Override
    public void release(final PageManager activePage) {
        this.isHeld = false;
    }

    @Override
    public void renderScreen(final GuiGraphics graphics, final double mouseX, final double mouseY) {
        if (this.currentPin != null) {
            this.currentPin.draw(graphics, (int) mouseX, (int) mouseY, false, true);
        }
    }
}
