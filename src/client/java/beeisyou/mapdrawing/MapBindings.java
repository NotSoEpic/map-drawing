package beeisyou.mapdrawing;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MapBindings {
    public static final KeyBinding openMap = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.map_drawing.open_map",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "category.map_drawing.map"
    ));

    public static void inputEvents(MinecraftClient client) {
        while (openMap.wasPressed()) {
            client.setScreen(new MapScreen());
        }
    }

    public static void init() {}
}
