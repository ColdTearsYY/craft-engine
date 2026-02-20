package net.momirealms.craftengine.core.plugin.context.function;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.*;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.plugin.context.number.NumberProviders;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.plugin.context.selector.PlayerSelector;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;

public final class PotionEffectFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final PlayerSelector<CTX> selector;
    private final Key potionEffectType;
    private final NumberProvider duration;
    private final NumberProvider amplifier;
    private final boolean ambient;
    private final boolean particles;

    private PotionEffectFunction(List<Condition<CTX>> predicates, NumberProvider duration, NumberProvider amplifier, boolean ambient, boolean particles, PlayerSelector<CTX> selector, Key potionEffectType) {
        super(predicates);
        this.potionEffectType = potionEffectType;
        this.duration = duration;
        this.amplifier = amplifier;
        this.selector = selector;
        this.ambient = ambient;
        this.particles = particles;
    }

    @Override
    public void runInternal(CTX ctx) {
        if (this.selector == null) {
            ctx.getOptionalParameter(DirectContextParameters.PLAYER).ifPresent(it -> {
                it.addPotionEffect(this.potionEffectType, this.duration.getInt(ctx), this.amplifier.getInt(ctx), this.ambient, this.particles);
            });
        } else {
            for (Player target : this.selector.get(ctx)) {
                RelationalContext relationalContext = ViewerContext.of(ctx, PlayerOptionalContext.of(target));
                target.addPotionEffect(this.potionEffectType, this.duration.getInt(relationalContext), this.amplifier.getInt(relationalContext), this.ambient, this.particles);
            }
        }
    }

    public static <CTX extends Context> FunctionFactory<CTX, PotionEffectFunction<CTX>> factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX, PotionEffectFunction<CTX>> {

        public Factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public PotionEffectFunction<CTX> create(ConfigSection section) {
            return new PotionEffectFunction<>(
                    getPredicates(section),
                    NumberProviders.fromObject(section.getOrDefault(20, "duration")),
                    NumberProviders.fromObject(section.getOrDefault(0, "amplifier")),
                    section.getBoolean("ambient"),
                    section.getBoolean(true, "particles"),
                    getPlayerSelector(section),
                    section.getNonNullIdentifier("potion_effect", "potion-effect")
            );
        }
    }
}