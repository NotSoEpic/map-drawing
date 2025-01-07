package beeisyou.mapdrawing.mapmanager;

import beeisyou.mapdrawing.MapScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class ColorElement extends ClickableWidget {
    int color;
    MapScreen parent;
    public ColorElement(int x, int y, int width, int height, int color, MapScreen parent) {
        super(x, y, width, height, Text.of("awawa"));
        this.color = color;
        this.parent = parent;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        parent.color = color;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getRight(), getBottom(), color);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
