package wawa.wayfinder.map.stamp_bag;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import wawa.wayfinder.NativeImageTracker;
import wawa.wayfinder.mixin.NativeImageAccessor;

import java.util.Collection;

/**
 * Developer exposed information for a stamp. file name, custom name, and favoritism is populated when initialized through {@link StampBagHandler}. <p>
 * Requested image is populated when called through {@link StampBagHandler#bulkRequestStamps(Collection, int...)}
 */
public final class StampInformation {

    public static final Codec<StampInformation> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.STRING.fieldOf("file_name").forGetter(StampInformation::getFileName),
                    Codec.STRING.fieldOf("custom_name").forGetter(StampInformation::getCustomName),
                    Codec.BOOL.fieldOf("favorite").forGetter(StampInformation::isFavorited))
            .apply(i, (f, c, fav) -> new StampInformation(f, c, fav, null)));

    /**
     * The name this stamp is saved to disk under
     */
    private final String fileName;

    /**
     * The custom name given to this stamp when initially saved
     */
    private final String customName;

    /**
     * Whether this stamp is favorited
     */
    private boolean favorited;

    /**
     * The requested image associated with this stamp. <p/>
     * Null until {@link StampBagHandler#bulkRequestStamps(Collection, int...)} is called with the index associated with this SI.
     */
    private @Nullable NativeImage requestedImage;

    private int ticksSinceImageGet = 0;

    public StampInformation(String fileName, String customName, boolean favorite, @Nullable NativeImage requestedImage) {
        this.fileName = fileName;
        this.customName = customName;

        this.favorited = favorite;
        this.requestedImage = requestedImage;
    }

    public void tick() {
//        if (ticksSinceImageGet != -1 && ticksSinceImageGet++ >= (20 * 60 * 5)) {
//            setRequestedImage(null);
//        }
    }

    public String getFileName() {
        return fileName;
    }

    public String getCustomName() {
        return customName;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public void resetCloseCounter() {
        ticksSinceImageGet = 0;
    }

    public @Nullable NativeImage getRequestedImage() {
        //lazy checking of native image in case mojang decides it's time to close this for no reason
        if (requestedImage == null) {
            return null;
        }

        if (((NativeImageAccessor) (Object) requestedImage).getPixels() == 0L) {
            setRequestedImage(null);
            return null;
        }

        return requestedImage;
    }

    public void setRequestedImage(@Nullable NativeImage newImage) {
        if (newImage != this.requestedImage) {
            if (newImage == null) {
                ticksSinceImageGet = -1;
                this.requestedImage.close();
            } else {
                ticksSinceImageGet = 0;
//                NativeImageTracker.newImage(newImage.getWidth(), newImage.getHeight(), false);
            }

            this.requestedImage = newImage;
        }
    }
}
