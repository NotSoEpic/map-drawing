package beeisyou.mapdrawing.mixin.client;

import beeisyou.mapdrawing.MapDrawingClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.SaveLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "Lnet/minecraft/client/MinecraftClient;startIntegratedServer(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/resource/ResourcePackManager;Lnet/minecraft/server/SaveLoader;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;disconnect()V", shift = At.Shift.AFTER))
    private void setSingeplayerPath(net.minecraft.world.level.storage.LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, boolean newWorld, CallbackInfo ci) {
        MapDrawingClient.regions.save();
        MapDrawingClient.regions.clear();
        MapDrawingClient.regions.setRegionPathSingleplayer(session.getDirectoryName());
    }
    @Inject(method = "Lnet/minecraft/client/MinecraftClient;disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"))
    private void unloadMapData(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
        MapDrawingClient.regions.save();
        MapDrawingClient.regions.clear();
        MapDrawingClient.regions.clearRegionPath();
        MapDrawingClient.movementHistory.clear();
    }
    @Inject(method = "Lnet/minecraft/client/MinecraftClient;joinWorld(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen$WorldEntryReason;)V", at = @At("HEAD"))
    private void unloadMapData2(ClientWorld world, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci) {
        MapDrawingClient.regions.save();
        MapDrawingClient.regions.clear();
        MapDrawingClient.movementHistory.clear();
    }
}
