package beeisyou.mapdrawing.stampitem;

import beeisyou.mapdrawing.AllComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class StampItem extends Item {
    public StampItem(Settings settings) {
        super(settings.component(AllComponents.STAMP, new StampComponent(Identifier.of("missingno"))));
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.ofNullable(stack.get(AllComponents.STAMP)).map(StampComponent::getTooltipData);
    }
}
