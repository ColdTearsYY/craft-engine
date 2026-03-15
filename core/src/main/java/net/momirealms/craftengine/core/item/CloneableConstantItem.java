package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.util.Key;

public class CloneableConstantItem implements BuildableItem {
    private final Item item;

    private CloneableConstantItem(Item item) {
        this.item = item;
    }

    public static CloneableConstantItem of(Item item) {
        return new CloneableConstantItem(item);
    }

    @Override
    public Key id() {
        return this.item.id();
    }

    @Override
    public Item buildItem(ItemBuildContext context, int count) {
        return this.item.copyWithCount(count);
    }

    @Override
    public boolean isEmpty() {
        return this.item.isEmpty();
    }
}
