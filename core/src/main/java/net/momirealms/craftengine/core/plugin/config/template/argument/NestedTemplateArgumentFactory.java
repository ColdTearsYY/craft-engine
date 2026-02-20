package net.momirealms.craftengine.core.plugin.config.template.argument;

import net.momirealms.craftengine.core.plugin.config.ConfigValue;

import java.util.List;
import java.util.Map;

public abstract class NestedTemplateArgumentFactory<T extends TemplateArgument> implements TemplateArgumentFactory<T> {

    protected TemplateArgument fromObject(ConfigValue configValue) {
        if (configValue == null) {
            return NullTemplateArgument.INSTANCE;
        }
        if (configValue.is(List.class)) {
            return ListTemplateArgument.list(configValue.getAsList());
        } else if (configValue.is(Map.class)) {
            return TemplateArguments.fromConfig(configValue.getAsSection());
        } else {
            return ObjectTemplateArgument.object(configValue.value());
        }
    }
}
