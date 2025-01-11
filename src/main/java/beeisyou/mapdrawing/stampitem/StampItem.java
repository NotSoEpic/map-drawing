package beeisyou.mapdrawing.stampitem;

import beeisyou.mapdrawing.AllComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class StampItem extends Item {
    public StampItem(Settings settings) {
        super(settings.component(AllComponents.STAMP, new StampComponent(Identifier.of("missingno"))));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable(
                Optional.ofNullable(stack.get(AllComponents.STAMP)).orElse(StampComponent.unknown)
                        .getTranslationString()
        ).withColor(Colors.LIGHT_GRAY));
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.ofNullable(stack.get(AllComponents.STAMP)).map(StampComponent::getTooltipData);
    }
}
