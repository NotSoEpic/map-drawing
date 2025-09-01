package wawa.wayfinder.map.stamp_bag;

import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StampBagHandler {

    public PageManager parentManager;
    public Path stampPath;
    public Path metaDataPath;

    List<String> favorites = new ArrayList<>();

    public StampBagHandler(PageManager parentManager) {
        this.parentManager = parentManager;
        createStampDirectory();
    }

    private void createStampDirectory() {
        Path path = parentManager.pageIO.getPagePath();
        this.stampPath = path.resolve("stamps");
        this.metaDataPath = stampPath.resolve("metadata.json");

        if (Files.notExists(stampPath)) {
            try {
                Files.createDirectory(stampPath);
            } catch (IOException e) {
                WayfinderClient.LOGGER.error("Could not create stamp directory\n{}", String.valueOf(e));
            }
        }

        if (Files.notExists(metaDataPath)) {
            try {
                BufferedWriter writer = Files.newBufferedWriter(metaDataPath);
                JsonWriter jsonWriter = WayfinderClient.WAYFINDER_GSON.newJsonWriter(writer);

                jsonWriter.beginObject();
                jsonWriter.endObject();
                jsonWriter.close();
            } catch (IOException e) {
                WayfinderClient.LOGGER.error("Could not create metadata json\n{}", String.valueOf(e));
            }
        }
    }

    public void addNewStamp(NativeImage newStamp, String desiredName) {
        Path imagePath = this.stampPath.resolve(desiredName + ".png");
        if (Files.exists(imagePath)) {
            WayfinderClient.LOGGER.warn("Attempting to overwite already present stamp image, skipping!");
            return;
        }

        Util.ioPool().execute(() -> {
            try {
                newStamp.writeToFile(imagePath);
            } catch (IOException e) {
                WayfinderClient.LOGGER.error("Could not save stamp image\n{}", String.valueOf(e));
            }
        });
    }

    public void removeStamp(String name) {




    }

    public void addNewFavorite(String name) {
        favorites.add(name);
    }

    public void removeFavorite(String name) {
        favorites.remove(name);
    }

    public void saveFavorites() {


    }

    public void getStampsForRange(int from) {
        getStampsForRange(from, from + 6);
    }

    public void getStampsForRange(int from, int to) {


    }


    public record StampImage() {

    }
}
