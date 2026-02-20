package net.momirealms.craftengine.core.loot.entry;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.loot.function.LootFunction;
import net.momirealms.craftengine.core.loot.function.LootFunctions;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class FurnitureItemLootEntryContainer<T> extends SingleItemLootEntryContainer<T> {
    public static final LootEntryContainerFactory<?> FACTORY = new Factory<>();
    private final boolean hasFallback;

    private FurnitureItemLootEntryContainer(@Nullable Key item,
                                            List<Condition<LootContext>> conditions,
                                            List<LootFunction<T>> lootFunctions,
                                            int weight,
                                            int quality) {
        super(item, conditions, lootFunctions, weight, quality);
        this.hasFallback = item != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void createItem(Consumer<Item<T>> lootConsumer, LootContext context) {
        Optional<Item<?>> optionalItem = context.getOptionalParameter(DirectContextParameters.FURNITURE_ITEM);
        if (optionalItem.isPresent()) {
            lootConsumer.accept((Item<T>) optionalItem.get());
        } else if (this.hasFallback) {
            super.createItem(lootConsumer, context);
        }
    }

    private static class Factory<A> implements LootEntryContainerFactory<A> {

        @Override
        public LootEntryContainer<A> create(ConfigSection section) {
            return new FurnitureItemLootEntryContainer<>(
                    section.getIdentifier("item"),
                    section.parseSectionList(CommonConditions::fromConfig, "conditions"),
                    section.parseSectionList(LootFunctions::fromConfig, "functions"),
                    section.getInt(1, "weight"),
                    section.getInt("quality")
            );
        }
    }
}
