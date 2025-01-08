package beeisyou.mapdrawing;

import beeisyou.mapdrawing.mapmanager.MapRegion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import org.apache.commons.io.FilenameUtils;
import org.joml.Vector2i;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Stream;

public class MapRegions extends HashMap<Vector2i, MapRegion> {
    public void save() {
        MapDrawing.LOGGER.info("Saving map");
        this.forEach((v, r) -> r.save());
    }

    public static MapRegions fromFolder(Path path) {
        MapDrawing.LOGGER.info("Reading map data from {}", path);
        MapRegions regions = new MapRegions();
        try {
            MinecraftClient.getInstance().getLevelStorage().getSavesDirectory().resolve("pages");
            Stream<Path> stream = Files.list(path);
            stream.forEach(p -> {
                String filename = FilenameUtils.removeExtension(p.getFileName().toString());
                int i = filename.lastIndexOf("_");
                if (i != -1) {
                    String srx = filename.substring(0, i);
                    String srz = filename.substring(i + 1);
                    try {
                        int rx = Integer.parseInt(srx);
                        int rz = Integer.parseInt(srz);
                        InputStream inputStream = Files.newInputStream(p);
                        MapRegion region = new MapRegion(rx, rz);
                        region.texture.setImage(NativeImage.read(inputStream));
                        region.texture.upload();
                        regions.put(new Vector2i(rx, rz), region);
                    } catch (NumberFormatException e) {
                        MapDrawing.LOGGER.warn("Invalid filename {}", p);
                    } catch (IOException e) {
                        MapDrawing.LOGGER.warn("Failed to load map from {}\n{}", path, e);
                    }
                } else {
                    MapDrawing.LOGGER.warn("Invalid filename {}", p);
                }
            });
        } catch (IOException e) {
            MapDrawing.LOGGER.warn("Failed to load map from {}\n{}", path, e);
        }
        // this is a very normal piece of code that will not kill your computer and/or hard drive
//        for (int i = 0; i < 1000; i++) {
//            for (int j = 0; j < 1000; j++) {
//                MapRegion region = new MapRegion(i, j);
//                region.putPixelRelative(0, 0, ColorHelper.getArgb(255, 255, 255), false);
//                regions.put(new Vector2i(i, j), region);
//            }
//        }
        return regions;
    }
}
