package wawa.wayfinder.mapmanager;

import wawa.wayfinder.RenderHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector2d;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Stores the previous positions of the players to be rendered in the map
 */
public class PlayerMovementHistory {
    public Queue<Vector2d> positions = new ArrayDeque<>(6 * 10);
    Vector2d newest = new Vector2d();
    int timer = 0;

    public void tick(ClientPlayerEntity player) {
        if (player == null)
            return;
        timer++;
        Vector2d playerPos = new Vector2d(player.getX(), player.getZ());
        if (playerPos.distance(newest) > 1 && (timer > 20 * 10 || playerPos.distance(newest) > 16)) {
            timer = 0;
            while (positions.size() >= 6 * 10) {
                positions.remove();
            }
            positions.add(new Vector2d(player.getX(), player.getZ()));
            newest = playerPos;
        }
    }

    public void render(DrawContext context, MapWidget manager) {
        positions.stream().forEach(v -> {
            Vector2d p = manager.worldToScreen(v.x, v.y, true);
            if (p.x > 0 && p.x < manager.getWidth() && p.y > 0 && p.y < manager.getHeight()) {
                RenderHelper.fill(context, p.x - 1, p.y - 1, p.x + 1, p.y + 1,
                        ColorHelper.getArgb(255, 255, 0));
            }
        });
    }

    public void clear() {
        positions.clear();
    }
}
