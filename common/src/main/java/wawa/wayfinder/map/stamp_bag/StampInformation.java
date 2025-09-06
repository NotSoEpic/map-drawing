package wawa.wayfinder.map.stamp_bag;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
            .apply(i, StampInformation::new));

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
    private final @NotNull StampTexture stampTexture;

    public StampInformation(String fileName, String customName, boolean favorite, NativeImage newStamp) {
        this(fileName, customName, favorite);
        stampTexture.setFirstStamp(newStamp);
    }

    public StampInformation(String fileName, String customName, boolean favorite) {
        this.fileName = fileName;
        this.customName = customName;

        this.favorited = favorite;
        this.stampTexture = new StampTexture();
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

    public @NotNull StampTexture getStampTexture() {
        return stampTexture;
    }

    public void setStampTexture(@Nullable NativeImage newImage) {
        NativeImage texture = stampTexture.getTexture();
        if (texture == null) {
            stampTexture.setFirstStamp(newImage);
        }
    }
}
