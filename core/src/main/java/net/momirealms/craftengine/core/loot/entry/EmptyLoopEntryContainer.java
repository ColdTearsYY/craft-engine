package net.momirealms.craftengine.core.loot.entry;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.function.Consumer;

public final class EmptyLoopEntryContainer<T> extends AbstractSingleLootEntryContainer<T> {
    public static final LootEntryContainerFactory<?> FACTORY = new Factory<>();

    private EmptyLoopEntryContainer(List<Condition<LootContext>> conditions, int weight, int quality) {
        super(conditions, null, weight, quality);
    }

    @Override
    protected void createItem(Consumer<Item<T>> lootConsumer, LootContext context) {}

    private static class Factory<A> implements LootEntryContainerFactory<A> {

        @Override
        public LootEntryContainer<A> create(ConfigSection section) {
            return new EmptyLoopEntryContainer<>(
                    section.parseSectionList(CommonConditions::fromConfig, "conditions"),
                    section.getInt(1, "weight"),
                    section.getInt("quality")
            );
        }
    }
}
