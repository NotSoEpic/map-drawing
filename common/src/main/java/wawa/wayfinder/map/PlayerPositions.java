package wawa.wayfinder.map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2d;

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

    public void tick(Player player) {
        if (player == null)
            return;
        timer++;
        Vector2d pos = new Vector2d(player.getX(), player.getZ());
        double dist = pos.distanceSquared(newest);
        if (dist > 16 || (dist > 1 && timer > 20 * 10)) {
            timer = 0;
            while (positions.size() >= MAX_POINTS) {
                positions.remove();
            }
            positions.add(pos);
            newest = pos;
        }
        current = pos;
    }

    public void clear() {
        positions.clear();
        newest = current;
    }

    public void render(GuiGraphics guiGraphics, int xOff, int yOff) {
        if (visible) {
            positions.forEach(v -> guiGraphics.fill((int) v.x + xOff, (int) v.y + yOff, (int) v.x + 1 + xOff, (int) v.y + 1 + yOff, 0xFFFF00FF));
            guiGraphics.fill((int) current.x - 1 + xOff, (int) current.y - 1 + yOff, (int) current.x + 2 + xOff, (int) current.y + 2 + yOff, 0xFFFF00FF);
        }
    }
}
