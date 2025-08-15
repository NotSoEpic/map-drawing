package wawa.wayfinder.map.tool;

import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2ic;
import wawa.wayfinder.data.PageManager;
import wawa.wayfinder.map.widgets.SingleToolWidget;

public class PaletteDrawTool extends DrawTool {
    private final SingleToolWidget.BrushWidget brushWidgetPicker;
    public PaletteDrawTool(final ResourceLocation icon, final int color, final int visual_color, final SingleToolWidget.BrushWidget brushWidgetPicker) {
        super(icon, color, visual_color);
        this.brushWidgetPicker = brushWidgetPicker;
    }

    @Override
    public void onSelect() {
        super.onSelect();
        this.brushWidgetPicker.last = this;
    }

    @Override
    public void putSquare(final PageManager activePage, final Vector2ic pos, final int targetColor) {
        activePage.startSnapshot();
        activePage.putConditionalSquare(pos.x(), pos.y(), targetColor, this.getRadius(), c -> c != 0xFF000000);
    }

    @Override
    public void removeSquare(final PageManager activePage, final Vector2ic pos, final int targetColor) {
        activePage.startSnapshot();
        activePage.putConditionalSquare(pos.x(), pos.y(), targetColor, this.getRadius(), c -> c != 0xFF000000);
    }
}
