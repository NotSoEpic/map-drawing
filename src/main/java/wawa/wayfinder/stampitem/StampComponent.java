package wawa.wayfinder.stampitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;
import wawa.wayfinder.Wayfinder;

import java.util.List;
import java.util.stream.Collectors;

public class StampComponent implements TooltipComponent {
    private List<ResourceLocation> textures;

    @Environment(EnvType.CLIENT)
    public int selectedIndex;

    public StampComponent(List<ResourceLocation> textures) {
        this.textures = textures.stream().distinct().sorted().collect(Collectors.toList());
    }

    public List<ResourceLocation> textures() {
        return textures;
    }

    public int getSize() {
        return textures.size();
    }

    public static final StampComponent unknown = StampComponent.single(Wayfinder.id("missingstamp"));
    public static final Codec<StampComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("textures").forGetter(StampComponent::textures)
            ).apply(instance, StampComponent::new)
    );

    public static StampComponent single(ResourceLocation singleTexture) {
        return new StampComponent(List.of(singleTexture));
    }

    @Override
    public String toString() {
        return "StampComponent{" +
                "textures=" + textures +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StampComponent that = (StampComponent) o;

        return textures.equals(that.textures);
    }

    @Override
    public int hashCode() {
        return textures.hashCode();
    }

    @Environment(EnvType.CLIENT)
    public String getSelectedTranslation() {
        return textures.get(selectedIndex).toLanguageKey("stamp");
    }

    @Environment(EnvType.CLIENT)
    @Nullable
    public String getGroupTranslation() {
        ResourceLocation group = StampGroups.getGroup(textures.get(selectedIndex));
        if (group == null)
            return null;
        return group.toLanguageKey("stamp_group");
    }

    @Environment(EnvType.CLIENT)
    @Nullable
    public ResourceLocation getGroup() {
        return StampGroups.getGroup(textures.get(selectedIndex));
    }

    @Environment(EnvType.CLIENT)
    public int getFullGroupSize() {
        return StampGroups.groupSizeOfTexture(textures.get(selectedIndex));
    }

    @Environment(EnvType.CLIENT)
    public void incrementSelected() {
        selectedIndex = (selectedIndex + 1) % textures.size();
    }
}
