package wawa.wayfinder.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.mixin.BiomeManagerAccessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class PageIO {
    private final Path mapPath;
    public PageIO(ClientLevel level, Minecraft client) {
        mapPath = buildMapPath(level, client);
        try {
            Files.createDirectories(mapPath);
        } catch (IOException e) {
            WayfinderClient.LOGGER.error("Could not create map directory\n{}", e);
        }
    }

    /**
     * @return "minecraftinstance/wayfinder_maps/singleplayer/uuid_worldname"
     */
    private Path buildMapPath(ClientLevel level, Minecraft client) {
        Path path = client.gameDirectory.toPath()
                .resolve("wayfinder_maps");
        long seed = ((BiomeManagerAccessor)level.getBiomeManager()).getBiomeZoomSeed();
        UUID uuid = Mth.createInsecureUUID(RandomSource.create(seed));
        if (client.isLocalServer()) {
            path = path.resolve("singleplayer")
                    .resolve(uuid + "_" + client.getSingleplayerServer().getWorldData().getLevelName());
        } else {
            path = path.resolve("multiplayer")
                    .resolve(uuid + "_" + client.getCurrentServer().name);
        }
        return path;
    }

    public Path pageFilepath(int rx, int ry) {
        return mapPath.resolve(String.format("%d_%d.png", rx, ry));
    }
}
