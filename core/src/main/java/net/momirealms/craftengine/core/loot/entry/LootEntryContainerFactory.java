package net.momirealms.craftengine.core.loot.entry;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface LootEntryContainerFactory<T> {

    LootEntryContainer<T> create(ConfigSection section);
}
