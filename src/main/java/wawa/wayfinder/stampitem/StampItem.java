package wawa.wayfinder.stampitem;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import wawa.wayfinder.AllComponents;

import java.util.List;
import java.util.Optional;

public class StampItem extends Item {
    public StampItem(Properties settings) {
        super(settings.component(AllComponents.STAMP, StampComponent.unknown));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, tooltip, type);
        StampComponent component = Optional.ofNullable(stack.get(AllComponents.STAMP)).orElse(StampComponent.unknown);
        tooltip.add(Component.translatable(component.getSelectedTranslation()).withColor(CommonColors.LIGHT_GRAY));
        int fullSize = component.getFullGroupSize();
        int size = component.getSize();
        if (fullSize > 1) {
            String groupTranslation = component.getGroupTranslation();
            MutableComponent groupTooltip = Component.translatable(groupTranslation).withColor(CommonColors.LIGHT_GRAY);
            groupTooltip.append(String.format(" (%d / %d)", size, fullSize)).withColor(CommonColors.LIGHT_GRAY);
            if (size > fullSize) {
                groupTooltip.append(" ???").withColor(CommonColors.LIGHT_GRAY);
            }
            tooltip.add(groupTooltip);
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.ofNullable(stack.get(AllComponents.STAMP));
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (slot.mayPlace(stack) && other.isEmpty() && action == ClickAction.SECONDARY && stack.has(AllComponents.STAMP)) {
            if (player.isLocalPlayer())
                stack.get(AllComponents.STAMP).incrementSelected();
            return true;
        }
        return false;
    }
}
