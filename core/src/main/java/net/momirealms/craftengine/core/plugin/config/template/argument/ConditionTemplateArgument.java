package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public final class ConditionTemplateArgument implements TemplateArgument {
    public static final TemplateArgumentFactory<ConditionTemplateArgument> FACTORY = new Factory();
    private final TemplateArgument result;

    private ConditionTemplateArgument(TemplateArgument result) {
        this.result = result;
    }

    public TemplateArgument result() {
        return this.result;
    }

    public static ConditionTemplateArgument condition(final TemplateArgument result) {
        return new ConditionTemplateArgument(result);
    }

    @Override
    public Object get(Map<String, TemplateArgument> arguments) {
        return this.result.get(arguments);
    }

    private static class Factory extends NestedTemplateArgumentFactory<ConditionTemplateArgument> {

        @Override
        public ConditionTemplateArgument create(ConfigSection section) {
            TemplateArgument onTrue = super.fromObject(section.getValue("on_true", "on-true"));
            TemplateArgument onFalse = super.fromObject(section.getValue("on_false", "on-false"));
            return new ConditionTemplateArgument(section.getBoolean("condition") ? onTrue : onFalse);
        }
    }
}
