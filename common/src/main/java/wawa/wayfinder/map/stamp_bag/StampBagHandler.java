package wawa.wayfinder.map.stamp_bag;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.GsonHelper;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageIO;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class StampBagHandler {

    public static final Codec<MetaDataRecord> META_DATA_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.list(Codec.STRING).fieldOf("favorites").forGetter(MetaDataRecord::favorites),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("translatables").forGetter(MetaDataRecord::translatedNames)
    ).apply(i, (favorites, translatable) -> new MetaDataRecord(new ArrayList<>(favorites), new HashMap<>(translatable))));

    /**
     * Path used to save stamps too
     */
    public Path stampPath;

    /**
     * Path used to access metadata.json
     */
    public Path metaDataPath;

    /**
     * The current state of this bag handler. All other functions stop when not set to {@link StampBagHandler.SavingState#NORMAL}
     */
    private SavingState state = SavingState.NORMAL;

    /**
     * The RAM version of the metadata.json.
     * @see MetaDataRecord
     */
    private volatile MetaDataRecord metadataObject;
    private Future<MetaDataRecord> loadingRecordThread;
    private Future<?> savingRecordThread;

    /**
     * Whether {@link StampBagHandler#metadataObject} should be saved
     */
    private volatile boolean dirty = false;

    /**
     * Whether metadata.json should be loaded from disk. <p/>
     * Saving is <b>ALWAYS</b> prioritized.
     */
    private volatile boolean loadRequested = false;

    public StampBagHandler() {
        createStampDirectory();
    }

    private void createStampDirectory() {
        Path mainPath = Minecraft.getInstance().gameDirectory.toPath().resolve(PageIO.mapName);
        this.stampPath = mainPath.resolve("stamps");
        this.metaDataPath = stampPath.resolve("metadata.json");

        if (Files.notExists(stampPath)) {
            try {
                Files.createDirectory(stampPath);
            } catch (IOException e) {
                WayfinderClient.LOGGER.error("Could not create stamp directory\n{}", String.valueOf(e));
            }
        }

        if (Files.notExists(metaDataPath)) {
            metadataObject = new MetaDataRecord(new ArrayList<>(), new HashMap<>());
            dirty = true;
        } else {
            loadRequested = true;
        }
    }

    public void tick() {
        if (state == SavingState.NORMAL) {
            if (dirty) {
                state = SavingState.SAVING;
            } else if (loadRequested) {
                state = SavingState.LOADING;
            }
        }

        switch (state) {
            case SAVING -> {
                if (dirty && savingRecordThread == null) {
                    savingRecordThread = saveStableMetaData();
                }

                if (savingRecordThread != null && savingRecordThread.isDone()) {
                    state = SavingState.NORMAL;
                    dirty = false;

                    savingRecordThread = null;
                }
            }

            case LOADING -> {
                if (loadRequested && loadingRecordThread == null) {
                    loadingRecordThread = loadStableMetaData();
                }

                if (loadingRecordThread != null && loadingRecordThread.isDone()) {
                    state = SavingState.NORMAL;
                    loadRequested = false;
                    try {
                        metadataObject = loadingRecordThread.get();
                    } catch (InterruptedException | ExecutionException e) { //should never be called....
                        WayfinderClient.LOGGER.warn("Unable to successfully load MetaData! {}", e.toString());
                    }

                    loadingRecordThread = null;
                }
            }
        }
    }

    public void addNewStamp(NativeImage newStamp, String desiredName) {
        String adjustedName = slug(desiredName);
        adjustedName = findFirstValidFilename(adjustedName, this.stampPath, "png");
        WayfinderClient.LOGGER.debug("Saving new stamp image: {}; adjusted to:{}", desiredName, adjustedName);
        Path imagePath = this.stampPath.resolve(adjustedName);

        metadataObject.translatedNames.put(adjustedName, desiredName);
        dirty = true;

        Util.ioPool().execute(() -> {
            try {
                newStamp.writeToFile(imagePath);
            } catch (IOException e) {
                WayfinderClient.LOGGER.error("Could not save stamp image\n{}", String.valueOf(e));
            }
        });
    }

    // thank you john create for these file management classes
    public static String findFirstValidFilename(String name, Path folderPath, String extension) {
        int index = 0;
        String filename;
        Path filepath;
        do {
            filename = slug(name) + ((index == 0) ? "" : "_" + index) + "." + extension;
            index++;
            filepath = folderPath.resolve(filename);
        } while (Files.exists(filepath));
        return filename;
    }

    // thank you john create for these file management classes
    public static String slug(String name) {
        return name.replaceAll("\\W+", "_");
    }

    private Future<MetaDataRecord> loadStableMetaData() {
        if (state == SavingState.LOADING) {
            return Util.ioPool().submit(() -> {
                MetaDataRecord loadedRecord = null;

                try {
                    DataResult<MetaDataRecord> md = META_DATA_CODEC.parse(JsonOps.INSTANCE,
                            Streams.parse(WayfinderClient.WAYFINDER_GSON.newJsonReader(Files.newBufferedReader(metaDataPath))));

                    if (md.isError()) {
                        WayfinderClient.LOGGER.warn("Malformed metadata file! {}", md.error());
                        return null;
                    }

                    loadedRecord = md.getOrThrow();
                } catch (IOException e) {
                    WayfinderClient.LOGGER.warn("Unable to read metadata file: {}", e.toString());
                }

                return loadedRecord;
            });
        }

        return null;
    }

    private Future<?> saveStableMetaData() {
        if (state == SavingState.SAVING) {
            return Util.ioPool().submit(() -> {
                try {
                    JsonElement ele = META_DATA_CODEC.encodeStart(JsonOps.INSTANCE, metadataObject).getOrThrow();
                    JsonWriter jwriter = WayfinderClient.WAYFINDER_GSON.newJsonWriter(Files.newBufferedWriter(metaDataPath));
                    jwriter.setSerializeNulls(false);
                    jwriter.setIndent(" ".repeat(Math.max(0, 2)));
                    GsonHelper.writeValue(jwriter, ele, null);
                    jwriter.close();
                } catch (IOException e) {
                    WayfinderClient.LOGGER.warn("Unable to write metadata file: {}", e.toString());
                    metadataObject = null;
                }
            });
        }

        return null;
    }

    private enum SavingState {
        NORMAL, SAVING, LOADING
    }

    /**
     * A mutable version of metadata.json. <p>
     * Saved to disk when {@link StampBagHandler#dirty} is set to true; Read from disk when {@link StampBagHandler#loadRequested} is set to true. <p>
     * Saving is <b>ALWAYS</b> prioritized.
     *
     * @param favorites
     * @param translatedNames
     */
    public record MetaDataRecord(List<String/*untranslated*/> favorites,
                                 Map<String/*untranslated*/, String/*translated*/> translatedNames) {

    }
}
