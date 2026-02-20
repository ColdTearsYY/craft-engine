package net.momirealms.craftengine.core.item.processor.lore;

import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;

class LoreFactory implements ItemProcessorFactory<LoreProcessor> {
    @Override
    public LoreProcessor create(ConfigValue value) {
        return LoreProcessor.createLoreModifier(value);
    }
}