package wawa.wayfinder.map.tool;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.MapScreen;

public abstract class Tool {
    private static Tool tool;

    public static void set(@Nullable Tool tool) {
        if (Tool.tool != null) {
            Tool.tool.onDeselect();
        }
        Tool.tool = tool;
        if (tool != null) {
            tool.onSelect();
        }
    }

    @Nullable
    public static Tool get() {
        return tool;
    }

    public void onSelect() {}

    public void onDeselect() {}

    public void hold(PageManager activePage, MapScreen.Mouse mouse, Vector2d oldWorld, Vector2d world) {}
}
