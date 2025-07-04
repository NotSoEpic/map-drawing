package wawa.wayfinder.map;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2d;
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

    public void render(final GuiGraphics guiGraphics, final int xOff, final int yOff) {
        if (this.visible) {
            this.positions.forEach(v -> guiGraphics.fill((int) v.x + xOff, (int) v.y + yOff, (int) v.x + 1 + xOff, (int) v.y + 1 + yOff, 0xFFFF00FF));

            int x = (int) (this.current.x + xOff);
            int y = (int) (this.current.y + yOff);

            Rendering.renderPlayerIcon(guiGraphics, x - 8, y - 8, Minecraft.getInstance().player);
        }
    }
}
