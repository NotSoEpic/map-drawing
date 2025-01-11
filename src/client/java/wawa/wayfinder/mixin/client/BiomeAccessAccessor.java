package wawa.wayfinder.mixin.client;

import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeAccess.class)
public interface BiomeAccessAccessor {

    @Accessor()
    long getSeed();

}
