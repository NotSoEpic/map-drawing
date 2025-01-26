package wawa.wayfinder.input;

import net.minecraft.client.Minecraft;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.screen.MapScreen;

public class InputListener {
    public static void tick(Minecraft minecraft) {
        while (minecraft.level != null && KeyMappings.OPEN_MAP.consumeClick()) {
            minecraft.setScreen(new MapScreen());
        }
    }
}
