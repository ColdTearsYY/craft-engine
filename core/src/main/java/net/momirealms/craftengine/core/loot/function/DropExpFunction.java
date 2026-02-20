package net.momirealms.craftengine.core.loot.function;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;

public final class DropExpFunction<T> extends AbstractLootConditionalFunction<T> {
    public static final LootFunctionFactory<?> FACTORY = new Factory<>();
    private final NumberProvider value;

    private DropExpFunction(List<Condition<LootContext>> predicates, NumberProvider value) {
        super(predicates);
        this.value = value;
    }

    @Override
    protected Item<T> applyInternal(Item<T> item, LootContext context) {
        context.getOptionalParameter(DirectContextParameters.POSITION)
                .ifPresent(it -> it.world().dropExp(it, value.getInt(context)));
        return item;
    }

    private static class Factory<T> implements LootFunctionFactory<T> {

        @Override
        public LootFunction<T> create(ConfigSection section) {
            return new DropExpFunction<>(
                    section.parseSectionList(CommonConditions::fromConfig, "conditions"),
                    section.getNonNullValue(ConfigConstants.ARGUMENT_NUMBER, "count", "amount").getAsNumber()
            );
        }
    }
}
