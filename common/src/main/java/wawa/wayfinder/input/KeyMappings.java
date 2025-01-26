package wawa.wayfinder.input;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.WayfinderClient;

import java.util.ArrayList;
import java.util.List;

public class KeyMappings {
    public static final List<KeyMapping> toRegister = new ArrayList<>();
    public static final KeyMapping OPEN_MAP = create("map", GLFW.GLFW_KEY_M);

    private static KeyMapping create(String name, int keyCode) {
        KeyMapping mapping = new KeyMapping("key." + WayfinderClient.MOD_ID + "." + name, keyCode, "key.categories.wayfinder");
        toRegister.add(mapping);
        return mapping;
    }
    private static KeyMapping createNonBlocking(String name, int keyCode) {
        KeyMapping mapping = new NonBlockingKeyMapping("key." + WayfinderClient.MOD_ID + "." + name, keyCode, "key.categories.wayfinder");
        toRegister.add(mapping);
        return mapping;
    }
}
