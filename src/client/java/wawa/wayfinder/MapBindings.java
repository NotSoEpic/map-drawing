package wawa.wayfinder;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.mapmanager.MapScreen;

public class MapBindings {
    public static final KeyMapping
            OPEN_MAP = register("open_map", GLFW.GLFW_KEY_M),
    UNDO = registerNonBlocking("undo", GLFW.GLFW_KEY_Z),
    SWAP_TOOL = registerNonBlocking("swap_tool", GLFW.GLFW_KEY_X),
    PENCIL = registerNonBlocking("pencil", GLFW.GLFW_KEY_N),
    BRUSH = registerNonBlocking("brush", GLFW.GLFW_KEY_B),
    ERASER = registerNonBlocking("eraser", GLFW.GLFW_KEY_E),
    RULER = registerNonBlocking("ruler", GLFW.GLFW_KEY_R),
    SCISSORS = registerNonBlocking("scissors", GLFW.GLFW_KEY_C);

    private static KeyMapping register(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.wayfinder." + name,
                InputConstants.Type.KEYSYM,
                key,
                "category.wayfinder.map"
        ));
    }

    private static KeyMapping registerNonBlocking(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(new NonBlockingKeyMapping(
                "key.wayfinder." + name,
                InputConstants.Type.KEYSYM,
                key,
                "category.wayfinder.map"
        ));
    }

    public static void inputEvents(Minecraft client) {
        while (OPEN_MAP.consumeClick()) {
            client.setScreen(new MapScreen());
        }
    }

    public static void init() {}
}
