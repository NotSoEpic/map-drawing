package wawa.wayfinder.map.tool;

import org.joml.RoundingMode;
import org.joml.Vector2d;
import org.joml.Vector2i;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.MapScreen;

import java.util.function.Consumer;

public class DrawTool extends Tool {
    @Override
    public void hold(PageManager activePage, MapScreen.Mouse mouse, Vector2d oldWorld, Vector2d world) {
        switch (mouse) {
            case LEFT -> pixelLine(oldWorld, world, pos -> activePage.putPixel(pos.x, pos.y, -1));
            case RIGHT -> pixelLine(oldWorld, world, pos -> activePage.putPixel(pos.x, pos.y, 0));
        }
    }

    private void pixelLine(Vector2d point1, Vector2d point2, Consumer<Vector2i> perPixel) {
        Vector2d delta = new Vector2d(point1).sub(point2);
        int steps = (int) Math.max(1, Math.ceil(Math.max(Math.abs(delta.x), Math.abs(delta.y))));
        delta.div(steps);
        Vector2d pos = new Vector2d(point2);
        for (int i = 0; i < steps + 1; i++) {
            perPixel.accept(new Vector2i(pos.x + 0.5, pos.y + 0.5, RoundingMode.FLOOR));
            pos.add(delta);
        }
    }
}
