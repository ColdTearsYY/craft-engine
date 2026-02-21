package net.momirealms.craftengine.core.pack.model.definition.special;

import com.google.gson.JsonObject;
import net.momirealms.craftengine.core.pack.revision.Revision;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.locale.LocalizedResourceConfigException;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.MinecraftVersion;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public final class ShulkerBoxSpecialModel implements SpecialModel {
    public static final SpecialModelFactory<ShulkerBoxSpecialModel> FACTORY = new Factory();
    public static final SpecialModelReader<ShulkerBoxSpecialModel> READER = new Reader();
    private final String texture;
    private final float openness;
    private final Direction orientation;

    public ShulkerBoxSpecialModel(String texture, float openness, @Nullable Direction orientation) {
        this.texture = texture;
        this.openness = openness;
        this.orientation = orientation;
    }

    public String texture() {
        return this.texture;
    }

    public float openness() {
        return this.openness;
    }

    public Direction orientation() {
        return this.orientation;
    }

    @Override
    public List<Revision> revisions() {
        return List.of();
    }

    @Override
    public JsonObject apply(MinecraftVersion version) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "shulker_box");
        json.addProperty("texture", this.texture);
        if (this.orientation != null) {
            json.addProperty("orientation", this.orientation.name().toLowerCase(Locale.ENGLISH));
        }
        json.addProperty("openness", this.openness);
        return json;
    }

    private static class Factory implements SpecialModelFactory<ShulkerBoxSpecialModel> {
        @Override
        public ShulkerBoxSpecialModel create(ConfigSection section) {
            float openness = section.getFloat("openness");
            String texture = section.getNonNullString("texture");
            Direction orientation = section.getEnum(Direction.class, "orientation");
            if (openness > 1 || openness < 0) {
                throw new LocalizedResourceConfigException("warning.config.item.model.special.shulker_box.invalid_openness", String.valueOf(openness));
            }
            return new ShulkerBoxSpecialModel(texture, openness, orientation);
        }
    }

    private static class Reader implements SpecialModelReader<ShulkerBoxSpecialModel> {
        @Override
        public ShulkerBoxSpecialModel read(JsonObject json) {
            float openness = json.has("openness") ? json.get("openness").getAsFloat() : 0f;
            Direction orientation = json.has("orientation") ? Direction.valueOf(json.get("orientation").getAsString().toUpperCase(Locale.ENGLISH)) : null;
            String texture = json.get("texture").getAsString();
            return new ShulkerBoxSpecialModel(texture, openness, orientation);
        }
    }
}
