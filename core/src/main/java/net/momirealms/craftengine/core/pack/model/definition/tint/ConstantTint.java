package net.momirealms.craftengine.core.pack.model.definition.tint;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import org.incendo.cloud.type.Either;

import java.util.List;

public final class ConstantTint implements Tint {
    public static final TintFactory<ConstantTint> FACTORY = new Factory();
    public static final TintReader<ConstantTint> READER = new Reader();
    private final Either<Integer, List<Float>> value;

    public ConstantTint(Either<Integer, List<Float>> value) {
        this.value = value;
    }

    public Either<Integer, List<Float>> value() {
        return this.value;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "constant");
        applyAnyTint(json, this.value, "value");
        return json;
    }

    private static class Factory implements TintFactory<ConstantTint> {
        @Override
        public ConstantTint create(ConfigSection section) {
            ConfigValue
            Either<Integer, List<Float>> value = section.getNonNull(this::parseTintValue, "warning.config.item.model.tint.constant.missing_value", "value", "default");
            return new ConstantTint(value);
        }
    }

    private static class Reader implements TintReader<ConstantTint> {
        @Override
        public ConstantTint read(JsonObject json) {
            return new ConstantTint(parseTintValue(json.get("value")));
        }
    }
}
