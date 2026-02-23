package net.momirealms.craftengine.core.loot.function;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.data.Enchantment;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.loot.function.formula.Formula;
import net.momirealms.craftengine.core.loot.function.formula.Formulas;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;
import java.util.Optional;

public final class ApplyBonusCountFunction<T> extends AbstractLootConditionalFunction<T> {
    public static final LootFunctionFactory<?> FACTORY = new Factory<>();
    public final Key enchantment;
    public final Formula formula;

    private ApplyBonusCountFunction(List<Condition<LootContext>> predicates,
                                    Key enchantment,
                                    Formula formula) {
        super(predicates);
        this.enchantment = enchantment;
        this.formula = formula;
    }

    @Override
    protected Item<T> applyInternal(Item<T> item, LootContext context) {
        Optional<Item<?>> itemInHand = context.getOptionalParameter(DirectContextParameters.ITEM_IN_HAND);
        int level = itemInHand.map(value -> value.getEnchantment(this.enchantment).map(Enchantment::level).orElse(0)).orElse(0);
        int newCount = this.formula.apply(item.count(), level);
        item.count(newCount);
        return item;
    }

    private static class Factory<T> implements LootFunctionFactory<T> {

        @Override
        public LootFunction<T> create(ConfigSection section) {
            return new ApplyBonusCountFunction<>(
                    section.parseSectionList(CommonConditions::fromConfig, "conditions"),
                    section.getNonNullIdentifier("enchantment"),
                    Formulas.fromConfig(section.getNonNullSection("formula"))
            );
        }
    }
}
