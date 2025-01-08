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

public class MapRegion {
    public final int rx;
    public final int rz;
    public final NativeImageBackedTexture texture;
    public final Identifier id;
    private boolean dirty = false; // unuploaded changes
    private boolean removed = false;
    public MapRegion(int rx, int rz) {
        this.rx = rx;
        this.rz = rz;
        texture = new NativeImageBackedTexture(512, 512, false); // each chunk region is 512x512 blocks, seems fitting
        id = Identifier.of("mapmanager", String.format("map_%d_%d", rx, rz));
        texture.getImage().apply(i -> 0); // reset to clear
//        int l = ((rx * 21673 + rz * 2938437) % 32 + 32) % 32;
//        texture.getImage().apply(i -> ColorHelper.getArgb(l, l, l)); // reset to clear
//        texture.getImage().fillRect(10, 0, 3, 512, ColorHelper.getArgb(0, 255, 0));
//        texture.getImage().fillRect(0, 10, 512, 3, ColorHelper.getArgb(0, 255, 0));
//        texture.getImage().fillRect(501, 0, 3, 512, ColorHelper.getArgb(0, 255, 0));
//        texture.getImage().fillRect(0, 501, 512, 3, ColorHelper.getArgb(0, 255, 0));
        texture.upload();
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
    }

    public void save() {
        MapDrawing.LOGGER.info("Saving map region");
        if (MinecraftClient.getInstance().isInSingleplayer()) {
            save(MinecraftClient.getInstance().getLevelStorage().getSavesDirectory().resolve("pages"));
        }
    }

    public void save(Path path) {
        MapDrawing.LOGGER.info("Saving map region to {}", path);
        Util.getIoWorkerExecutor().execute(() -> {
            try {
                Files.createDirectories(path);
                // saves/world/pages/[rx]_[rz].png
                texture.getImage().writeTo(path.resolve(String.format("%d_%d.png", rx, rz)));
            } catch (IOException e) {
                MapDrawing.LOGGER.warn("Failed to save map {} {} to {}\n{}", rx, rz, path, e);
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
