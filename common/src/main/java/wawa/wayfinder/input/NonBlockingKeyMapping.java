package wawa.wayfinder.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import wawa.wayfinder.mixin.KeyBindingAccessor;

/**
 * A keybind implementation that doesn't block other keybinds
 * <p>
 * {@link KeyMapping#isDown()} and {@link KeyMapping#consumeClick()} will not work, use {@link KeyMapping#matches(int, int)} somewhere that passes in the appropriate values
 * <p>
 * Based off of <a href="https://github.com/mezz/JustEnoughItems/blob/1.21.x/Fabric/src/main/java/mezz/jei/fabric/input/FabricKeyMapping.java">JEI's implementation</a>
 */
public class NonBlockingKeyMapping extends KeyMapping {
    private InputConstants.Key realKey;
    public NonBlockingKeyMapping(final String name, final int keyCode, final String category) {
        super(name, keyCode, category);
        this.realKey = ((KeyBindingAccessor)this).getKey();
        super.setKey(InputConstants.UNKNOWN);
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

    @Override
    public boolean matchesMouse(final int key) {
        return this.realKey.getType() == InputConstants.Type.MOUSE &&
                this.realKey.getValue() == key;
    }

    @Override
    public Component getTranslatedKeyMessage() {
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