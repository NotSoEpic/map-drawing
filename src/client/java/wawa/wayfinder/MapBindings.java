package wawa.wayfinder;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.mapmanager.MapScreen;

public class MapBindings {
    public static final KeyMapping openMap = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.way_finder.open_map",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "category.way_finder.map"
    ));

    public static void inputEvents(Minecraft client) {
        while (openMap.consumeClick()) {
            client.setScreen(new MapScreen());
        }
    }

    public static void init() {}
}
