package wawa.wayfinder.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import wawa.wayfinder.input.KeyMappings;

public class MapScreen extends Screen {
    public MapScreen() {
        super(Component.literal("Wayfinder Map"));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyMappings.OPEN_MAP.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
