package wawa.wayfinder.stampitem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import wawa.wayfinder.AllComponents;
import wawa.wayfinder.AllItems;
import wawa.wayfinder.Wayfinder;

import java.util.*;
import java.util.stream.Collectors;

public class StampGroups extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
    public static final Codec<List<ResourceLocation>> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("textures").forGetter(list -> list)
            ).apply(instance, list -> list)
    );
    public static final ResourceLocation UNGROUPED = Wayfinder.id("ungrouped");
    public static final ResourceLocation ID = Wayfinder.id("stamp_group");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<ResourceLocation, Set<ResourceLocation>> groupToStamps = new HashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> stampToGroup = new HashMap<>();

    @Nullable
    public static ResourceLocation getGroup(ResourceLocation texture) {
        if (!inGroup(texture))
            return null;
        return stampToGroup.get(texture);
    }
    public static boolean inGroup(ResourceLocation texture) {
        return stampToGroup.get(texture) != null && !UNGROUPED.equals(stampToGroup.get(texture));
    }
    public static int groupSizeOfTexture(ResourceLocation texture) {
        if (!inGroup(texture))
            return 0;
        return groupToStamps.get(stampToGroup.get(texture)).size();
    }

    public StampGroups() {
        super(GSON, "stamp_group");
    }

    public static void sendToPlayer(ServerPlayer player) {
        player.connection.send(new ClientboundCustomPayloadPacket(
                new Payload(stampToGroup.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue())).collect(Collectors.toList()))
        ));
    }

    public static void handlePayload(Payload payload) {
        groupToStamps.clear();
        stampToGroup.clear();
        for (Pair<ResourceLocation, ResourceLocation> pair : payload.payload) {
            addStamp(pair.getSecond(), pair.getFirst());
        }
    }

    private static void addStamp(ResourceLocation group, ResourceLocation texture) {
        Set<ResourceLocation> groupSet = groupToStamps.computeIfAbsent(group, g -> new HashSet<>());
        if (stampToGroup.containsKey(texture)) {
            Wayfinder.LOGGER.error("Tried to add texture {} to group {}, but is already in {}", texture, group, stampToGroup.get(texture));
            return;
        }
        groupSet.add(texture);
        stampToGroup.put(texture, group);
    }

    public static void generatePresetStamps(FabricItemGroupEntries content) {
        for (Map.Entry<ResourceLocation, Set<ResourceLocation>> group : groupToStamps.entrySet()) {
            StampComponent component = new StampComponent(group.getValue().stream().toList());
            ItemStack itemStack = new ItemStack(AllItems.STAMP);
            itemStack.set(AllComponents.STAMP, component);
            content.accept(itemStack);
        }
        for (ResourceLocation texture : stampToGroup.keySet()) {
            StampComponent component = StampComponent.single(texture);
            ItemStack itemStack = new ItemStack(AllItems.STAMP);
            itemStack.set(AllComponents.STAMP, component);
            content.accept(itemStack);
        }
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        groupToStamps.clear();
        stampToGroup.clear();
        return super.prepare(resourceManager, profiler);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            List<ResourceLocation> textures = CODEC.decode(JsonOps.INSTANCE, entry.getValue()).getOrThrow().getFirst();
            ResourceLocation group = entry.getKey();
            for (ResourceLocation texture : textures) {
                addStamp(group, texture);
            }
        }
    }

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public record Payload(List<Pair<ResourceLocation, ResourceLocation>> payload) implements CustomPacketPayload {
        private static final StreamCodec<FriendlyByteBuf, Pair<ResourceLocation, ResourceLocation>> PAIR_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC,
                Pair::getFirst,
                ResourceLocation.STREAM_CODEC,
                Pair::getSecond,
                Pair::new
        );
        private static final StreamCodec<FriendlyByteBuf, List<Pair<ResourceLocation, ResourceLocation>>> LIST_CODEC = StreamCodec.composite(
                PAIR_CODEC.apply(ByteBufCodecs.list()),
                list -> list,
                list -> list
        );
        public static final StreamCodec<FriendlyByteBuf, Payload> STREAM_CODEC = CustomPacketPayload.codec(Payload::write, Payload::new);
        public static final CustomPacketPayload.Type<Payload> TYPE = new CustomPacketPayload.Type<>(ID);

        private Payload(FriendlyByteBuf buf) {
            this(LIST_CODEC.decode(buf));
        }

        private void write(FriendlyByteBuf buf) {
            LIST_CODEC.encode(buf, payload);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
