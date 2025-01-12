package wawa.wayfinder;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.mapmanager.MapScreen;

public class MapBindings {
    public static final KeyMapping openMap = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.wayfinder.open_map",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "category.wayfinder.map"
    ));

    public static final KeyMapping undo = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.wayfinder.undo",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "category.wayfinder.map"
    ));

    public static void inputEvents(Minecraft client) {
        while (openMap.consumeClick()) {
            client.setScreen(new MapScreen());
        }
    }

    public static void init() {}
}
