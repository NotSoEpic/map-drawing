package wawa.wayfinder.platform.services;

import org.jetbrains.annotations.Nullable;

public interface IKeyMappings {
    enum Normal {
        OPEN_MAP,
        UNDO,
        SWAP
    }

    boolean consume(Normal bind);
    boolean matches(Normal bind, final int keysym, final int scancode, final int modifier);

    enum ToolSwap {
        BRUSH,
        PENCIL
    }

    @Nullable
    ToolSwap getToolSwap(final int keysym, final int scancode, final int modifier);
}
