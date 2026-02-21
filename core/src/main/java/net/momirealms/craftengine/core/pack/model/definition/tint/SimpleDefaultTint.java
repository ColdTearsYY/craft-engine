package net.momirealms.craftengine.core.pack.model.definition.tint;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import org.incendo.cloud.type.Either;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SimpleDefaultTint implements Tint {
    public static final TintFactory<SimpleDefaultTint> FACTORY = new Factory();
    public static final TintReader<SimpleDefaultTint> READER = new Reader();
    private final Either<Integer, List<Float>> defaultValue;
    private final Key type;

    public SimpleDefaultTint(Key type, @Nullable Either<Integer, List<Float>> defaultValue) {
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public Either<Integer, List<Float>> defaultValue() {
        return this.defaultValue;
    }

    public Key type() {
        return this.type;
    }

    @Override
    public JsonObject get() {
        JsonObject json = new JsonObject();
        json.addProperty("type", this.type.asMinimalString());
        applyAnyTint(json, this.defaultValue, "default");
        return json;
    }

    private static class Factory implements TintFactory<SimpleDefaultTint> {
        @Override
        public SimpleDefaultTint create(ConfigSection section) {
            Either<Integer, List<Float>> value = section.getOrDefault(this::parseTintValue, Either.ofPrimary(0), "default", "value");
            Key type = section.getNonNullIdentifier("type");
            return new SimpleDefaultTint(type, parseTintValue(value));
        }
    }

    private static class Reader implements TintReader<SimpleDefaultTint> {
        @Override
        public SimpleDefaultTint read(JsonObject json) {
            return new SimpleDefaultTint(Key.of(json.get("type").getAsString()), parseTintValue(json.get("default")));
        }
    }
}
