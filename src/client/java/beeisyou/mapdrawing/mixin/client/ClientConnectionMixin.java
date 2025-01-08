package beeisyou.mapdrawing.mixin.client;

import beeisyou.mapdrawing.MapDrawingClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "Lnet/minecraft/network/ClientConnection;disconnect(Lnet/minecraft/network/DisconnectionInfo;)V", at = @At("HEAD"))
    private void saveMapOnLeave(DisconnectionInfo disconnectionInfo, CallbackInfo ci) {
        MapDrawingClient.regions.save();
    }
}
