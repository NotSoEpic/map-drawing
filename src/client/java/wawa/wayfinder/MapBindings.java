package wawa.wayfinder;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.mapmanager.MapScreen;

public class MapBindings {
    public static final KeyMapping
            openMap = register("open_map", GLFW.GLFW_KEY_M),
    undo = register("undo", GLFW.GLFW_KEY_Z),
    swap_tool = register("swap_tool", GLFW.GLFW_KEY_X),
    pencil = register("pencil", GLFW.GLFW_KEY_N),
    brush = register("brush", GLFW.GLFW_KEY_B),
    eraser = register("eraser", GLFW.GLFW_KEY_E),
    ruler = register("ruler", GLFW.GLFW_KEY_R);

    private static KeyMapping register(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.wayfinder." + name,
                InputConstants.Type.KEYSYM,
                key,
                "category.wayfinder.map"
        ));
    }

    public static void inputEvents(Minecraft client) {
        while (openMap.consumeClick()) {
            client.setScreen(new MapScreen());
        }
    }

    public static void init() {}
}
