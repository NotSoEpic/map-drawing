package wawa.wayfinder.input;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.widgets.ToolPickerWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class KeyMappings {

    public static final List<KeyMapping> toRegister = new ArrayList<>();

    /**
     * Normal KeyMappings used for opening maps etc
     */
    public enum NormalMappings {
        OPEN_MAP("map", GLFW.GLFW_KEY_M, true),
        UNDO("undo", GLFW.GLFW_KEY_Z, false),
        SWAP("swap", GLFW.GLFW_KEY_X, false);

        final public KeyMapping mapping;

        NormalMappings(final String name, final int keyCode, final boolean blocking) {
            if (blocking) {
                this.mapping = new KeyMapping("key." + WayfinderClient.MOD_ID + "." + name, keyCode, "key.categories.wayfinder");
            } else {
                this.mapping = new NonBlockingKeyMapping("key." + WayfinderClient.MOD_ID + "." + name, keyCode, "key.categories.wayfinder");
            }

            toRegister.add(this.mapping);
        }
    }

    /**
     * Specific ToolPickerKeyMappings with associated consumers for switching to the specified tool
     */
    public enum ToolPickerMappings {
        PENCIL("pencil", GLFW.GLFW_KEY_N, ToolPickerWidget::pickPencil),
        BRUSH("brush", GLFW.GLFW_KEY_B, ToolPickerWidget::pickBrush);

        final public KeyMapping mapping;
        final Consumer<ToolPickerWidget> swapper;

        ToolPickerMappings(final String name, final int keyCode, final Consumer<ToolPickerWidget> swapper) {
            this.mapping = new NonBlockingKeyMapping("key." + WayfinderClient.MOD_ID + "." + name, keyCode, "key.categories.wayfinder");
            toRegister.add(this.mapping);
            this.swapper = swapper;
        }

        public void swapToTool(final ToolPickerWidget widget) {
            this.swapper.accept(widget);
        }
    }
}

