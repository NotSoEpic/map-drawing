package wawa.wayfinder;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2d;
import org.joml.Vector4dc;

public class Helper {
    public static Vec2 preciseMousePos() {
        final MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        final Window window = Minecraft.getInstance().getWindow();
        return new Vec2(
                (float) (mouse.xpos() * window.getGuiScaledWidth() / window.getScreenWidth()),
                (float) (mouse.ypos() * window.getGuiScaledHeight() / window.getScreenHeight())
        );
    }

    /**
     * Clamps value between two vectors stored in a Vector4dc
     * @param value vector to be mutated
     * @param bounds used as (minX, minY, maxX, maxY)
     * @return value for chaining
     */
    public static Vector2d clampWithin(final Vector2d value, final Vector4dc bounds) {
        value.x = Math.clamp(value.x, bounds.x(), bounds.z());
        value.y = Math.clamp(value.y, bounds.y(), bounds.w());
        return value;
    }
}
