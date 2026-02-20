package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelector;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;
import java.util.Optional;

public final class DamageFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private static final Key GENERIC = Key.of("generic");
    private final PlayerSelector<CTX> selector;
    private final Key damageType;
    private final NumberProvider amount;

    private DamageFunction(List<Condition<CTX>> predicates, Key damageType, NumberProvider amount, PlayerSelector<CTX> selector) {
        super(predicates);
        this.selector = selector;
        this.damageType = damageType;
        this.amount = amount;
    }

    @Override
    protected void runInternal(CTX ctx) {
        if (this.selector != null) {
            this.selector.get(ctx).forEach(p -> p.damage(this.amount.getDouble(ctx), this.damageType, null));
        } else {
            ctx.getOptionalParameter(DirectContextParameters.PLAYER).ifPresent(it -> it.damage(this.amount.getDouble(ctx), this.damageType, null));
        }
    }

    public static <CTX extends Context> FunctionFactory<CTX, DamageFunction<CTX>> factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX, DamageFunction<CTX>> {

        public Factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public DamageFunction<CTX> create(ConfigSection section) {
            return new DamageFunction<>(
                    getPredicates(section),
                    Optional.ofNullable(section.getIdentifier("damage_type", "damage-type")).orElse(GENERIC),
                    NumberProviders.fromObject(section.getOrDefault(1f, "amount", "damage")),
                    getPlayerSelector(section)
            );
        }
    }
}