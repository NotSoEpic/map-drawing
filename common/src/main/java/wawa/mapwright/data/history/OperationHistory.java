package wawa.mapwright.data.history;

import com.mojang.blaze3d.platform.NativeImage;
import org.joml.Vector2i;

import java.util.Map;

public record OperationHistory(Map<Vector2i, NativeImage> pagesModified) {
    public void clear() {
        pagesModified.forEach((key, value) -> {
            value.close();
        });
        pagesModified.clear();
    }
}
