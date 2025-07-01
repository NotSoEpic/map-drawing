package wawa.wayfinder.map.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import wawa.wayfinder.Helper;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.tool.DrawTool;
import wawa.wayfinder.map.tool.Tool;

import java.util.ArrayList;
import java.util.List;

public class ToolPickerWidget extends AbstractWidget {
    private final List<SingleToolWidget> tools = new ArrayList<>();
    private final DrawTool pencil = new DrawTool(0xFF000000);
    private final SingleToolWidget.Brush brush;
    private final DrawTool eraser = new DrawTool(0);
    public ToolPickerWidget(final int x, final int y) {
        super(x, y, 0, 0, Component.literal("tool picker"));
        this.pencil.icon = WayfinderClient.id("cursor/pencil");
        this.tools.add(new SingleToolWidget(
                this.getX(), this.getY(),
                WayfinderClient.id("tool/pencil"),
                WayfinderClient.id("tool/pencil_highlight"),
                (w) -> this.pencil,
                Component.literal("pencil")
        ));
        this.brush = new SingleToolWidget.Brush(this.getX(), this.getY() + 20);
        this.tools.add(this.brush);
        this.eraser.icon = WayfinderClient.id("cursor/eraser");
        this.tools.add(new SingleToolWidget(
                this.getX(), this.getY() + 40,
                WayfinderClient.id("tool/eraser"),
                WayfinderClient.id("tool/eraser_highlight"),
                (w) -> this.eraser,
                Component.literal("eraser")
        ));
        this.updateBounds();
    }

    public void pickPencil() {
        Tool.set(this.pencil);
    }

    public void pickBrush() {
        if (Tool.get() == this.brush.last) {
            final Vec2 mouse = Helper.preciseMousePos();
            this.brush.openToMouse((int)mouse.x, (int)mouse.y);
        } else {
            Tool.set(this.brush.last);
        }
    }

    public void pickColor(final int color) {
        if (color == 0xFF000000) {
            Tool.set(this.pencil);
        } else {
            for (final DrawTool tool : this.brush.getBrushes()) {
                if (tool.getInternalColor() == color) {
                    Tool.set(tool);
                    return;
                }
            }
        }
    }

    private void updateBounds() {
        int left = 100000;
        int top = 100000;
        int right = 0;
        int bottom = 0;
        for (final SingleToolWidget tool : this.tools) {
            left = Math.min(left, tool.getX());
            top = Math.min(top, tool.getY());
            right = Math.max(right, tool.getRight());
            bottom = Math.max(bottom, tool.getBottom());
        }
        this.setX(left);
        this.setY(top);
        this.setWidth(right - left);
        this.setHeight(bottom - top);
    }

    @Override
    public boolean isMouseOver(final double mouseX, final double mouseY) {
        return this.tools.stream().anyMatch(tool -> tool.isMouseOver(mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        for (final SingleToolWidget tool : this.tools) {
            if (tool.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void renderWidget(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        for (final SingleToolWidget tool : this.tools) {
            tool.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {}
}
