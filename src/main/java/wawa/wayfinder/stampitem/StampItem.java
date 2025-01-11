package wawa.wayfinder.stampitem;

import wawa.wayfinder.AllComponents;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class StampItem extends Item {
    public StampItem(Properties settings) {
        super(settings.component(AllComponents.STAMP, new StampComponent(ResourceLocation.parse("missingno"))));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, tooltip, type);
        tooltip.add(Component.translatable(
                Optional.ofNullable(stack.get(AllComponents.STAMP)).orElse(StampComponent.unknown)
                        .getTranslationString()
        ).withColor(CommonColors.LIGHT_GRAY));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.ofNullable(stack.get(AllComponents.STAMP)).map(StampComponent::getTooltipData);
    }
}
