package wawa.wayfinder.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import wawa.wayfinder.stampitem.StampGroups;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class StampGroupProvider implements DataProvider {
    final PackOutput.PathProvider groupPathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;
    public StampGroupProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.groupPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "stamp_group");
        this.registries = registries;
    }

    public abstract void generateGroups(GroupBuilder builder);

    @FunctionalInterface
    public interface GroupBuilder {
        void add(String group, String texture);

        default void add(ResourceLocation group, ResourceLocation texture) {
            add(group.toString(), texture.toString());
        }

        default void add(ResourceLocation group, Collection<ResourceLocation> textures) {
            for (ResourceLocation texture : textures) {
                add(group, texture);
            }
        }

        default void add(ResourceLocation group, String namespace, String... textures) {
            for (String path : textures) {
                add(group, ResourceLocation.fromNamespaceAndPath(namespace, path));
            }
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return registries.thenCompose(lookup -> {
            TreeMap<ResourceLocation, List<ResourceLocation>> groups = new TreeMap<>();
            generateGroups((key, value) -> {
                groups.computeIfAbsent(ResourceLocation.parse(key), r -> new ArrayList<>()).add(ResourceLocation.parse(value));
            });

            final List<CompletableFuture<?>> list = new ArrayList<>();

            for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : groups.entrySet()) {
                list.add(DataProvider.saveStable(output, lookup, StampGroups.CODEC, entry.getValue(), groupPathProvider.json(entry.getKey())));
            }
            return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Stamp Group";
    }
}
