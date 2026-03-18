package net.momirealms.craftengine.bukkit.compatibility.mythicmobs;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.function.AbstractConditionalFunction;
import net.momirealms.craftengine.core.plugin.context.function.FunctionFactory;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;

import java.util.List;

public final class MythicMobsSkillFunction<CTX extends Context> extends AbstractConditionalFunction<CTX> {
    private final String skill;
    private final float power;

    private MythicMobsSkillFunction(List<Condition<CTX>> predicates,
                                    float power,
                                    String skill) {
        super(predicates);
        this.skill = skill;
        this.power = power;
    }

    @Override
    protected void runInternal(CTX ctx) {
        ctx.getOptionalParameter(DirectContextParameters.PLAYER).ifPresent(it -> {
            CraftEngine.instance().compatibilityManager().executeMMSkill(this.skill, this.power, it);
        });
    }

    public static <CTX extends Context> FunctionFactory<CTX, MythicMobsSkillFunction<CTX>> factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
        return new Factory<>(factory);
    }

    private static class Factory<CTX extends Context> extends AbstractFactory<CTX, MythicMobsSkillFunction<CTX>> {

        public Factory(java.util.function.Function<ConfigSection, Condition<CTX>> factory) {
            super(factory);
        }

        @Override
        public MythicMobsSkillFunction<CTX> create(ConfigSection section) {
            return new MythicMobsSkillFunction<>(
                    getPredicates(section),
                    section.getFloat("power", 1f),
                    section.getNonNullString("skill")
            );
        }
    }
}