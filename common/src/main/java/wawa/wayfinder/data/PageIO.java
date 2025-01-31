package wawa.wayfinder.data;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.mixin.BiomeManagerAccessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Holds file path information and helper functions for reading and writing image data
 */
public class PageIO {
    private final Path pagePath;

    public PageIO(Level level, Minecraft client) {
        pagePath = buildMapPath(level, client);
        try {
            Files.createDirectories(pagePath);
        } catch (IOException e) {
            WayfinderClient.LOGGER.error("Could not create map directory\n{}", e);
        }
    }

    /**
     * @return "minecraftinstance/wayfinder_maps/singleplayer/uuid_worldname"
     */
    private Path buildMapPath(Level level, Minecraft client) {
        Path path = client.gameDirectory.toPath()
                .resolve("wayfinder_maps");
        long seed = ((BiomeManagerAccessor) level.getBiomeManager()).getBiomeZoomSeed();
        UUID uuid = Mth.createInsecureUUID(RandomSource.create(seed));
        if (client.isLocalServer()) {
            path = path.resolve("singleplayer")
                    .resolve(uuid + "_" + client.getSingleplayerServer().getWorldData().getLevelName());
        } else {
            path = path.resolve("multiplayer")
                    .resolve(uuid + "_" + client.getCurrentServer().name);
        }
        return path.resolve(level.dimension().location().toDebugFileName());
    }

    public Path getPagePath() {
        return pagePath;
    }

    public Path pageFilepath(int rx, int ry) {
        return pagePath.resolve(String.format("%d_%d.png", rx, ry));
    }

    /**
     * Attempts to load an image. Should be run via {@link Util#ioPool()}
     * @return null if file doesn't exist or there was an error loading
     */
    public @Nullable NativeImage tryLoadImage(int rx, int ry) {
        Path path = pageFilepath(rx, ry);
        File file = new File(path.toUri());
        if (file.isFile()) {
            try {
                InputStream inputStream = Files.newInputStream(path);
                return NativeImage.read(inputStream);
            } catch (IOException e) {
                WayfinderClient.LOGGER.error("Failed to load image {}\n{}", path, e);
            }
        }
        return null;
    }

    /**
     * Attempts to save an image. Should be run via {@link Util#ioPool()}
     * @param image null to delete
     */
    public void trySaveImage(int rx, int ry, @Nullable NativeImage image) {
        Path path = pageFilepath(rx, ry);
        try {
            if (image == null) {
                Files.deleteIfExists(path);
            } else {
                image.writeToFile(path);
            }
        } catch (IOException e) {
            WayfinderClient.LOGGER.error("Failed to save region {} {} to {}\n{}", rx, ry, path, e);
        }
    }
}
