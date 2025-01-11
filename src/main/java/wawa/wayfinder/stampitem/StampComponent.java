package wawa.wayfinder.stampitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record StampComponent(ResourceLocation texture) {
    public static final StampComponent unknown = new StampComponent(ResourceLocation.withDefaultNamespace("missingno"));
    public static final Codec<StampComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(StampComponent::texture)
            ).apply(instance, StampComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StampComponent> PACKET_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            StampComponent::texture,
            StampComponent::new
    );

    public StampTextureTooltipData getTooltipData() {
        return new StampTextureTooltipData(texture);
    }
    public String getTranslationString() {
        return texture.toLanguageKey("stamp");
    }
}
