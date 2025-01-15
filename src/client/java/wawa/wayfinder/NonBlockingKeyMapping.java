package wawa.wayfinder;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

/**
 * A keybind implementation that doesn't block other keybinds
 * <p>
 * {@link KeyMapping#isDown()} and {@link KeyMapping#consumeClick()} will not work, use {@link KeyMapping#matches(int, int)} somewhere that passes in the appropriate values
 * <p>
 * Based off of <a href="https://github.com/mezz/JustEnoughItems/blob/1.21.x/Fabric/src/main/java/mezz/jei/fabric/input/FabricKeyMapping.java">JEI's implementation</a>
 */
public class NonBlockingKeyMapping extends KeyMapping {
    private InputConstants.Key realKey;
    public NonBlockingKeyMapping(String name,InputConstants.Type type, int keyCode, String category) {
        super(name, type, keyCode, category);
        realKey = KeyBindingHelper.getBoundKeyOf(this);
        super.setKey(InputConstants.UNKNOWN);
    }

    @Override
    public void setKey(InputConstants.Key key) {
        realKey = key;
    }

    @Override
    public boolean same(KeyMapping binding) {
        if (binding instanceof NonBlockingKeyMapping other) {
            return realKey.equals(other.realKey);
        }
        return false;
    }

    @Override
    public boolean isUnbound() {
        return realKey.equals(InputConstants.UNKNOWN);
    }

    @Override
    public boolean matches(int keysym, int scancode) {
        if (keysym != InputConstants.UNKNOWN.getValue()) {
            return realKey.getType() == InputConstants.Type.KEYSYM &&
                    realKey.getValue() == keysym;
        } else {
            return realKey.getType() == InputConstants.Type.SCANCODE &&
                    realKey.getValue() == scancode;
        }
    }

    @Override
    public boolean matchesMouse(int key) {
        return realKey.getType() == InputConstants.Type.MOUSE &&
                realKey.getValue() == key;
    }

    @Override
    public Component getTranslatedKeyMessage() {
        return realKey.getDisplayName();
    }

    @Override
    public boolean isDefault() {
        return realKey.equals(getDefaultKey());
    }

    @Override
    public String saveString() {
        return realKey.getName();
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
