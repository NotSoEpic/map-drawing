package wawa.wayfinder.mapmanager;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import wawa.wayfinder.AllComponents;
import wawa.wayfinder.ClientStampTooltipComponent;
import wawa.wayfinder.mapmanager.tools.StampTool;
import wawa.wayfinder.mapmanager.tools.Tool;
import wawa.wayfinder.stampitem.StampGroups;

import java.util.*;

public class AvailableStamps {
    public static Map<ResourceLocation, List<ResourceLocation>> map = new HashMap<>();
    private static ResourceLocation selectedGroup;
    private static int selectedIndex;

    public static void recalculate(Player player) {
        map = collectAvailableStampGroups(player);
    }

    public static void select(ResourceLocation texture) {
        selectedGroup = StampGroups.getGroup(texture);
        if (selectedGroup != null)
            selectedIndex = AvailableStamps.map.get(selectedGroup).indexOf(texture);
    }

    public static void deltaSelect(int amount) {
        if (selectedGroup != null) {
            List<ResourceLocation> group = map.get(selectedGroup);
            selectedIndex = ((selectedIndex + amount) % group.size() + group.size()) % group.size();
            Tool.set(new StampTool(group.get(selectedIndex).withPath(ClientStampTooltipComponent::fromPathShorthand)));
        }
    }

    public static Map<ResourceLocation, List<ResourceLocation>> collectAvailableStampGroups(Player player) {
        boolean stampAllowed = !(Tool.get() instanceof StampTool);
        Map<ResourceLocation, List<ResourceLocation>> groups = new HashMap<>();
        for (ResourceLocation texture : collectAvailableStamps(player)) {
            ResourceLocation group = StampGroups.getGroup(texture);
            if (group == null)
                group = StampGroups.UNGROUPED;
            groups.computeIfAbsent(group, g -> new ArrayList<>()).add(texture);
            if (!stampAllowed && Tool.get() instanceof StampTool stampTool && stampTool.stamp.equals(texture.withPath(ClientStampTooltipComponent::fromPathShorthand)))
                stampAllowed = true;
        }
        if (!stampAllowed)
            Tool.set(null);
        groups.forEach((k, v) -> v.sort(ResourceLocation::compareTo));
        return groups;
    }

    private static Set<ResourceLocation> collectAvailableStamps(Player player) {
        Set<ResourceLocation> stamps = new HashSet<>();
        collectAvailableStamps(stamps, player.getInventory().items);
        return stamps;
    }

    private static void collectAvailableStamps(Set<ResourceLocation> stamps, Iterable<ItemStack> items) {
        items.forEach(i -> {
            if (i.has(AllComponents.STAMP)) {
                stamps.addAll(i.get(AllComponents.STAMP).textures());
            }
            if (i.has(DataComponents.BUNDLE_CONTENTS)) {
                collectAvailableStamps(stamps, i.get(DataComponents.BUNDLE_CONTENTS).items());
            }
            if (i.has(DataComponents.CONTAINER)) {
                collectAvailableStamps(stamps, i.get(DataComponents.CONTAINER).nonEmptyItems());
            }
        });
    }
}
