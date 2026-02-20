package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.item.processor.ItemProcessor;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;

public interface ItemProcessorFactory<T extends ItemProcessor> {

    T create(ConfigValue value);
}
