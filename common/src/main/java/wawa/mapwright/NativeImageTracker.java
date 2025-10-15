package wawa.mapwright;

import com.mojang.blaze3d.platform.NativeImage;
import wawa.mapwright.mixin.NativeImageAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NativeImageTracker {
    private static final List<ImageAllocation> images = new ArrayList<>();
    public static boolean tracking = true;

    public static NativeImage newImage(final int width, final int height, final boolean calloc) {
        NativeImage image = new NativeImage(width, height, calloc);
        if (tracking) {
            images.add(new ImageAllocation(Thread.currentThread().getStackTrace(), image));
        }
        return image;
    }

    public static void checkAllocationAndClose() {
        int closed = 0;
        int unclosed = 0;
        for (ImageAllocation image : images) {
            if (((NativeImageAccessor)(Object)image.image).getPixels() != 0) {
                MapwrightClient.LOGGER.warn("Native image was not closed. Initialised at: {}", Arrays.toString(image.stackTrace));
                unclosed++;
//                image.image.close(); // oops... race error
            } else {
                closed++;
            }
        }
        MapwrightClient.LOGGER.debug("{} unclosed images, {} closed images", unclosed, closed);
        images.clear();
    }

    public record ImageAllocation(StackTraceElement[] stackTrace, NativeImage image) {}
}
