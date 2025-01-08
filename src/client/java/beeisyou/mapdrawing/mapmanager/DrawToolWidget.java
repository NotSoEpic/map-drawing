package beeisyou.mapdrawing.mapmanager;

import beeisyou.mapdrawing.MapScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class DrawToolWidget extends ClickableWidget {
    private final MapScreen parent;
    private final int color;
    private final int size;
    public DrawToolWidget(MapScreen parent, int x, int y, int width, int height, int color, int size) {
        super(x, y, width, height, Text.of("draw tool"));
        this.parent = parent;
        this.color = color;
        this.size = size;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        parent.color = color;
        parent.size = size;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getRight(), getBottom(), color);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
