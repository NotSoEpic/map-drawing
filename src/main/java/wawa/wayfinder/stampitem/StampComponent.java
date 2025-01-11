package wawa.wayfinder.stampitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public record StampComponent(Identifier texture) {
    public static final StampComponent unknown = new StampComponent(Identifier.ofVanilla("missingno"));
    public static final Codec<StampComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Identifier.CODEC.fieldOf("texture").forGetter(StampComponent::texture)
            ).apply(instance, StampComponent::new)
    );

    public static final PacketCodec<RegistryByteBuf, StampComponent> PACKET_CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            StampComponent::texture,
            StampComponent::new
    );

    public StampTextureTooltipData getTooltipData() {
        return new StampTextureTooltipData(texture);
    }
    public String getTranslationString() {
        return texture.toTranslationKey("stamp");
    }
}
