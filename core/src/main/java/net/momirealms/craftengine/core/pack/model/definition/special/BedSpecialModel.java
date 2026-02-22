package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.util.List;

public final class BedSpecialModel implements SpecialModel {
    public static final SpecialModelFactory<BedSpecialModel> FACTORY = new Factory();
    public static final SpecialModelReader<BedSpecialModel> READER = new Reader();
    private final String texture;

    public BedSpecialModel(String texture) {
        this.texture = texture;
    }

    public String texture() {
        return this.texture;
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "bed");
        json.addProperty("texture", this.texture);
        return json;
    }

    @Override
    public List<Revision> revisions() {
        return List.of();
    }

    private static class Factory implements SpecialModelFactory<BedSpecialModel> {
        @Override
        public BedSpecialModel create(ConfigSection section) {
            return new BedSpecialModel(section.getNonNullIdentifier("texture").asMinimalString());
        }
    }

    private static class Reader implements SpecialModelReader<BedSpecialModel> {
        @Override
        public BedSpecialModel read(JsonObject json) {
            return new BedSpecialModel(json.get("texture").getAsString());
        }
    }
}
