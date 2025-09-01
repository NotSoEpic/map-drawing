package wawa.wayfinder.neoforge.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.map.StampBagScreen;
import wawa.wayfinder.platform.services.IKeyMappings;

public class NeoKeyMappings implements IKeyMappings {
    private static KeyMapping create(final String name, final KeyConflictContext conflictContext, final KeyModifier modifier, final int keycode) {
        return new KeyMapping(
                "key." + WayfinderClient.MOD_ID + "." + name,
                conflictContext,
                modifier,
                InputConstants.Type.KEYSYM,
                keycode,
                "key.categories.wayfinder"
        );
    }

    public static final Lazy<KeyMapping>
            OPEN_MAP = Lazy.of(() -> create(
            "map", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, GLFW.GLFW_KEY_M)),

            UNDO = Lazy.of(() -> create(
                    "undo", KeyConflictContext.GUI, KeyModifier.CONTROL, GLFW.GLFW_KEY_Z)),

            REDO = Lazy.of(() -> create(
                    "redo", KeyConflictContext.GUI, KeyModifier.CONTROL, GLFW.GLFW_KEY_Y)),

            SWAP = Lazy.of(() -> create(
                    "swap", KeyConflictContext.GUI, KeyModifier.NONE, GLFW.GLFW_KEY_X));

    @Override
    public boolean consume(final Normal bind) {
        final Lazy<KeyMapping> mapping = switch (bind) {
            case OPEN_MAP -> OPEN_MAP;
            case UNDO -> UNDO;
            case REDO -> REDO;
            case SWAP -> SWAP;
        };

        return mapping.get().consumeClick();
    }

    @Override
    public boolean matches(final Normal bind, final int keysym, final int scancode, final int modifier) {
        final Lazy<KeyMapping> mapping = switch (bind) {
            case OPEN_MAP -> OPEN_MAP;
            case UNDO -> UNDO;
            case REDO -> REDO;
            case SWAP -> SWAP;
        };
        return mapping.get().isActiveAndMatches(InputConstants.getKey(keysym, scancode));
    }

    public static final Lazy<KeyMapping>
            HAND = Lazy.of(() -> create(
                "hand", KeyConflictContext.GUI, KeyModifier.NONE, GLFW.GLFW_KEY_H
            )),
            PENCIL = Lazy.of(() -> create(
                "pencil", KeyConflictContext.GUI, KeyModifier.NONE, GLFW.GLFW_KEY_N
            )),
            BRUSH = Lazy.of(() -> create(
                "brush", KeyConflictContext.GUI, KeyModifier.NONE, GLFW.GLFW_KEY_B
            )),
            ERASER = Lazy.of(() -> create(
                "eraser", KeyConflictContext.GUI, KeyModifier.NONE, GLFW.GLFW_KEY_E
            ));

    @Override
    public ToolSwap getToolSwap(final int keysym, final int scancode, final int modifier) {
        final InputConstants.Key key = InputConstants.getKey(keysym, scancode);
        if (HAND.get().isActiveAndMatches(key)) {
            return ToolSwap.HAND;
        }
        if (PENCIL.get().isActiveAndMatches(key)) {
            return ToolSwap.PENCIL;
        }
        if (BRUSH.get().isActiveAndMatches(key)) {
            return ToolSwap.BRUSH;
        }
        if (ERASER.get().isActiveAndMatches(key)) {
            return ToolSwap.ERASER;
        }
        return null;
    }
}
