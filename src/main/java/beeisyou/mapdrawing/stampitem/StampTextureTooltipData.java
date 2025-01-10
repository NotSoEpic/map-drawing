package beeisyou.mapdrawing.stampitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public record StampTextureTooltipData(Identifier texture) implements TooltipData {
    public static final StampTextureTooltipData DEFAULT = new StampTextureTooltipData(Identifier.ofVanilla("missingno"));
    public static final Codec<StampTextureTooltipData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Identifier.CODEC.fieldOf("texture").forGetter(StampTextureTooltipData::texture)
            ).apply(instance, StampTextureTooltipData::new)
    );
    public static final PacketCodec<ByteBuf, StampTextureTooltipData> PACKET_CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            StampTextureTooltipData::texture,
            StampTextureTooltipData::new
    );
}
