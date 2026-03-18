package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MinecraftVersion;

import java.util.List;

public final class SignSpecialModel implements SpecialModel {
    public static final SpecialModelFactory<SignSpecialModel> FACTORY = new Factory();
    public static final SpecialModelReader<SignSpecialModel> READER = new Reader();
    private final Key type;
    private final String woodType;
    private final String texture;

    public SignSpecialModel(Key type, String woodType, String texture) {
        this.type = type;
        this.woodType = woodType;
        this.texture = texture;
    }

    public Key type() {
        return this.type;
    }

    public String woodType() {
        return this.woodType;
    }

    public String texture() {
        return this.texture;
    }

    @Override
    public List<Revision> revisions() {
        return List.of();
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", this.type.asMinimalString());
        json.addProperty("wood_type", woodType);
        json.addProperty("texture", texture);
        return json;
    }

    private static class Factory implements SpecialModelFactory<SignSpecialModel> {
        private static final String[] WOOD_TYPES = new String[] {"wood_type", "wood-type"};

        @Override
        public SignSpecialModel create(ConfigSection section) {
            return new SignSpecialModel(
                    section.getNonNullIdentifier("type"),
                    section.getNonNullString(WOOD_TYPES),
                    section.getNonNullString("texture")
            );
        }
    }

    private static class Reader implements SpecialModelReader<SignSpecialModel> {
        @Override
        public SignSpecialModel read(JsonObject json) {
            Key type = Key.of(json.get("type").toString());
            String woodType = json.get("wood_type").getAsString();
            String texture = json.get("texture").getAsString();
            return new SignSpecialModel(type, woodType, texture);
        }
    }
}
