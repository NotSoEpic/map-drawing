package wawa.mapwright.map.tool;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class PanTool extends Tool {
    public static PanTool INSTANCE = new PanTool();

    private PanTool() {
    }

    @Override
    public void onSelect() {
        GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
    }
}