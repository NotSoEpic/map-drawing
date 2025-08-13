package wawa.wayfinder.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector4dc;
import wawa.wayfinder.Helper;
import wawa.wayfinder.Rendering;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Stores path that a player takes
 */
public class PlayerPositions {
    private static final int MAX_POINTS = 60 * 10;
    private final Queue<Vector2d> positions = new ArrayDeque<>(MAX_POINTS);
    Vector2d current = new Vector2d();
    Vector2d newest = new Vector2d();
    int timer = 0;
    public boolean visible = true;

    public void tick(final Player player) {
        if (player == null)
            return;
        this.timer++;
        final Vector2d pos = new Vector2d(player.getX(), player.getZ());
        final double dist = pos.distanceSquared(this.newest);
        if (dist > 16 || (dist > 1 && this.timer > 20 * 10)) {
            this.timer = 0;
            while (this.positions.size() >= MAX_POINTS) {
                this.positions.remove();
            }
            this.positions.add(pos);
            this.newest = pos;
        }
        this.current = pos;
    }

    public void clear() {
        this.positions.clear();
        this.newest = this.current;
    }

    public void renderPositions(final GuiGraphics guiGraphics, final double xOff, final double yOff) {
        if (this.visible) {
            this.positions.forEach(v -> {
                final int x = (int) (v.x + xOff);
                final int y = (int) (v.y + yOff);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((v.x + xOff) - x, (v.y + yOff) - y, 0); // evil evil minecraft >:(
                guiGraphics.fill(x, y, x + 1, y + 1, 0xFFFF00FF);
                guiGraphics.pose().popPose();
            });
        }
    }

    public void renderHead(final GuiGraphics guiGraphics, final Vector2dc mouseScreen, final double xOff, final double yOff, final float scale, final Vector4dc worldBounds) {
        if (this.visible) {
            final Vector2d pos = new Vector2d(this.current).add(xOff, yOff).mul(scale); // world position to screen position
            final float alpha = Helper.getMouseProximityFade(mouseScreen, pos);
            Helper.clampWithin(pos, worldBounds);
            Rendering.renderPlayerIcon(guiGraphics, pos.x - 8, pos.y - 8, Minecraft.getInstance().player, alpha);
        }
    }
}
