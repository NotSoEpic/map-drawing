package wawa.wayfinder.data;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import wawa.wayfinder.WayfinderClient;
import wawa.wayfinder.mixin.BiomeManagerAccessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

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
}
