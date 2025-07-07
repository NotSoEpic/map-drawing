package wawa.wayfinder.map.tool;

import wawa.wayfinder.map.widgets.SingleToolWidget;

public class PaletteDrawTool extends DrawTool {
    private final SingleToolWidget.Brush brushPicker;
    public PaletteDrawTool(final int color, final int visual_color, final SingleToolWidget.Brush brushPicker) {
        super(color, visual_color);
        this.brushPicker = brushPicker;
    }

    @Override
    public void onSelect() {
        super.onSelect();
        this.brushPicker.last = this;
    }
}
