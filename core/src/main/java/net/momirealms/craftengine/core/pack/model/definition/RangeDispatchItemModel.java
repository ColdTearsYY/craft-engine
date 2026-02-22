package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.definition.rangedisptach.RangeDispatchProperties;
import net.momirealms.craftengine.core.pack.model.definition.rangedisptach.RangeDispatchProperty;
import net.momirealms.craftengine.core.pack.model.generation.ModelGeneration;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class RangeDispatchItemModel implements ItemModel {
    public static final ItemModelFactory<RangeDispatchItemModel> FACTORY = new Factory();
    public static final ItemModelReader<RangeDispatchItemModel> READER = new Reader();
    private final RangeDispatchProperty property;
    private final float scale;
    private final ItemModel fallBack;
    private final Map<Float, ItemModel> entries;

    public RangeDispatchItemModel(@NotNull RangeDispatchProperty property,
                                  float scale,
                                  @Nullable ItemModel fallBack,
                                  @NotNull Map<Float, ItemModel> entries) {
        this.property = property;
        this.scale = scale;
        this.fallBack = fallBack;
        this.entries = entries;
    }

    public RangeDispatchProperty property() {
        return this.property;
    }

    public float scale() {
        return this.scale;
    }

    @Nullable
    public ItemModel fallBack() {
        return this.fallBack;
    }

    public Map<Float, ItemModel> entries() {
        return this.entries;
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "range_dispatch");
        this.property.accept(json);
        JsonArray array = new JsonArray();
        for (Map.Entry<Float, ItemModel> entry : this.entries.entrySet()) {
            float threshold = entry.getKey();
            ItemModel model = entry.getValue();
            JsonObject jo = new JsonObject();
            jo.addProperty("threshold", threshold);
            jo.add("model", model.apply(version));
            array.add(jo);
        }
        json.add("entries", array);
        if (this.scale != 1) {
            json.addProperty("scale", this.scale);
        }
        if (this.fallBack != null) {
            json.add("fallback", this.fallBack.apply(version));
        }
        return json;
    }

    @Override
    public List<Revision> revisions() {
        List<Revision> versions = new ArrayList<>(4);
        if (this.fallBack != null) {
            versions.addAll(this.fallBack.revisions());
        }
        for (ItemModel model : this.entries.values()) {
            versions.addAll(model.revisions());
        }
        return versions;
    }

    @Override
    public List<ModelGeneration> modelsToGenerate() {
        List<ModelGeneration> models = new ArrayList<>(4);
        if (this.fallBack != null) {
            models.addAll(this.fallBack.modelsToGenerate());
        }
        for (ItemModel model : this.entries.values()) {
            models.addAll(model.modelsToGenerate());
        }
        return models;
    }

    private static class Factory implements ItemModelFactory<RangeDispatchItemModel> {

        @Override
        public RangeDispatchItemModel create(ConfigSection section) {
            RangeDispatchProperty property = RangeDispatchProperties.fromConfig(section);
            float scale = section.getFloat(1.0f, "scale");
            ItemModel fallbackModel = section.getValue(ConfigValue::getAsItemModel, "fallback");
            Map<Float, ItemModel> entryMap = new TreeMap<>();
            section.forEachSection(entry -> {
                float threshold = entry.getNonNullFloat("threshold");
                ItemModel model = entry.getValueOrDefault(ConfigValue::getAsItemModel, fallbackModel, "model");
                entryMap.put(threshold, model);
            }, "entries");
            return new RangeDispatchItemModel(
                    property,
                    scale,
                    fallbackModel,
                    entryMap
            );
        }
    }

    private static class Reader implements ItemModelReader<RangeDispatchItemModel> {

        @Override
        public RangeDispatchItemModel read(JsonObject json) {
            JsonArray entriesArray = json.getAsJsonArray("entries");
            if (entriesArray == null) {
                throw new IllegalArgumentException("entries is expected to be a JsonArray");
            }
            Map<Float, ItemModel> entries = new TreeMap<>();
            for (JsonElement entry : entriesArray) {
                if (entry instanceof JsonObject entryObj) {
                    float threshold = entryObj.getAsJsonPrimitive("threshold").getAsFloat();
                    ItemModel model = ItemModels.fromJson(entryObj.getAsJsonObject("model"));
                    entries.put(threshold, model);
                }
            }
            return new RangeDispatchItemModel(RangeDispatchProperties.fromJson(json),
                    json.has("scale") ? json.get("scale").getAsFloat() : 1f,
                    json.has("fallback") ? ItemModels.fromJson(json.getAsJsonObject("fallback")) : null,
                    entries
            );
        }
    }
}
