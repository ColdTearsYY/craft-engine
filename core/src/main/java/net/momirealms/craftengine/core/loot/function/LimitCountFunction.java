package net.momirealms.craftengine.core.loot.function;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class LimitCountFunction<T> extends AbstractLootConditionalFunction<T> {
    public static final LootFunctionFactory<?> FACTORY = new Factory<>();
    @Nullable
    public final NumberProvider min;
    @Nullable
    public final NumberProvider max;

    private LimitCountFunction(List<Condition<LootContext>> predicates, @Nullable NumberProvider min, @Nullable NumberProvider max) {
        super(predicates);
        this.min = min;
        this.max = max;
    }

    @Override
    protected Item<T> applyInternal(Item<T> item, LootContext context) {
        int amount = item.count();
        if (this.min != null) {
            int minAmount = this.min.getInt(context);
            if (amount < minAmount) {
                item.count(minAmount);
            }
        }
        if (this.max != null) {
            int maxAmount = this.max.getInt(context);
            if (amount > maxAmount) {
                item.count(maxAmount);
            }
        }
        return item;
    }

    private static class Factory<A> implements LootFunctionFactory<A> {

        @Override
        public LootFunction<A> create(ConfigSection section) {
            return new LimitCountFunction<>(
                    section.parseSectionList(CommonConditions::fromConfig, "conditions"),
                    section.getValue(ConfigValue::getAsNumber, "min"),
                    section.getValue(ConfigValue::getAsNumber, "max")
            );
        }
    }
}
