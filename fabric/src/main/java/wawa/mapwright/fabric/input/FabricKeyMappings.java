package wawa.mapwright.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.platform.services.IKeyMappings;

public class FabricKeyMappings implements IKeyMappings {
    public static void init() {
    }

    private static KeyMapping create(final String name, final int keycode) {
        return new KeyMapping(
                "key." + MapwrightClient.MOD_ID + "." + name,
                InputConstants.Type.KEYSYM,
                keycode,
                "key.categories.mapwright"
        );
    }

    private static NonBlockingKeyMapping createNonBlocking(final String name, final int keycode) {
        return new NonBlockingKeyMapping(
                "key." + MapwrightClient.MOD_ID + "." + name,
                keycode,
                "key.categories.mapwright"
        );
    }

    public static KeyMapping
            OPEN_MAP = KeyBindingHelper.registerKeyBinding(create(
            "map", GLFW.GLFW_KEY_M)),

            UNDO = KeyBindingHelper.registerKeyBinding(createNonBlocking(
                    "undo", GLFW.GLFW_KEY_Z)
            .setKeyModifier(GLFW.GLFW_MOD_CONTROL)),

            REDO = KeyBindingHelper.registerKeyBinding(createNonBlocking(
                    "redo", GLFW.GLFW_KEY_Y)
            .setKeyModifier(GLFW.GLFW_MOD_CONTROL)),

            HAND = KeyBindingHelper.registerKeyBinding(createNonBlocking(
                    "hand", GLFW.GLFW_KEY_H
            )),

            PEN = KeyBindingHelper.registerKeyBinding(createNonBlocking(
                    "pen", GLFW.GLFW_KEY_N
            )),

            BRUSH = KeyBindingHelper.registerKeyBinding(createNonBlocking(
                    "brush", GLFW.GLFW_KEY_B
            )),

            ERASER = KeyBindingHelper.registerKeyBinding(createNonBlocking(
                    "eraser", GLFW.GLFW_KEY_E
            )),

            SWAP = KeyBindingHelper.registerKeyBinding(createNonBlocking(
                    "swap", GLFW.GLFW_KEY_X
            ));

    @Override
    public boolean consume(final Normal bind) {
        final KeyMapping key = switch (bind) {
            case OPEN_MAP -> OPEN_MAP;
            case UNDO -> UNDO;
            case REDO -> REDO;
            case SWAP -> SWAP;
        };
        return key.consumeClick();
    }

    @Override
    public boolean matches(final Normal bind, final int keysym, final int scancode, final int modifier) {
        final KeyMapping key = switch (bind) {
            case OPEN_MAP -> OPEN_MAP;
            case UNDO -> UNDO;
            case REDO -> REDO;
            case SWAP -> SWAP;
        };
        if (key instanceof final NonBlockingKeyMapping nonBlockingKeyMapping) {
            return nonBlockingKeyMapping.matchesWithModifier(keysym, scancode, modifier);
        } else {
            return key.matches(keysym, scancode);
        }
    }

    @Override
    public @Nullable ToolSwap getToolSwap(final int keysym, final int scancode, final int modifier) {
        if (HAND.matches(keysym, scancode)) {
            return ToolSwap.HAND;
        }
        if (PEN.matches(keysym, scancode)) {
            return ToolSwap.PEN;
        }
        if (BRUSH.matches(keysym, scancode)) {
            return ToolSwap.BRUSH;
        }
        if (ERASER.matches(keysym, scancode)) {
            return ToolSwap.ERASER;
        }
        return null;
    }
}

