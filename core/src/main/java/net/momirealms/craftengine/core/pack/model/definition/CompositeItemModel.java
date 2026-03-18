package net.momirealms.craftengine.core.pack.model.definition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.model.generation.ModelGenerationHolder;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CompositeItemModel implements ItemModel {
    public static final ItemModelFactory<CompositeItemModel> FACTORY = new Factory();
    public static final ItemModelReader<CompositeItemModel> READER = new Reader();
    private final List<ItemModel> models;

    public CompositeItemModel(List<ItemModel> models) {
        this.models = models;
    }

    @NotNull
    public List<ItemModel> models() {
        return this.models;
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "composite");
        JsonArray array = new JsonArray();
        for (ItemModel model : this.models) {
            array.add(model.apply(version));
        }
        json.add("models", array);
        return json;
    }

    @Override
    public void collectRevision(Consumer<Revision> consumer) {
        for (ItemModel model : this.models) {
            model.collectRevision(consumer);
        }
    }

    @Override
    public void prepareModelGeneration(Consumer<ModelGenerationHolder> consumer) {
        for (ItemModel model : this.models) {
            model.prepareModelGeneration(consumer);
        }
    }

    private static class Factory implements ItemModelFactory<CompositeItemModel> {

        @Override
        public CompositeItemModel create(ConfigSection section) {
            return new CompositeItemModel(section.getList("models", ItemModels::fromConfig));
        }
    }

    private static class Reader implements ItemModelReader<CompositeItemModel> {

        @Override
        public CompositeItemModel read(JsonObject json) {
            JsonArray models = json.getAsJsonArray("models");
            if (models == null) {
                throw new IllegalArgumentException("models is expected to be a JsonArray");
            }
            List<ItemModel> modelList = new ArrayList<>();
            for (JsonElement model : models) {
                if (!(model instanceof JsonObject jo)) {
                    throw new IllegalArgumentException("model is expected to be a JsonObject");
                }
                modelList.add(ItemModels.fromJson(jo));
            }
            return new CompositeItemModel(modelList);
        }
    }
}
