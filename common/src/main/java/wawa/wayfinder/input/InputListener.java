package wawa.wayfinder.input;

import net.minecraft.client.Minecraft;
import org.joml.Vector2d;
import wawa.wayfinder.map.MapScreen;

public class InputListener {
    public static void tick(Minecraft minecraft) {
        while (minecraft.level != null && KeyMappings.OPEN_MAP.consumeClick()) {
            Vector2d target = new Vector2d(minecraft.player.getBlockX(), minecraft.player.getBlockZ());
            minecraft.setScreen(new MapScreen(target.x, target.y));
        }
    }
}
