package wawa.mapwright.map.stamp_bag;

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
import wawa.mapwright.MapwrightClient;
import wawa.mapwright.data.PageIO;
import wawa.mapwright.platform.MapWrightServices;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class StampBagHandler {
	public static final Codec<MetaDataRecord> META_DATA_CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.list(StampInformation.CODEC).fieldOf("translatables").forGetter(MetaDataRecord::allStamps)
	).apply(i, (translatable) -> new MetaDataRecord(new ArrayList<>(translatable), new ArrayList<>())));

	/**
	 * Path used to save stamps too
	 */
	private Path stampPath;

	/**
	 * Path used to access metadata.json
	 */
	private Path metaDataPath;

	/**
	 * The current state of this bag handler. All other functions stop when not set to {@link SavingState#NORMAL}
	 */
	private SavingState state = SavingState.NORMAL;

	/**
	 * The RAM version of the metadata.json.
	 *
	 * @see MetaDataRecord
	 */
	private MetaDataRecord metadataObject;

	/**
	 * Stamp information handled separately from the metaDataOObject, used for saving images from the copy tool for use in the stamp tool
	 */
	//TODO: move somewhere better
	public StampInformation temporaryStampInformation = new StampInformation("placeholder.png", "placeholder", false);

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
		if(!MapWrightServices.PLATFORM.isRunningDatagen()) {
            this.createStampDirectory();
		}
	}

	private void createStampDirectory() {
		final Path mainPath = Minecraft.getInstance().gameDirectory.toPath().resolve(PageIO.mapName);
		this.stampPath = mainPath.resolve("stamps");
		this.metaDataPath = this.stampPath.resolve("metadata.json");

		if (Files.notExists(this.stampPath)) {
            this.stampPath.toFile().mkdirs();
        }

		if (Files.notExists(this.metaDataPath)) {
            this.metadataObject = new MetaDataRecord(new ArrayList<>(), new ArrayList<>());
            this.dirty = true;
		} else {
            this.metaDataLoadingRequested = true;
		}
	}

	public void tick() {
        this.switchStates();
        this.state.stateManager.accept(this);

		if (this.metadataObject != null) {
            this.metadataObject.allStamps.forEach(si -> si.getTextureManager().tick());
		}
	}

	private void switchStates() {
		if (this.state == SavingState.NORMAL) {
			if (!this.stampThreads.isEmpty()) {
                this.state = SavingState.LOADING_IMAGES;
				return;
			}

			if (this.dirty) {
                this.state = SavingState.SAVING;
				return;
			}

			if (this.metaDataLoadingRequested) {
                this.state = SavingState.LOADING;
			}
		}
	}

	public void addNewStamp(final NativeImage newStamp, final String desiredName) {
		String adjustedName = slug(desiredName);
		adjustedName = findFirstValidFilename(adjustedName, this.stampPath, "png");
		MapwrightClient.LOGGER.debug("Saving new stamp image: {}; adjusted to:{}", desiredName, adjustedName);
		final Path imagePath = this.stampPath.resolve(adjustedName);

        this.metadataObject.allStamps.add(new StampInformation(adjustedName, desiredName, false, newStamp));
        this.dirty = true;

		Util.ioPool().execute(() -> {
			try {
				newStamp.writeToFile(imagePath);
			} catch (final IOException e) {
				MapwrightClient.LOGGER.error("Could not save stamp image\n{}", String.valueOf(e));
			}
		});
	}

	// thank you john create for these file management classes
	private static String findFirstValidFilename(final String name, final Path folderPath, final String extension) {
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
	private static String slug(final String name) {
		return name.replaceAll("\\W+", "_");
	}

	private Future<MetaDataRecord> loadStableMetaData() {
		if (this.state == SavingState.LOADING) {
			return Util.ioPool().submit(() -> {
				MapwrightClient.LOGGER.info("attempting to load stamps");
				MetaDataRecord loadedRecord = null;

				try {
					final DataResult<MetaDataRecord> md = META_DATA_CODEC.parse(JsonOps.INSTANCE,
							Streams.parse(MapwrightClient.MAPWRIGHT_GSON.newJsonReader(Files.newBufferedReader(this.metaDataPath))));

					if (md.isError()) {
						MapwrightClient.LOGGER.error("Malformed metadata file! {}", md.error());
						return null;
					}

					loadedRecord = md.getOrThrow();
				} catch (final IOException e) {
					MapwrightClient.LOGGER.error("Unable to read metadata file: {}", e.toString());
				}

				return loadedRecord;
			});
		}

		return null;
	}

	private Future<?> saveStableMetaData(final MetaDataRecord record) {
		if (this.state == SavingState.SAVING) {
			return Util.ioPool().submit(() -> {
				MapwrightClient.LOGGER.info("attempting to save stamps");

				try {
					final JsonElement ele = META_DATA_CODEC.encodeStart(JsonOps.INSTANCE, record).getOrThrow();
					final JsonWriter jwriter = MapwrightClient.MAPWRIGHT_GSON.newJsonWriter(Files.newBufferedWriter(this.metaDataPath));
					jwriter.setSerializeNulls(false);
					jwriter.setIndent(" ".repeat(Math.max(0, 2)));
					GsonHelper.writeValue(jwriter, ele, null);
					jwriter.close();
				} catch (final IOException e) {
					MapwrightClient.LOGGER.error("Unable to write metadata file: {}", e.toString());
				}
			});
		}

		return null;
	}

	public void requestAllStamps(final Collection<StampInformation> collection, final boolean favoritesOnly) {
		collection.addAll(this.getCorrectStampCollection(favoritesOnly));

        this.filterAndLoadStampsFromDisk(collection);
	}

	public void requestStampContaining(final Collection<StampInformation> collection, final String searchParam, final boolean favoritesOnly) {
		for (final StampInformation si : this.getCorrectStampCollection(favoritesOnly)) {
			if (si.getCustomName().toLowerCase().contains(searchParam.toLowerCase())) {
				collection.add(si);
			}
		}

        this.filterAndLoadStampsFromDisk(collection);
	}

	/**
	 * Bulk requests stamps from an array of indices into {@link StampBagHandler#metadataObject} <p>
	 * If the requested stamp does not have an image associated with it, one will attempt to be loaded from disk and associated with the appropriate {@link StampInformation}
	 *
	 * @param collection The collection to populate with {@link StampInformation}
	 * @param indices    An array of indices to grab
	 */
	public void bulkRequestStamps(final Collection<StampInformation> collection, final boolean favoritesOnly, final int... indices) {
		final List<StampInformation> stampCollection = this.getCorrectStampCollection(favoritesOnly);
		for (final StampInformation si : stampCollection) {
			for (final int i : indices) {
				if (stampCollection.indexOf(si) == i) {
					collection.add(si);
				}
			}
		}

        this.filterAndLoadStampsFromDisk(collection);
	}

	private List<StampInformation> getCorrectStampCollection(final boolean favoritesOnly) {
		final List<StampInformation> stampCollection;

		if (favoritesOnly) {
            this.metadataObject.bulkUpdateFavorites();
			stampCollection = this.metadataObject.favorites();
		} else {
			stampCollection = this.metadataObject.allStamps();
		}

		return stampCollection;
	}

	private void filterAndLoadStampsFromDisk(final Collection<StampInformation> collection) {
		for (final StampInformation si : collection) {
			if (si.getTextureManager().getTexture() == null) {
                this.loadStampImage(si);
			}
		}
	}

	private void loadStampImage(final StampInformation si) {
        this.stampThreads.add(Util.ioPool().submit(() -> {
			final Path stampPath = this.stampPath.resolve(si.getFileName());
			try {
				MapwrightClient.LOGGER.info("attempting to load stamp image for: {}", si.getFileName());
				final InputStream inputStream = Files.newInputStream(stampPath);
				final NativeImage image = NativeImage.read(inputStream);
				si.setStampTexture(image);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}));
	}

	public int getTotalEntryCount() {
		return this.getTotalEntryCount(false);
	}

	public int getTotalEntryCount(final boolean favoritesOnly) {
		if (favoritesOnly) {
			int favoriteCount = 0;

			for (final StampInformation allStamp : this.metadataObject.allStamps()) {
				if (allStamp.isFavorited()) {
					favoriteCount++;
				}
			}

			return favoriteCount;
		}


		return this.metadataObject.allStamps().size();
	}

	public void setDirty() {
        this.dirty = true;
	}

	public void removeStamp(final StampInformation si) {
		si.setRemoved();
        this.metadataObject.allStamps().remove(si);
        this.setDirty();

		//TODO: move removed stamps to deleted folder
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
					MapwrightClient.LOGGER.error("Unable to successfully load MetaData! {}", e.toString());
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

		SavingState(@Nullable final Consumer<StampBagHandler> handler) {
			this.stateManager = Objects.requireNonNullElseGet(handler, () -> (sbh) -> {
			});
		}
	}

	/**
	 * A mutable version of metadata.json. <p>
	 * Saved to disk when {@link StampBagHandler#dirty} is set to true; Read from disk when {@link StampBagHandler#metaDataLoadingRequested} is set to true. <p>
	 * Saving is <b>ALWAYS</b> prioritized.
	 */
	public record MetaDataRecord(List<StampInformation> allStamps, List<StampInformation> favorites) {
		public void bulkUpdateFavorites() {
            this.favorites.clear();
			for (final StampInformation si : this.allStamps) {
				if (si.isFavorited()) {
                    this.favorites.add(si);
				}
			}
		}
	}
}
