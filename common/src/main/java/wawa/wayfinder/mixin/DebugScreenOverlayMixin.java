package wawa.wayfinder.mixin;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wawa.wayfinder.platform.Services;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin {

    @Shadow
    protected abstract Level getLevel();

    @Shadow
    @Nullable
    private ChunkPos lastPos;

    @Shadow
    public abstract void clearChunkCache();

    @Shadow
    protected abstract LevelChunk getClientChunk();

    @Shadow
    @Nullable
    protected abstract LevelChunk getServerChunk();

    @Shadow
    @Nullable
    protected abstract ServerLevel getServerLevel();

    @Shadow
    private static String printBiome(Holder<Biome> biomeHolder) {
        return biomeHolder.unwrap().map(key -> key.location().toString(), biome -> "[unregistered " + biome + "]");
    }

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Nullable
    protected abstract String getServerChunkStats();

    @Shadow
    @Final
    private static Map<Heightmap.Types, String> HEIGHTMAP_NAMES;

    @Shadow
    protected abstract String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> entry);

    @Shadow
    private HitResult block;

    @Shadow
    private HitResult liquid;

    @Inject(method = "getGameInformation", at = @At(value = "RETURN"), cancellable = true)
    private void getGameInformation(CallbackInfoReturnable<List<String>> cir, @Local(ordinal = 1) String s, @Local BlockPos blockpos) {
        if (!minecraft.showOnlyReducedInfo() && Services.CONFIG.config().hideCoordinates()) {
            Entity cameraEntity = minecraft.getCameraEntity();
            Direction facingDirection = cameraEntity.getDirection();
            String facingDescription;

            switch (facingDirection) {
                case NORTH -> facingDescription = "Towards negative Z";
                case SOUTH -> facingDescription = "Towards positive Z";
                case WEST -> facingDescription = "Towards negative X";
                case EAST -> facingDescription = "Towards positive X";
                default -> facingDescription = "Invalid";
            }

            ChunkPos currentChunkPos = new ChunkPos(blockpos);
            if (!Objects.equals(lastPos, currentChunkPos)) {
                lastPos = currentChunkPos;
                clearChunkCache();
            }

            Level level = getLevel();
            LongSet forcedChunks = (level instanceof ServerLevel serverLevel)
                    ? serverLevel.getForcedChunks()
                    : LongSets.EMPTY_SET;

            String minecraftVersion = SharedConstants.getCurrentVersion().getName();
            String launchVersion = minecraft.getLaunchedVersion();
            String clientModName = ClientBrandRetriever.getClientModName();
            String versionType = minecraft.getVersionType();
            String versionString = "Minecraft " + minecraftVersion + " (" + launchVersion + "/" + clientModName
                    + ("release".equalsIgnoreCase(versionType) ? "" : "/" + versionType) + ")";

            List<String> debugInfo = Lists.newArrayList(
                    versionString,
                    minecraft.fpsString,
                    s,
                    minecraft.levelRenderer.getSectionStatistics(),
                    minecraft.levelRenderer.getEntityStatistics(),
                    "P: " + minecraft.particleEngine.countParticles() + ". T: " + minecraft.level.getEntityCount(),
                    minecraft.level.gatherChunkSourceStats()
            );

            String serverChunkStats = getServerChunkStats();
            if (serverChunkStats != null) {
                debugInfo.add(serverChunkStats);
            }

            debugInfo.add(level.dimension().location() + " FC: " + forcedChunks.size());
            debugInfo.add("");

            debugInfo.add(String.format(Locale.ROOT,
                    "Facing: %s (%s) (%.1f / %.1f)",
                    facingDirection, facingDescription,
                    Mth.wrapDegrees(cameraEntity.getYRot()),
                    Mth.wrapDegrees(cameraEntity.getXRot())
            ));

            LevelChunk clientChunk = getClientChunk();
            if (clientChunk.isEmpty()) {
                debugInfo.add("Waiting for chunk...");
            } else {
                int rawBrightness = level.getChunkSource().getLightEngine().getRawBrightness(blockpos, 0);
                int skyLight = level.getBrightness(LightLayer.SKY, blockpos);
                int blockLight = level.getBrightness(LightLayer.BLOCK, blockpos);

                debugInfo.add("Client Light: " + rawBrightness + " (" + skyLight + " sky, " + blockLight + " block)");

                LevelChunk serverChunk = getServerChunk();

                StringBuilder clientHeightInfo = new StringBuilder("CH");
                for (Heightmap.Types type : Heightmap.Types.values()) {
                    if (type.sendToClient()) {
                        clientHeightInfo.append(" ")
                                .append(HEIGHTMAP_NAMES.get(type))
                                .append(": ")
                                .append(clientChunk.getHeight(type, blockpos.getX(), blockpos.getZ()));
                    }
                }
                debugInfo.add(clientHeightInfo.toString());

                StringBuilder serverHeightInfo = new StringBuilder("SH");
                for (Heightmap.Types type : Heightmap.Types.values()) {
                    if (type.keepAfterWorldgen()) {
                        serverHeightInfo.append(" ")
                                .append(HEIGHTMAP_NAMES.get(type))
                                .append(": ");
                        if (serverChunk != null) {
                            serverHeightInfo.append(serverChunk.getHeight(type, blockpos.getX(), blockpos.getZ()));
                        } else {
                            serverHeightInfo.append("??");
                        }
                    }
                }
                debugInfo.add(serverHeightInfo.toString());

                if (blockpos.getY() >= level.getMinBuildHeight() && blockpos.getY() < level.getMaxBuildHeight()) {
                    Holder<Biome> biomeHolder = level.getBiome(blockpos);
                    debugInfo.add("Biome: " + printBiome(biomeHolder));

                    if (serverChunk != null) {
                        float moonBrightness = level.getMoonBrightness();
                        long inhabitedTime = serverChunk.getInhabitedTime();
                        DifficultyInstance difficulty = new DifficultyInstance(
                                level.getDifficulty(),
                                level.getDayTime(),
                                inhabitedTime,
                                moonBrightness
                        );

                        debugInfo.add(String.format(Locale.ROOT,
                                "Local Difficulty: %.2f // %.2f (Day %d)",
                                difficulty.getEffectiveDifficulty(),
                                difficulty.getSpecialMultiplier(),
                                level.getDayTime() / 24000L
                        ));
                    } else {
                        debugInfo.add("Local Difficulty: ??");
                    }
                }

                if (serverChunk != null && serverChunk.isOldNoiseGeneration()) {
                    debugInfo.add("Blending: Old");
                }
            }

            ServerLevel serverLevel = getServerLevel();
            if (serverLevel != null) {
                ServerChunkCache chunkCache = serverLevel.getChunkSource();
                ChunkGenerator generator = chunkCache.getGenerator();
                RandomState randomState = chunkCache.randomState();

                generator.addDebugScreenInfo(debugInfo, randomState, blockpos);

                Climate.Sampler climateSampler = randomState.sampler();
                BiomeSource biomeSource = generator.getBiomeSource();
                biomeSource.addDebugInfo(debugInfo, blockpos, climateSampler);

                NaturalSpawner.SpawnState spawnState = chunkCache.getLastSpawnState();
                if (spawnState != null) {
                    Object2IntMap<MobCategory> mobCounts = spawnState.getMobCategoryCounts();
                    int spawnableChunks = spawnState.getSpawnableChunkCount();
                    String mobSummary = Stream.of(MobCategory.values())
                            .map(cat -> Character.toUpperCase(cat.getName().charAt(0)) + ": " + mobCounts.getInt(cat))
                            .collect(Collectors.joining(", "));

                    debugInfo.add("SC: " + spawnableChunks + ", " + mobSummary);
                } else {
                    debugInfo.add("SC: N/A");
                }
            }

            PostChain postChain = minecraft.gameRenderer.currentEffect();
            if (postChain != null) {
                debugInfo.add("Shader: " + postChain.getName());
            }

            String soundDebug = minecraft.getSoundManager().getDebugString();
            float mood = minecraft.player.getCurrentMood();
            debugInfo.add(soundDebug + String.format(Locale.ROOT, " (Mood %d%%)", Math.round(mood * 100.0F)));

            cir.setReturnValue(debugInfo);
        }
    }

    @Inject(method = "getSystemInformation", at = @At("RETURN"), cancellable = true)
    private void getSystemInformation(CallbackInfoReturnable<List<String>> cir, @Local List<String> debugInfo) {
        if (!minecraft.showOnlyReducedInfo() && Services.CONFIG.config().hideCoordinates()) {
            if (block.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) block).getBlockPos();
                BlockState blockState = minecraft.level.getBlockState(blockPos);

                debugInfo.add("");
                debugInfo.add(ChatFormatting.UNDERLINE + "Targeted Block:");
                debugInfo.add(String.valueOf(BuiltInRegistries.BLOCK.getKey(blockState.getBlock())));

                for (Map.Entry<Property<?>, Comparable<?>> propertyEntry : blockState.getValues().entrySet()) {
                    debugInfo.add(getPropertyValueString(propertyEntry));
                }

                blockState.getTags()
                        .map(tag -> "#" + tag.location())
                        .forEach(debugInfo::add);
            }

            if (liquid.getType() == HitResult.Type.BLOCK) {
                BlockPos fluidPos = ((BlockHitResult) liquid).getBlockPos();
                FluidState fluidState = minecraft.level.getFluidState(fluidPos);

                debugInfo.add("");
                debugInfo.add(ChatFormatting.UNDERLINE + "Targeted Fluid:");
                debugInfo.add(String.valueOf(BuiltInRegistries.FLUID.getKey(fluidState.getType())));

                for (Map.Entry<Property<?>, Comparable<?>> propertyEntry : fluidState.getValues().entrySet()) {
                    debugInfo.add(getPropertyValueString(propertyEntry));
                }

                fluidState.getTags()
                        .map(tag -> "#" + tag.location())
                        .forEach(debugInfo::add);
            }

            Entity targetedEntity = minecraft.crosshairPickEntity;
            if (targetedEntity != null) {
                debugInfo.add("");
                debugInfo.add(ChatFormatting.UNDERLINE + "Targeted Entity");
                debugInfo.add(String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(targetedEntity.getType())));
            }

            cir.setReturnValue(debugInfo);
        }
    }
}
