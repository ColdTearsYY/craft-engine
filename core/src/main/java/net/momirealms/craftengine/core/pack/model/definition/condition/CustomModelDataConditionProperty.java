package net.momirealms.craftengine.core.pack.model.definition.condition;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public final class CustomModelDataConditionProperty implements ConditionProperty {
    public static final ConditionPropertyFactory<CustomModelDataConditionProperty> FACTORY = new Factory();
    public static final ConditionPropertyReader<CustomModelDataConditionProperty> READER = new Reader();
    private final int index;

    public CustomModelDataConditionProperty(int index) {
        this.index = index;
    }

    public int index() {
        return this.index;
    }

    @Override
    public void accept(JsonObject jsonObject) {
        jsonObject.addProperty("property", "custom_model_data");
        if (this.index != 0)
            jsonObject.addProperty("index", this.index);
    }

    private static class Factory implements ConditionPropertyFactory<CustomModelDataConditionProperty> {
        @Override
        public CustomModelDataConditionProperty create(ConfigSection section) {
            int index = section.getInt("index");
            return new CustomModelDataConditionProperty(index);
        }
    }

    private static class Reader implements ConditionPropertyReader<CustomModelDataConditionProperty> {
        @Override
        public CustomModelDataConditionProperty read(JsonObject json) {
            int index = json.has("index") ? json.get("index").getAsInt() : 0;
            return new CustomModelDataConditionProperty(index);
        }
    }
}
