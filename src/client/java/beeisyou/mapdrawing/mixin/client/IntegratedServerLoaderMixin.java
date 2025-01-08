package beeisyou.mapdrawing.mixin.client;

import beeisyou.mapdrawing.MapDrawingClient;
import beeisyou.mapdrawing.MapRegions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServerLoader.class)
public class IntegratedServerLoaderMixin {
    @Inject(method = "Lnet/minecraft/server/integrated/IntegratedServerLoader;start(Lnet/minecraft/world/level/storage/LevelStorage$Session;Ljava/lang/Runnable;)V", at = @At("HEAD"))
    private void loadMap(LevelStorage.Session session, Runnable onCancel, CallbackInfo ci) {
        MapDrawingClient.regions = MapRegions.fromFolder(MinecraftClient.getInstance().getLevelStorage().getSavesDirectory().resolve("pages"));
    }
}
