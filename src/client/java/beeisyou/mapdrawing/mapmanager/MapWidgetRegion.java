package beeisyou.mapdrawing.mapmanager;

import beeisyou.mapdrawing.MapDrawing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * A 512x512 section of the map
 */
public class MapWidgetRegion {
    public final int rx;
    public final int rz;
    public final NativeImageBackedTexture texture;
    public final Identifier id;
    private boolean dirty = false; // unuploaded changes
    private boolean removed = false;
    public MapWidgetRegion(int rx, int rz) {
        this.rx = rx;
        this.rz = rz;
        texture = new NativeImageBackedTexture(512, 512, false); // each chunk region is 512x512 blocks, seems fitting
        id = Identifier.of("mapmanager", String.format("map_%d_%d", rx, rz));
        texture.getImage().apply(i -> 0); // reset to clear
        texture.upload();
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
    }

    public void save() {
        if (MinecraftClient.getInstance().isInSingleplayer()) {
            save(MinecraftClient.getInstance().getLevelStorage().getSavesDirectory().resolve("pages"));
        }
    }

    public void save(Path path) {
        Path file = path.resolve(String.format("%d_%d.png", rx, rz));
        MapDrawing.LOGGER.info("Saving map region to {}", file);
        Util.getIoWorkerExecutor().execute(() -> {
            try {
                Files.createDirectories(file);
                // saves/world/pages/[rx]_[rz].png
                texture.getImage().writeTo(file);
            } catch (IOException e) {
                MapDrawing.LOGGER.warn("Failed to save map {} {} to {}\n{}", rx, rz, file, e);
            }
        });
    }


    public boolean inBoundsRel(int x, int z) {
        return x >= 0 && x < 512 && z >= 0 && z < 512;
    }

    public boolean inBoundsAbs(int x, int z) {
        return inBoundsRel(x - rx * 512, z - rz * 512);
    }

    public boolean putPixelWorld(int x, int z, int color, boolean highlight) {
        return putPixelRelative(x - rx * 512, z - rz * 512, color, highlight);
    }

    public boolean putPixelRelative(int x, int z, int color, boolean highlight) {
        if (!inBoundsRel(x, z))
            return false;
        if (!highlight || texture.getImage().getColorArgb(x - rx * 512, z - rz * 512) == 0) {
            texture.getImage().setColorArgb(x, z, color);
            dirty = true;
            return true;
        }
        return false;
    }

    public void checkDirty() {
        if (dirty) {
            if (Arrays.stream(texture.getImage().copyPixelsAbgr()).allMatch(i -> i == 0)) {
                removed = true;
                clear();
            } else {
                texture.upload();
                dirty = false;
            }
        }
    }

    public boolean isRemoved() {
        return removed;
    }

    public void clear() {
        texture.close();
    }
}
