package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.DataComponentKeys;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemProcessorFactory;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.util.Key;

public final class CustomModelDataProcessor implements SimpleNetworkItemProcessor {
    public static final ItemProcessorFactory<CustomModelDataProcessor> FACTORY = new Factory();
    private final NumberProvider argument;

    public CustomModelDataProcessor(NumberProvider argument) {
        this.argument = argument;
    }

    @Override
    public <I> Item<I> apply(Item<I> item, ItemBuildContext context) {
        item.customModelData(this.argument.getInt(context));
        return item;
    }

    @Override
    public <I> Key componentType(Item<I> item, ItemBuildContext context) {
        return DataComponentKeys.CUSTOM_MODEL_DATA;
    }

    @Override
    public <I> Object[] nbtPath(Item<I> item, ItemBuildContext context) {
        return new Object[]{"CustomModelData"};
    }

    @Override
    public <I> String nbtPathString(Item<I> item, ItemBuildContext context) {
        return "CustomModelData";
    }

    private static class Factory implements ItemProcessorFactory<CustomModelDataProcessor> {

        @Override
        public CustomModelDataProcessor create(ConfigValue value) {
            return new CustomModelDataProcessor(NumberProviders.fromConfig(value));
        }
    }
}
