package net.momirealms.craftengine.core.block.properties;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface PropertyFactory<T extends Comparable<T>> {

    Property<T> create(String name, ConfigSection section);
}
