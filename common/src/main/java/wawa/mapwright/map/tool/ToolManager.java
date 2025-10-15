package wawa.mapwright.map.tool;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class ToolManager {
    private Tool tool;
    private Tool previous;

    public ToolManager(final @NotNull Tool def) {
        this.tool = def;
        this.previous = def;
    }

    public void set(final Tool tool) {
        this.tool.onDeselect();
        this.previous = this.tool;
        this.tool = tool;
        GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        this.tool.onSelect();
    }

    public Tool get() {
        return this.tool;
    }

    public void swap() {
        final Tool temp = this.tool;
        this.tool.onDeselect();
        this.tool = this.previous;
        this.tool.onSelect();
        this.previous = temp;
    }
}
