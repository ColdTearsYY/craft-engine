package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.List;

public final class ChestSpecialModel implements SpecialModel {
    public static final SpecialModelFactory<ChestSpecialModel> FACTORY = new Factory();
    public static final SpecialModelReader<ChestSpecialModel> READER = new Reader();
    private final String texture;
    private final float openness;

    public ChestSpecialModel(String texture, float openness) {
        this.texture = texture;
        this.openness = openness;
    }

    public String texture() {
        return this.texture;
    }

    public float openness() {
        return this.openness;
    }

    @Override
    public List<Revision> revisions() {
        return List.of();
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "chest");
        json.addProperty("texture", this.texture);
        if (this.openness > 0) {
            json.addProperty("openness", this.openness);
        }
        return json;
    }

    private static class Factory implements SpecialModelFactory<ChestSpecialModel> {
        @Override
        public ChestSpecialModel create(ConfigSection section) {
            return new ChestSpecialModel(
                    section.getNonNullIdentifier("texture").asMinimalString(),
                    MiscUtils.clamp(section.getFloat("openness"), 0f, 1f)
            );
        }
    }

    private static class Reader implements SpecialModelReader<ChestSpecialModel> {
        @Override
        public ChestSpecialModel read(JsonObject json) {
            return new ChestSpecialModel(
                    json.get("texture").getAsString(),
                    json.has("openness") ? json.get("openness").getAsFloat() : 0
            );
        }
    }
}
