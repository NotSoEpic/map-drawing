package wawa.wayfinder.data.pages;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An Unloaded region of a WayFinder map
 */
public class UnloadedPage extends AbstractPage {

    /**
     * A path to the associated image.
     */
    @NotNull
    Path associatedImage;

    public UnloadedPage(final int rx, final int ry, final PageManager manager) {
        super(rx, ry, manager);

        this.associatedImage = manager.pageIO.pageFilepath(rx, ry);
    }

    /**
     * Attempts to load the path associated with this region and inserts it into a LoadedPage. If no image is present, returns a LoadedPage without an image.
     *
     * @return Null if loading failed; LoadedPage on image load or new load.
     */
    @NotNull
    public LoadedPage attemptToLoad() {
        final LoadedPage page = new LoadedPage(this.rx, this.ry, this.manager);

        Util.ioPool().execute(() -> {
            final File file = new File(this.associatedImage.toUri());

            //If the image is available, attempt to load it. if not, create a new loaded page without an image
            NativeImage gatheredImage = null;
            if (file.isFile()) {
                try {
                    final InputStream inputStream = Files.newInputStream(this.associatedImage);
                    gatheredImage = NativeImage.read(inputStream);
                } catch (final IOException e) {
                    WayfinderClient.LOGGER.error("Failed to load image {}\n{}", this.associatedImage, e);
                    page.failedToLoad = true;
                }
            }

            if (gatheredImage != null) {
                page.setImageExternally(gatheredImage);
            }
        });

        return page;
    }

    @Override
    public boolean isUnloaded() {
        return true;
    }
}
