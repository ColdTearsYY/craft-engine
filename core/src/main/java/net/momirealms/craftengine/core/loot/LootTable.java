package net.momirealms.craftengine.core.loot;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.entry.LootEntryContainer;
import net.momirealms.craftengine.core.loot.entry.LootEntryContainers;
import net.momirealms.craftengine.core.loot.function.LootFunction;
import net.momirealms.craftengine.core.loot.function.LootFunctions;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class LootTable<T> {
    private final List<LootPool<T>> pools;
    private final List<LootFunction<T>> functions;
    private final BiFunction<Item<T>, LootContext, Item<T>> compositeFunction;

    public LootTable(List<LootPool<T>> pools, List<LootFunction<T>> functions) {
        this.pools = pools;
        this.functions = functions;
        this.compositeFunction = LootFunctions.compose(functions);
    }

    @NotNull
    public static <T> LootTable<T> fromConfig(@NotNull ConfigSection section) {
        List<LootPool<T>> lootPools = section.parseSectionList(innerSection -> {
            NumberProvider rolls = innerSection.getValueOrDefault(ConfigValue::getAsNumber, ConfigConstants.CONSTANT_ONE, "rolls");
            NumberProvider bonus_rolls = innerSection.getValueOrDefault(ConfigValue::getAsNumber, ConfigConstants.CONSTANT_ONE, "bonus_rolls", "bonus-rolls");
            List<Condition<LootContext>> conditions = innerSection.parseSectionList(CommonConditions::fromConfig, "conditions");
            List<LootEntryContainer<T>> containers = innerSection.parseSectionList(LootEntryContainers::fromConfig, "entries");
            List<LootFunction<T>> functions = innerSection.parseSectionList(LootFunctions::fromConfig, "functions");
            return new LootPool<>(containers, conditions, functions, rolls, bonus_rolls);
        }, "pools");
        return new LootTable<>(lootPools, section.parseSectionList(LootFunctions::fromConfig, "functions"));
    }

    public List<Item<T>> getRandomItems(ContextHolder parameters, World world) {
        return this.getRandomItems(parameters, world, null);
    }

    public List<Item<T>> getRandomItems(ContextHolder parameters, World world, @Nullable Player player) {
        return this.getRandomItems(new LootContext(world, player, player == null ? 1f : (float) player.luck(), parameters));
    }

    private List<Item<T>> getRandomItems(LootContext context) {
        ArrayList<Item<T>> list = new ArrayList<>();
        this.getRandomItems(context, list::add);
        return list;
    }

    public void getRandomItems(LootContext context, Consumer<Item<T>> lootConsumer) {
        this.getRandomItemsRaw(context, createFunctionApplier(createStackSplitter(lootConsumer), context));
    }

    private Consumer<Item<T>> createFunctionApplier(Consumer<Item<T>> lootConsumer, LootContext context) {
        return (item -> {
            for (LootFunction<T> function : this.functions) {
                function.apply(item, context);
            }
            lootConsumer.accept(item);
        });
    }

    private Consumer<Item<T>> createStackSplitter(Consumer<Item<T>> consumer) {
        return (item) -> {
            if (item.count() < item.maxStackSize()) {
                consumer.accept(item);
            } else {
                int remaining = item.count();
                while (remaining > 0) {
                    Item<T> splitItem = item.copyWithCount(Math.min(item.maxStackSize(), remaining));
                    remaining -= splitItem.count();
                    consumer.accept(splitItem);
                }
            }
        };
    }

    public void getRandomItemsRaw(LootContext context, Consumer<Item<T>> lootConsumer) {
        Consumer<Item<T>> consumer = LootFunction.decorate(this.compositeFunction, lootConsumer, context);
        for (LootPool<T> pool : this.pools) {
            pool.addRandomItems(consumer, context);
        }
    }
}
