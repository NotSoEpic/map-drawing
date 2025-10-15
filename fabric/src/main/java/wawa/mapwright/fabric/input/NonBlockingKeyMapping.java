package wawa.mapwright.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import wawa.mapwright.mixin.KeyBindingAccessor;

/**
 * A keybind implementation that doesn't block other keybinds
 * <p>
 * {@link KeyMapping#isDown()} and {@link KeyMapping#consumeClick()} will not work, use {@link KeyMapping#matches(int, int)} somewhere that passes in the appropriate values
 * <p>
 * Based off of <a href="https://github.com/mezz/JustEnoughItems/blob/1.21.x/Fabric/src/main/java/mezz/jei/fabric/input/FabricKeyMapping.java">JEI's implementation</a><br>
 * Also supports a key modifier
 */
public class NonBlockingKeyMapping extends KeyMapping {
    private InputConstants.Key realKey;
    private int keyModifier = 0;
    public NonBlockingKeyMapping(final String name, final int keyCode, final String category) {
        super(name, keyCode, category);
        this.realKey = ((KeyBindingAccessor)this).getKey();
        super.setKey(InputConstants.UNKNOWN);
    }

    public NonBlockingKeyMapping setKeyModifier(final int modifier) {
        this.keyModifier = modifier;
        return this;
    }

    @Override
    public void setKey(final InputConstants.Key key) {
        this.realKey = key;
    }

    @Override
    public boolean same(final KeyMapping binding) {
        if (binding instanceof final NonBlockingKeyMapping other) {
            return this.realKey.equals(other.realKey);
        }
        return false;
    }

    @Override
    public boolean isUnbound() {
        return this.realKey.equals(InputConstants.UNKNOWN);
    }

    @Override
    public boolean matches(final int keysym, final int scancode) {
        if (keysym != InputConstants.UNKNOWN.getValue()) {
            return this.realKey.getType() == InputConstants.Type.KEYSYM &&
                    this.realKey.getValue() == keysym;
        } else {
            return this.realKey.getType() == InputConstants.Type.SCANCODE &&
                    this.realKey.getValue() == scancode;
        }
    }

    public boolean matchesWithModifier(final int keysym, final int scancode, final int modifier) {
        return this.matches(keysym, scancode) && (this.keyModifier == 0 || (this.keyModifier & modifier) != 0);
    }

    @Override
    public boolean matchesMouse(final int key) {
        return this.realKey.getType() == InputConstants.Type.MOUSE &&
                this.realKey.getValue() == key;
    }

    @Override
    public Component getTranslatedKeyMessage() {
        if (this.keyModifier == GLFW.GLFW_MOD_CONTROL) { // this is the point where i give up making it generalized
            return Component.literal("CTRL + ").append(this.realKey.getDisplayName());
        }
        return this.realKey.getDisplayName();
    }

    @Override
    public boolean isDefault() {
        return this.realKey.equals(this.getDefaultKey());
    }

    @Override
    public String saveString() {
        return this.realKey.getName();
    }

    @Override
    public boolean isDown() {
        throw new AssertionError();
    }

    @Override
    public boolean consumeClick() {
        throw new AssertionError();
    }
}