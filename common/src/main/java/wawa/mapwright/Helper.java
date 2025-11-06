package wawa.mapwright;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2d;
import org.joml.Vector2dc;
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
        if (bounds.x() > bounds.z() || bounds.y() > bounds.w()) {
            return new Vector2d(bounds.x(), bounds.y());
        }
        value.x = Math.clamp(value.x, bounds.x(), bounds.z());
        value.y = Math.clamp(value.y, bounds.y(), bounds.w());
        return value;
    }

    public static boolean isUsingSpyglass(final Player player) {
        return player.isUsingItem() && player.getItemInHand(player.getUsedItemHand()).is(Items.SPYGLASS);
    }

    public static float getMouseProximityFade(final Vector2dc mouseScreen, final Vector2dc posScreen, final float startDist) {
        final double dist = mouseScreen.distance(posScreen);
        if (dist < startDist) {
            return Mth.clamp(1.5f * (float) (dist / startDist) - 0.5f, 0.11f, 1);
        } else {
            return  1;
        }
    }
}
