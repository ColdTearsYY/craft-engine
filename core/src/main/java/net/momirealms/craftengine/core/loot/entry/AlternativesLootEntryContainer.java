package net.momirealms.craftengine.core.loot.entry;

import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;

import java.util.List;

public final class AlternativesLootEntryContainer<T> extends AbstractCompositeLootEntryContainer<T> {
    public static final LootEntryContainerFactory<?> FACTORY = new Factory<>();

    private AlternativesLootEntryContainer(List<Condition<LootContext>> conditions, List<LootEntryContainer<T>> children) {
        super(conditions, children);
    }

    @Override
    protected LootEntryContainer<T> compose(List<? extends LootEntryContainer<T>> children) {
        return switch (children.size()) {
            case 0 -> LootEntryContainer.alwaysFalse();
            case 1 -> children.get(0);
            case 2 -> children.get(0).or(children.get(1));
            default -> (context, choiceConsumer) -> {
                for (LootEntryContainer<T> child : children) {
                    if (child.expand(context, choiceConsumer)) {
                        return true;
                    }
                }
                return false;
            };
        };
    }

    private static class Factory<A> implements LootEntryContainerFactory<A> {

        @Override
        public LootEntryContainer<A> create(ConfigSection section) {
            return new AlternativesLootEntryContainer<>(
                    section.parseSectionList(CommonConditions::fromConfig, "conditions"),
                    section.parseSectionList(LootEntryContainers::fromConfig, "children")
            );
        }
    }
}
