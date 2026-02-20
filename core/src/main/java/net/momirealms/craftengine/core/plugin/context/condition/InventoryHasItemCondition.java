package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;

import java.util.Optional;

public final class InventoryHasItemCondition<CTX extends Context> implements Condition<CTX> {
    private final Key itemId;
    private final NumberProvider count;

    private InventoryHasItemCondition(Key itemId, NumberProvider count) {
        this.itemId = itemId;
        this.count = count;
    }

    @Override
    public boolean test(CTX ctx) {
        Optional<Player> optionalPlayer = ctx.getOptionalParameter(DirectContextParameters.PLAYER);
        if (optionalPlayer.isEmpty()) {
            return false;
        }
        Player player = optionalPlayer.get();
        return player.clearOrCountMatchingInventoryItems(this.itemId, 0) >= this.count.getInt(ctx);
    }

    public static <CTX extends Context> ConditionFactory<CTX, InventoryHasItemCondition<CTX>> factory() {
        return new Factory<>();
    }

    private static class Factory<CTX extends Context> implements ConditionFactory<CTX, InventoryHasItemCondition<CTX>> {

        @Override
        public InventoryHasItemCondition<CTX> create(ConfigSection section) {
            return new InventoryHasItemCondition<>(
                    section.getNonNullIdentifier("id", "item"),
                    NumberProviders.fromObject(section.getOrDefault(1, "count", "amount"))
            );
        }
    }
}