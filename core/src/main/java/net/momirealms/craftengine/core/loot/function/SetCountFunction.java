package net.momirealms.craftengine.core.loot.function;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;

import java.util.List;

public final class SetCountFunction<T> extends AbstractLootConditionalFunction<T> {
    public static final LootFunctionFactory<?> FACTORY = new Factory<>();
    public final NumberProvider value;
    public final boolean add;

    private SetCountFunction(List<Condition<LootContext>> conditions,
                             NumberProvider value,
                             boolean add) {
        super(conditions);
        this.value = value;
        this.add = add;
    }

    @Override
    protected Item<T> applyInternal(Item<T> item, LootContext context) {
        int amount = this.add ? item.count() : 0;
        item.count(amount + this.value.getInt(context));
        return item;
    }

    private static class Factory<A> implements LootFunctionFactory<A> {
        private static final String[] COUNT = new String[] {"count", "amount"};

        @Override
        public LootFunction<A> create(ConfigSection section) {
            return new SetCountFunction<>(
                    section.getList("conditions", CommonConditions::fromConfig),
                    section.getNonNullValue(COUNT, ConfigConstants.ARGUMENT_NUMBER).getAsNumber(),
                    section.getBoolean("add")
            );
        }
    }
}
