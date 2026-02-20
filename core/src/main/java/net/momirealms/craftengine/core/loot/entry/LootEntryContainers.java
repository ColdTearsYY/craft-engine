package net.momirealms.craftengine.core.loot.entry;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class LootEntryContainers {
    public static final LootEntryContainerType<?> ALTERNATIVES = register(Key.ce("alternatives"), AlternativesLootEntryContainer.FACTORY);
    public static final LootEntryContainerType<?> IF_ELSE = register(Key.ce("if_else"), AlternativesLootEntryContainer.FACTORY);
    public static final LootEntryContainerType<?> ITEM = register(Key.ce("item"), SingleItemLootEntryContainer.FACTORY);
    public static final LootEntryContainerType<?> EXP = register(Key.ce("exp"), ExpLootEntryContainer.FACTORY);
    public static final LootEntryContainerType<?> FURNITURE_ITEM = register(Key.ce("furniture_item"), FurnitureItemLootEntryContainer.FACTORY);
    public static final LootEntryContainerType<?> EMPTY = register(Key.ce("empty"), EmptyLoopEntryContainer.FACTORY);

    private LootEntryContainers() {}

    public static <T> LootEntryContainerType<T> register(Key key, LootEntryContainerFactory<T> factory) {
        LootEntryContainerType<T> type = new LootEntryContainerType<>(key, factory);
        ((WritableRegistry<LootEntryContainerType<?>>) BuiltInRegistries.LOOT_ENTRY_CONTAINER_TYPE)
                .register(ResourceKey.create(Registries.LOOT_ENTRY_CONTAINER_TYPE.location(), key), type);
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <T> LootEntryContainer<T> fromConfig(ConfigSection section) {
        String type = section.getNonNullString("type");
        Key key = Key.ce(type);
        LootEntryContainerType<T> containerType = (LootEntryContainerType<T>) BuiltInRegistries.LOOT_ENTRY_CONTAINER_TYPE.getValue(key);
        if (containerType == null) {
            throw new KnownResourceException("loot.entry.unknown_type", section.assemblePath("type"), type);
        }
        return containerType.factory().create(section);
    }
}
