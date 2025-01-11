package wawa.wayfinder.stampitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record StampTextureTooltipData(ResourceLocation texture) implements TooltipComponent {
    public static final StampTextureTooltipData DEFAULT = new StampTextureTooltipData(ResourceLocation.withDefaultNamespace("missingno"));
    public static final Codec<StampTextureTooltipData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(StampTextureTooltipData::texture)
            ).apply(instance, StampTextureTooltipData::new)
    );
    public static final StreamCodec<ByteBuf, StampTextureTooltipData> PACKET_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            StampTextureTooltipData::texture,
            StampTextureTooltipData::new
    );
}
