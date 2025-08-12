package wawa.wayfinder.data;

import com.google.gson.*;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.mixin.BiomeManagerAccessor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Holds file path information and helper functions for reading and writing image data
 */
public class PageIO {
    private final Path pagePath;

    public PageIO(final Level level, final Minecraft client) {
        this.pagePath = this.buildMapPath(level, client);
        try {
            Files.createDirectories(this.pagePath);
        } catch (final IOException e) {
            WayfinderClient.LOGGER.error("Could not create map directory\n{}", e);
        }
    }

    /**
     * @return "minecraftinstance/wayfinder_maps/singleplayer/uuid_worldname"
     */
    private Path buildMapPath(final Level level, final Minecraft client) {
        Path path = client.gameDirectory.toPath()
                .resolve("wayfinder_maps");
        final long seed = ((BiomeManagerAccessor) level.getBiomeManager()).getBiomeZoomSeed();
        final UUID uuid = Mth.createInsecureUUID(RandomSource.create(seed));
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
        return this.pagePath;
    }

    public Path pageFilepath(final int rx, final int ry) {
        return this.pagePath.resolve(String.format("%d_%d.png", rx, ry));
    }

    /**
     * Attempts to load an image. Should be run via {@link Util#ioPool()}
     * @return null if file doesn't exist or there was an error loading
     */
    public @Nullable NativeImage tryLoadImage(final int rx, final int ry) {
        final Path path = this.pageFilepath(rx, ry);
        final File file = new File(path.toUri());
        if (file.isFile()) {
            try {
                final InputStream inputStream = Files.newInputStream(path);
                return NativeImage.read(inputStream);
            } catch (final IOException e) {
                WayfinderClient.LOGGER.error("Failed to load image {}\n{}", path, e);
            }
        }
        return null;
    }

    /**
     * Attempts to save an image. Should be run via {@link Util#ioPool()}
     * @param image null to delete
     */
    public void trySaveImage(final int rx, final int ry, @Nullable final NativeImage image) {
        final Path path = this.pageFilepath(rx, ry);
        try {
            if (image == null) {
                Files.deleteIfExists(path);
            } else {
                image.writeToFile(path);
            }
        } catch (final IOException e) {
            WayfinderClient.LOGGER.error("Failed to save region {} {} to {}\n{}", rx, ry, path, e);
        }
    }

    // todo: codec this please ....
    public void savePins(final Map<Pin.Type, Pin> pins) {
        final Gson gson = new Gson();
        final JsonObject pinsObject = new JsonObject();

        for (final Map.Entry<Pin.Type, Pin> entry : pins.entrySet()) {
            final JsonArray position = new JsonArray();
            position.add(entry.getValue().getPosition().x());
            position.add(entry.getValue().getPosition().y());

            final JsonObject pinObject = new JsonObject();
            pinObject.add("position", position);
            pinsObject.add(entry.getKey().id().toString(), pinObject);
        }

        try (final FileWriter writer = new FileWriter(this.pagePath.resolve("pins.json").toFile(), false)) {
            gson.toJson(pinsObject, writer);
        } catch (final IOException e) {
            WayfinderClient.LOGGER.error("Error saving pin data", e);
        }
    }

    public Map<Pin.Type, Pin> readPins() {
        final Gson gson = new Gson();
        final File file = this.pagePath.resolve("pins.json").toFile();
        if (file.exists() && file.isFile()) {
            try (final FileReader reader = new FileReader(file)) {
                final JsonObject pinsObject = JsonParser.parseReader(reader).getAsJsonObject();
                final Map<Pin.Type, Pin> pins = new HashMap<>();
                for (final Map.Entry<String, JsonElement> entry : pinsObject.entrySet()) {
                    final ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
                    final Pin.Type type = Pin.TYPES.get(id);
                    if (type == null) {
                        WayfinderClient.LOGGER.warn("Unknown pin type {}", id);
                    } else {
                        if (entry.getValue().isJsonObject()) {
                            final JsonElement position = entry.getValue().getAsJsonObject().get("position");
                            if (position.isJsonArray() && position.getAsJsonArray().size() >= 2) {
                                final Pin pin = new Pin(type);
                                pin.setPosition(
                                        new Vector2d(position.getAsJsonArray().get(0).getAsDouble(),
                                        position.getAsJsonArray().get(1).getAsDouble())
                                );
                                pins.put(type, pin);
                            }
                        }
                    }
                }
                return pins;
            } catch (final IOException e) {
                WayfinderClient.LOGGER.error("Error reading pin data", e);
            }
        }
        return new HashMap<>();
    }
}
