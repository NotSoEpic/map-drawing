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
import org.jetbrains.annotations.Nullable;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.data.PageIO;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class StampBagHandler {

    public static final Codec<MetaDataRecord> META_DATA_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.list(StampInformation.CODEC).fieldOf("translatables").forGetter(MetaDataRecord::allStamps)
    ).apply(i, (translatable) -> new MetaDataRecord(new ArrayList<>(translatable))));

    /**
     * Path used to save stamps too
     */
    private Path stampPath;

    /**
     * Path used to access metadata.json
     */
    private Path metaDataPath;

    /**
     * The current state of this bag handler. All other functions stop when not set to {@link StampBagHandler.SavingState#NORMAL}
     */
    private SavingState state = SavingState.NORMAL;

    /**
     * The RAM version of the metadata.json.
     *
     * @see MetaDataRecord
     */
    private MetaDataRecord metadataObject;
    private Future<MetaDataRecord> loadingRecordThread;
    private Future<?> savingRecordThread;

    private final List<Future<?>> stampThreads = new ArrayList<>();

    /**
     * Whether {@link StampBagHandler#metadataObject} should be saved
     */
    private boolean dirty = false;

    /**
     * Whether metadata.json should be loaded from disk. <p/>
     * Saving is <b>ALWAYS</b> prioritized.
     */
    private boolean metaDataLoadingRequested = false;

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
            metadataObject = new MetaDataRecord(new ArrayList<>());
            dirty = true;
        } else {
            metaDataLoadingRequested = true;
        }
    }

    public void tick() {
        switchStates();
        state.stateManager.accept(this);

        if (metadataObject != null) {
            metadataObject.allStamps.forEach(si -> si.getStampTexture().tick());
        }
    }

    private void switchStates() {
        if (state == SavingState.NORMAL) {
            if (!stampThreads.isEmpty()) {
                state = SavingState.LOADING_IMAGES;
                return;
            }

            if (dirty) {
                state = SavingState.SAVING;
                return;
            }

            if (metaDataLoadingRequested) {
                state = SavingState.LOADING;
            }
        }
    }

    public void addNewStamp(NativeImage newStamp, String desiredName) {
        String adjustedName = slug(desiredName);
        adjustedName = findFirstValidFilename(adjustedName, this.stampPath, "png");
        WayfinderClient.LOGGER.debug("Saving new stamp image: {}; adjusted to:{}", desiredName, adjustedName);
        Path imagePath = this.stampPath.resolve(adjustedName);

        metadataObject.allStamps.add(new StampInformation(adjustedName, desiredName, false));
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
    private static String findFirstValidFilename(String name, Path folderPath, String extension) {
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
    private static String slug(String name) {
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
                        WayfinderClient.LOGGER.error("Malformed metadata file! {}", md.error());
                        return null;
                    }

                    loadedRecord = md.getOrThrow();
                } catch (IOException e) {
                    WayfinderClient.LOGGER.error("Unable to read metadata file: {}", e.toString());
                }

                return loadedRecord;
            });
        }

        return null;
    }

    private Future<?> saveStableMetaData(MetaDataRecord record) {
        if (state == SavingState.SAVING) {
            return Util.ioPool().submit(() -> {
                try {
                    JsonElement ele = META_DATA_CODEC.encodeStart(JsonOps.INSTANCE, record).getOrThrow();
                    JsonWriter jwriter = WayfinderClient.WAYFINDER_GSON.newJsonWriter(Files.newBufferedWriter(metaDataPath));
                    jwriter.setSerializeNulls(false);
                    jwriter.setIndent(" ".repeat(Math.max(0, 2)));
                    GsonHelper.writeValue(jwriter, ele, null);
                    jwriter.close();
                } catch (IOException e) {
                    WayfinderClient.LOGGER.error("Unable to write metadata file: {}", e.toString());
                }
            });
        }

        return null;
    }

    public StampInformation requestSingleStamp(int i) {
        if (i <= metadataObject.allStamps().size() - 1) {
            StampInformation si = metadataObject.allStamps().get(i);
            if (si.getStampTexture() == null) {
                loadStampImage(si);
            }

            return si;
        }

        return null;
    }

    public void requestAllStamps(Collection<StampInformation> collection) {
        collection.addAll(metadataObject.allStamps());
        filterAndLoadStampsFromDisk(collection);
    }

    /**
     * Bulk requests stamps from an array of indices into {@link StampBagHandler#metadataObject} <p>
     * If the requested stamp does not have an image associated with it, one will attempt to be loaded from disk and associated with the appropriate {@link StampInformation}
     *
     * @param collection The collection to populate with {@link StampInformation}
     * @param indices    An array of indices to grab
     */
    public void bulkRequestStamps(Collection<StampInformation> collection, int... indices) {
        for (int i : indices) {
            int size = metadataObject.allStamps().size();

            if (i <= size - 1) {
                collection.add(metadataObject.allStamps().get(i));
            }
        }

        filterAndLoadStampsFromDisk(collection);
    }

    private void filterAndLoadStampsFromDisk(Collection<StampInformation> collection) {
        for (StampInformation si : collection) {
            if (si.getStampTexture().getTexture() == null) {
                loadStampImage(si);
            }
        }
    }

    private void loadStampImage(StampInformation si) {
        stampThreads.add(Util.ioPool().submit(() -> {
            Path stampPath = this.stampPath.resolve(si.getFileName());
            try {
                InputStream inputStream = Files.newInputStream(stampPath);
                NativeImage image = NativeImage.read(inputStream);
                si.setStampTexture(image);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private enum SavingState {
        NORMAL(null),
        SAVING((sbh) -> {
            if (!sbh.dirty) {
                sbh.state = SavingState.NORMAL;
                return;
            }

            if (sbh.savingRecordThread == null) {
                sbh.savingRecordThread = sbh.saveStableMetaData(sbh.metadataObject);
            }

            if (sbh.savingRecordThread != null && sbh.savingRecordThread.isDone()) {
                sbh.state = SavingState.NORMAL;
                sbh.dirty = false;

                sbh.savingRecordThread = null;
            }
        }),
        LOADING((sbh) -> {
            if (!sbh.metaDataLoadingRequested) {
                sbh.state = SavingState.NORMAL;
                return;
            }

            if (sbh.loadingRecordThread == null) {
                sbh.loadingRecordThread = sbh.loadStableMetaData();
            }

            if (sbh.loadingRecordThread != null && sbh.loadingRecordThread.isDone()) {
                sbh.state = SavingState.NORMAL;
                sbh.metaDataLoadingRequested = false;
                try {
                    sbh.metadataObject = sbh.loadingRecordThread.get();
                } catch (InterruptedException | ExecutionException e) { //should never be called....
                    WayfinderClient.LOGGER.error("Unable to successfully load MetaData! {}", e.toString());
                }

                sbh.loadingRecordThread = null;
            }
        }),
        LOADING_IMAGES((sbh) -> {
            sbh.stampThreads.removeIf(Future::isDone);
            if (sbh.stampThreads.isEmpty()) {
                sbh.state = SavingState.NORMAL;
            }
        });

        private final Consumer<StampBagHandler> stateManager;

        SavingState(@Nullable Consumer<StampBagHandler> handler) {
            this.stateManager = Objects.requireNonNullElseGet(handler, () -> (sbh) -> {
            });
        }
    }

    /**
     * A mutable version of metadata.json. <p>
     * Saved to disk when {@link StampBagHandler#dirty} is set to true; Read from disk when {@link StampBagHandler#metaDataLoadingRequested} is set to true. <p>
     * Saving is <b>ALWAYS</b> prioritized.
     */
    public record MetaDataRecord(List<StampInformation> allStamps) {

    }
}
