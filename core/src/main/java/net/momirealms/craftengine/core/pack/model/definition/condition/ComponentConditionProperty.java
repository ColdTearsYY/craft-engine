package net.momirealms.craftengine.core.pack.model.definition.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public final class ComponentConditionProperty implements ConditionProperty {
    public static final ConditionPropertyFactory<ComponentConditionProperty> FACTORY = new Factory();
    public static final ConditionPropertyReader<ComponentConditionProperty> READER = new Reader();
    private final String predicate;
    private final JsonElement value;

    public ComponentConditionProperty(String predicate, JsonElement value) {
        this.predicate = predicate;
        this.value = value;
    }

    public String predicate() {
        return this.predicate;
    }

    public JsonElement value() {
        return this.value;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "component");
        jsonObject.addProperty("predicate", this.predicate);
        jsonObject.add("value", this.value);
    }

    private static class Factory implements ConditionPropertyFactory<ComponentConditionProperty> {
        @Override
        public ComponentConditionProperty create(ConfigSection section) {
            String predicate = section.getNonNullString("predicate");
            JsonElement jsonElement = section.getNonNullJson("value");
            return new ComponentConditionProperty(predicate, jsonElement);
        }
    }

    private static class Reader implements ConditionPropertyReader<ComponentConditionProperty> {
        @Override
        public ComponentConditionProperty read(JsonObject json) {
            String predicate = json.get("predicate").getAsString();
            JsonElement value = json.get("value");
            return new ComponentConditionProperty(predicate, value);
        }
    }
}
